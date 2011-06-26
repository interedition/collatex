package eu.interedition.collatex2.implementation.vg_alignment;

import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.vg_analysis.Analysis;
import eu.interedition.collatex2.implementation.vg_analysis.ISequence;
import eu.interedition.collatex2.implementation.vg_analysis.ITransposition;
import eu.interedition.collatex2.implementation.vg_analysis.SequenceDetection3;
import eu.interedition.collatex2.interfaces.*;

//TODO: rename to my new variant graph builder
//TODO: extract the real aligner out of this class
public class VariantGraphAligner implements IAligner {
  private final VariantGraph graph;

  public VariantGraphAligner(VariantGraph graph) {
    this.graph = graph;
  }

  @Override
  public IAligner add(IWitness... witnesses) {
    for (IWitness witness : witnesses) {
      // 1. Do the matching and linking of tokens
      final SuperbaseCreator creator = new SuperbaseCreator();
      final IWitness superbase = creator.create(graph);
      graph.listener().newSuperbase(superbase);

      final Map<INormalizedToken, INormalizedToken> linkedTokens = linkTheTokens(witness, superbase);
      graph.listener().newLinkedTokenMap(graph, witness, linkedTokens);

      final List<ITokenMatch> tokenMatches = new ArrayList<ITokenMatch>(linkedTokens.size());
      for (Map.Entry<INormalizedToken, INormalizedToken> tokenLink : linkedTokens.entrySet()) {
        tokenMatches.add(new TokenMatch(tokenLink.getValue(), tokenLink.getKey()));
      }
      graph.listener().newAlignment(new Alignment(graph, witness, tokenMatches));

      // 2. Determine sequences
      SequenceDetection3 detection = new SequenceDetection3();
      List<ISequence> sequences = detection.getSequences(linkedTokens, superbase, witness);

      // 3. Determine transpositions of the sequences
      final Analysis analysis = new Analysis(sequences, superbase);

      List<ITransposition> transpositions = analysis.getTranspositions();
      Map<INormalizedToken, INormalizedToken> alignedTokens;
      alignedTokens = VariantGraphAligner.determineAlignedTokens(linkedTokens, transpositions, witness);
      IVariantGraphVertex previous =  graph.getStartVertex();
      for (INormalizedToken token : witness.getTokens()) {
        // determine whether this token is a match or not
        // System.out.println(token+":"+linkedTokens.containsKey(token));
        INormalizedToken vertexKey = linkedTokens.containsKey(token)  ? ((IVariantGraphVertex)linkedTokens.get(token)).getVertexKey() : token;
        IVariantGraphVertex vertex = alignedTokens.containsKey(token) ? (IVariantGraphVertex) linkedTokens.get(token) : addNewVertex(token.getNormalized(), vertexKey);
        IVariantGraphEdge edge = graph.getEdge(previous, vertex);
        if (edge == null) edge = addNewEdge(previous, vertex);
        vertex.addToken(witness, token);
        edge.addWitness(witness);
        previous = vertex;
      }

      graph.listener().newAnalysis(analysis);

      IVariantGraphEdge edge = graph.getEdge(previous, graph.getEndVertex());
      if (edge == null) edge = addNewEdge(previous, graph.getEndVertex());
      edge.addWitness(witness);
    }
    return this;
  }

  @Override
  public IVariantGraph getResult() {
    return graph;
  }

  private Map<INormalizedToken, INormalizedToken> linkTheTokens(
      IWitness witness, IWitness superbase) {
    Map<INormalizedToken, INormalizedToken> linkedTokens;
    if (graph.isEmpty()) {
      linkedTokens = Maps.newLinkedHashMap();
    }
    TokenLinker linker = new TokenLinker();
    linkedTokens = linker.link2(superbase, witness);
    return linkedTokens;
  }

  //write
  private IVariantGraphVertex addNewVertex(String normalized, INormalizedToken vertexKey) {
    // System.out.println("Add vertex "+normalized);
    IVariantGraphVertex vertex = new VariantGraphVertex(normalized, vertexKey);
    graph.addVertex(vertex);
    return vertex;
  }

  //write
  private IVariantGraphEdge addNewEdge(IVariantGraphVertex begin, IVariantGraphVertex end) {
    // System.out.println("Add edge between "+begin.getNormalized()+ " and " + end.getNormalized());
    IVariantGraphEdge edge = new VariantGraphEdge();
    graph.addEdge(begin, end, edge);
    return edge;
  }


  @Override
  public IAlignment align(IWitness witness) {
    throw new RuntimeException("NOT YET IMPLEMENTED!");
  }

  //NOTE: It would be better to not use getNormalized here!
  //NOTE: This does not work with a custom matching function
  static ITransposition findMirroredTransposition(final Stack<ITransposition> transToCheck, final ITransposition original) {
    for (final ITransposition transposition : transToCheck) {
      if (transposition.getSequenceA().getNormalized().equals(original.getSequenceB().getNormalized())) {
        if (transposition.getSequenceB().getNormalized().equals(original.getSequenceA().getNormalized())) {
          return transposition;
        }
      }
    }
    return null;
  }

  // Note: this only calculates the distance between the tokens in the witness.
  // Note: it does not take into account a possible distance in the vertices in the graph!
  static boolean transpositionsAreNear(ITransposition top, ITransposition mirrored, IWitness witness) {
    INormalizedToken lastToken = top.getSequenceB().getWitnessPhrase().getLastToken();
    INormalizedToken firstToken = mirrored.getSequenceB().getWitnessPhrase().getFirstToken();
    return witness.isNear(lastToken, firstToken);
  }

  // NOTE: this method should not return the original sequence when a mirror exists!
  static List<ISequence> getSequencesThatAreTransposed(List<ITransposition> transpositions, IWitness witness) {
    List<ISequence> transposedSequences = Lists.newArrayList();
    final Stack<ITransposition> transToCheck = new Stack<ITransposition>();
    transToCheck.addAll(transpositions);
    Collections.reverse(transToCheck);
    while (!transToCheck.isEmpty()) {
      final ITransposition top = transToCheck.pop();
      // System.out.println("Detected transposition: "+top.getSequenceA().toString());
      final ITransposition mirrored = VariantGraphAligner.findMirroredTransposition(transToCheck, top);
      // remove mirrored transpositions (a->b, b->a) from transpositions
      if (mirrored != null && VariantGraphAligner.transpositionsAreNear(top, mirrored, witness)) {
        // System.out.println("Detected mirror: "+mirrored.getSequenceA().toString());
        // System.out.println("Keeping: transposition " + top.toString());
        // System.out.println("Removing: transposition " + mirrored.toString());
        transToCheck.remove(mirrored);
        transposedSequences.add(mirrored.getSequenceA());
      } else {
        transposedSequences.add(top.getSequenceA());
      }
    }
    return transposedSequences;
  }

  static Map<INormalizedToken, INormalizedToken> determineAlignedTokens(Map<INormalizedToken, INormalizedToken> linkedTokens, List<ITransposition> transpositions, IWitness witness) {
    Map<INormalizedToken, INormalizedToken> alignedTokens = Maps.newLinkedHashMap();
    alignedTokens.putAll(linkedTokens);
    List<ISequence> sequencesThatAreTransposed = VariantGraphAligner.getSequencesThatAreTransposed(transpositions, witness);
    for (ISequence sequenceA : sequencesThatAreTransposed) {
      for (INormalizedToken token : sequenceA.getWitnessPhrase().getTokens()) {
        alignedTokens.remove(token);
      }
    }
    return alignedTokens;
  }
}
