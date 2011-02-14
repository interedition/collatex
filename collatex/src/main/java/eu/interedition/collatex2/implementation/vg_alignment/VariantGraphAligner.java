package eu.interedition.collatex2.implementation.vg_alignment;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.containers.graph.VariantGraphEdge;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraphVertex;
import eu.interedition.collatex2.implementation.vg_analysis.Analysis;
import eu.interedition.collatex2.implementation.vg_analysis.IAnalysis;
import eu.interedition.collatex2.implementation.vg_analysis.ISequence;
import eu.interedition.collatex2.implementation.vg_analysis.ITransposition2;
import eu.interedition.collatex2.implementation.vg_analysis.SequenceDetection2;
import eu.interedition.collatex2.interfaces.IAligner;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphAligner implements IAligner {

  private final IVariantGraph graph;

  public VariantGraphAligner(IVariantGraph graph) {
    this.graph = graph;
  }

  // This method does the alignment between a variantgraph and a witness
  // The alignment is done using token matching
  // purpose: find the matching tokens between the graph and the witness
  // it uses the VariantGraphIndexMatcher class for that 
  public IAlignment2 align(IWitness witness) {
    TokenIndexMatcher matcher = new TokenIndexMatcher(graph);
    List<ITokenMatch> tokenMatches = matcher.getMatches(witness);
    return new Alignment2(tokenMatches);
  }


  // write
  // NOTE: tokenA is the token from the Witness
  // For every token in the witness we have to map a VariantNode
  // for matches such a node should already exist
  // however for additions and replacements this will not be the case
  // then we need to add the arcs
  // in some cases the arcs may already exist
  // if they already exist we need to add the witness to the
  // existing arc!
  public void addWitness(IWitness witness) {
    // align the witness
    VariantGraphAligner aligner = new VariantGraphAligner(graph);
    IAlignment2 alignment = aligner.align(witness);
    List<ITokenMatch> matches = alignment.getTokenMatches();
    // analyze the results
    // TODO: Make separate analyzer class?
    SequenceDetection2 seqDetection = new SequenceDetection2(matches, graph, witness);
    List<ISequence> sequences = seqDetection.chainTokenMatches();
    IAnalysis analysis = new Analysis(sequences, graph);
    List<ITransposition2> transpositions = analysis.getTranspositions();
    makeEdgesForMatches(witness, matches, transpositions);
  }

  private void makeEdgesForMatches(IWitness witness, List<ITokenMatch> matches, List<ITransposition2> transpositions) {
    // Map Tokens in the Witness to the Matches
    Map<INormalizedToken, ITokenMatch> witnessTokenToMatch;
    Map<INormalizedToken, ITokenMatch> witnessTokenToTranspositionMatch;
    witnessTokenToMatch = Maps.newLinkedHashMap();
    witnessTokenToTranspositionMatch = Maps.newLinkedHashMap();
    for (ITokenMatch match : matches) {
      INormalizedToken tokenA = match.getTokenA();
      witnessTokenToMatch.put(tokenA, match);
    }
    final Stack<ITransposition2> transToCheck = new Stack<ITransposition2>();
    transToCheck.addAll(transpositions);
    Collections.reverse(transToCheck);
    while (!transToCheck.isEmpty()) {
      final ITransposition2 top = transToCheck.pop();
      // System.out.println("Detected transposition: "+top.getSequenceA().toString());
      final ITransposition2 mirrored = findMirroredTransposition(transToCheck, top);
        if (mirrored != null && transpositionsAreNear(top, mirrored)) {
          // System.out.println("Detected mirror: "+mirrored.getSequenceA().toString());
          // System.out.println("Keeping: transposition " + top.toString());
          // System.out.println("Removing: transposition " + mirrored.toString());
          // remove mirrored transpositions (a->b, b->a) from transpositions
          transToCheck.remove(mirrored);
          // make addition out of mirrored transposition
          // keep top transposition as match!
          // remove tokens from sequence from the matches!
          ISequence sequenceA = mirrored.getSequenceA();
          removeSequenceFromMatches(witnessTokenToMatch, witnessTokenToTranspositionMatch, sequenceA); 
       } else {
         // treat transposition as a replacement
         ISequence sequenceA = top.getSequenceA();
         removeSequenceFromMatches(witnessTokenToMatch, witnessTokenToTranspositionMatch, sequenceA);
       }
    }
    addWitnessToGraph(witness, witnessTokenToMatch, witnessTokenToTranspositionMatch);
  }

  //FIXME: why does getWitnessPhrase work and not getBasePhrase?
  // Note: this only calculates the distance between the vertices in the graph.
  // Note: it does not take into account a possible distance in the tokens in the witness!
  private boolean transpositionsAreNear(ITransposition2 top, ITransposition2 mirrored) {
    INormalizedToken lastToken = top.getSequenceA().getWitnessPhrase().getLastToken();
    INormalizedToken firstToken = mirrored.getSequenceA().getWitnessPhrase().getFirstToken();
    //    System.out.println(lastToken.getClass());
    //    System.out.println(firstToken.getClass());
    boolean isNear = graph.isNear(lastToken, firstToken);
    return isNear;
  }

  private void removeSequenceFromMatches(Map<INormalizedToken, ITokenMatch> witnessTokenToMatch, Map<INormalizedToken, ITokenMatch> witnessTokenToTranspositionMatch, ISequence sequenceA) {
    for (INormalizedToken witnessToken : sequenceA.getBasePhrase().getTokens()) {
      if (!witnessTokenToMatch.containsKey(witnessToken)) {
        throw new RuntimeException("Could not remove match from map!");
      }  
      ITokenMatch tokenMatch = witnessTokenToMatch.remove(witnessToken);
      witnessTokenToTranspositionMatch.put(witnessToken, tokenMatch);
    }
  }

  private static ITransposition2 findMirroredTransposition(final Stack<ITransposition2> transToCheck, final ITransposition2 original) {
    for (final ITransposition2 transposition : transToCheck) {
      if (transposition.getSequenceA().getNormalized().equals(original.getSequenceB().getNormalized())) {
        if (transposition.getSequenceB().getNormalized().equals(original.getSequenceA().getNormalized())) {
          return transposition;
        }
      }
    }
    return null;
  }


  private void addWitnessToGraph(IWitness witness, Map<INormalizedToken, ITokenMatch> witnessTokenToMatch, Map<INormalizedToken, ITokenMatch> witnessTokenToTranspositionMatch) {
    IVariantGraphVertex current = graph.getStartVertex();
    for (INormalizedToken token : witness.getTokens()) {
      IVariantGraphVertex end;
      if (!witnessTokenToMatch.containsKey(token)) {
        // NOTE: here we determine that the token is an addition/replacement!
        INormalizedToken vertexKey = (witnessTokenToTranspositionMatch.containsKey(token)) ? ((IVariantGraphVertex) witnessTokenToTranspositionMatch.get(token).getTokenB()).getVertexKey() : token;
        end = addNewVertex(token.getNormalized(), vertexKey);
      } else {
        // NOTE: it is a match!
        ITokenMatch tokenMatch = witnessTokenToMatch.get(token);
        end = (IVariantGraphVertex) tokenMatch.getBaseToken();
      }
      connectBeginToEndVertex(current, end, witness);
      end.addToken(witness, token);
      current = end;
    }
    // adds edge from last vertex to end vertex
    IVariantGraphVertex end = graph.getEndVertex();
    connectBeginToEndVertex(current, end, witness);
  }

  // write
  private void connectBeginToEndVertex(IVariantGraphVertex begin, IVariantGraphVertex end, IWitness witness) {
    if (graph.containsEdge(begin, end)) {
      IVariantGraphEdge existingEdge = graph.getEdge(begin, end);
      existingEdge.addWitness(witness);
    } else {
      addNewEdge(begin, end, witness);
    }
  }


//  public static IVariantGraph create(IWitness... witnesses) {
//    List<IWitness> witnessList = Lists.newArrayList(witnesses);
//    if (witnessList.isEmpty()) {
//      return VariantGraph2.create();
//    }
//    IWitness w1 = witnessList.remove(0);
//    IWitness[] w2 = witnessList.toArray(new IWitness[witnessList.size()]);
//    VariantGraph2 graph = VariantGraph2.create(w1);
//    VariantGraphAligner aligner = new VariantGraphAligner(graph);
//    for (IWitness witness : w2) {
//      aligner.addWitness(witness);
//    }
//    return graph;
//  }

  @Override
  public IVariantGraph getResult() {
    return graph;
  }

  @Override
  public IAligner add(IWitness... witnesses) {
    for (IWitness witness : witnesses) {
      addWitness(witness);
    }
    return this;
  }

  //write
  private IVariantGraphVertex addNewVertex(String normalized, INormalizedToken vertexKey) {
    final VariantGraphVertex vertex = new VariantGraphVertex(normalized, vertexKey);
    graph.addVertex(vertex);
    return vertex;
  }

  //write
  private void addNewEdge(IVariantGraphVertex begin, IVariantGraphVertex end, IWitness witness) {
    IVariantGraphEdge edge = new VariantGraphEdge();
    edge.addWitness(witness);
    graph.addEdge(begin, end, edge);
  }


}
