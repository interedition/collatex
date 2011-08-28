package eu.interedition.collatex2.implementation.vg_alignment;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.containers.graph.VariantGraphEdge;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraphVertex;
import eu.interedition.collatex2.implementation.vg_analysis.Analysis;
import eu.interedition.collatex2.implementation.vg_analysis.IAnalysis;
import eu.interedition.collatex2.implementation.vg_analysis.ISequence;
import eu.interedition.collatex2.implementation.vg_analysis.ITransposition2;
import eu.interedition.collatex2.implementation.vg_analysis.SequenceDetection3;
import eu.interedition.collatex2.interfaces.IAligner;
import eu.interedition.collatex2.interfaces.ILinker;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphAligner implements IAligner {
  private final IVariantGraph graph;
  private Analysis analysis;

  public VariantGraphAligner(IVariantGraph graph) {
    this.graph = graph;
  }

  public void addWitness(IWitness witness) {
    // 1. Do the matching and linking of tokens
    //TODO: the TokenLinker class should be replaced by the new linker class
    //TODO: based on the decision graph
    TokenLinker tokenLinker = new TokenLinker();
    Map<INormalizedToken, INormalizedToken> linkedTokens = linkTheTokens(witness, tokenLinker);
    // 2. Determine sequences
    SuperbaseCreator creator = new SuperbaseCreator();
    IWitness superbase = creator.create(graph);
    SequenceDetection3 detection = new SequenceDetection3();
    List<ISequence> sequences = detection.getSequences(linkedTokens, superbase, witness);
    // 3. Determine transpositions of the sequences
    Analysis analysis = new Analysis(sequences, superbase); 
    //NOTE: This is not very nice!
    this.analysis = analysis;
    List<ITransposition2> transpositions = analysis.getTranspositions();
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
    IVariantGraphEdge edge = graph.getEdge(previous, graph.getEndVertex());
    if (edge == null) edge = addNewEdge(previous, graph.getEndVertex());
    edge.addWitness(witness);
  }

  private Map<INormalizedToken, INormalizedToken> linkTheTokens(
      IWitness witness, ILinker tokenLinker) {
    Map<INormalizedToken, INormalizedToken> linkedTokens;
    if (graph.isEmpty()) {
      linkedTokens = Maps.newLinkedHashMap();
    } 
    linkedTokens = tokenLinker.link(graph, witness);
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


  @Override
  public IAlignment2 align(IWitness witness) {
    throw new RuntimeException("NOT YET IMPLEMENTED!");
  }

  public IAnalysis getAnalysis() {
    return analysis;
  }

  //NOTE: It would be better to not use getNormalized here!
  //NOTE: This does not work with a custom matching function
  static ITransposition2 findMirroredTransposition(final Stack<ITransposition2> transToCheck, final ITransposition2 original) {
    for (final ITransposition2 transposition : transToCheck) {
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
  static boolean transpositionsAreNear(ITransposition2 top, ITransposition2 mirrored, IWitness witness) {
    INormalizedToken lastToken = top.getSequenceB().getWitnessPhrase().getLastToken();
    INormalizedToken firstToken = mirrored.getSequenceB().getWitnessPhrase().getFirstToken();
    return witness.isNear(lastToken, firstToken);
  }

  // NOTE: this method should not return the original sequence when a mirror exists!
  static List<ISequence> getSequencesThatAreTransposed(List<ITransposition2> transpositions, IWitness witness) {
    List<ISequence> transposedSequences = Lists.newArrayList();
    final Stack<ITransposition2> transToCheck = new Stack<ITransposition2>();
    transToCheck.addAll(transpositions);
    Collections.reverse(transToCheck);
    while (!transToCheck.isEmpty()) {
      final ITransposition2 top = transToCheck.pop();
      // System.out.println("Detected transposition: "+top.getSequenceA().toString());
      final ITransposition2 mirrored = VariantGraphAligner.findMirroredTransposition(transToCheck, top);
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

  static Map<INormalizedToken, INormalizedToken> determineAlignedTokens(Map<INormalizedToken, INormalizedToken> linkedTokens, List<ITransposition2> transpositions, IWitness witness) {
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
