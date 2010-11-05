package eu.interedition.collatex2.implementation.containers.graph;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.vg_alignment.IAlignment2;
import eu.interedition.collatex2.implementation.vg_alignment.VariantGraphAligner;
import eu.interedition.collatex2.implementation.vg_analysis.Analysis;
import eu.interedition.collatex2.implementation.vg_analysis.IAnalysis;
import eu.interedition.collatex2.implementation.vg_analysis.ISequence;
import eu.interedition.collatex2.implementation.vg_analysis.ITransposition2;
import eu.interedition.collatex2.implementation.vg_analysis.SequenceDetection2;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraph2Creator {

  private final VariantGraph2 graph;

  public VariantGraph2Creator(VariantGraph2 graph) {
    this.graph = graph;
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

  ///write
  //  private void makeEdgesForMatches(IWitness witness, List<ITokenMatch> matches, List<ITransposition2> transpositions) {
  //    // Map Tokens in the Witness to the Matches
  //    Map<INormalizedToken, ITokenMatch> witnessTokenToMatch;
  //    witnessTokenToMatch = Maps.newLinkedHashMap();
  //    for (ITokenMatch match : matches) {
  //      INormalizedToken tokenA = match.getTokenA();
  //      witnessTokenToMatch.put(tokenA, match);
  //    }
  //    // delete transpositions from map
  //    // TODO: Rename IMatch2 to IMatchSequence?
  //    for (ITransposition2 trans : transpositions) {
  //      ISequence sequenceA = trans.getSequenceA();
  //      // TODO: check whether it is matchA
  //      for (INormalizedToken witnessToken : sequenceA.getBasePhrase().getTokens()) {
  //        // NOTE: sanity check
  //        if (witnessTokenToMatch.containsKey(witnessToken)) {
  //          witnessTokenToMatch.remove(witnessToken);
  //        } else {
  //          throw new RuntimeException("Could not remove match from map!");
  //        }
  //      }
  //    }
  //    addWitnessToGraph(witness, witnessTokenToMatch);
  //  }

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
    for (ITransposition2 trans : transpositions) {
      ISequence sequenceA = trans.getSequenceA();
      for (INormalizedToken witnessToken : sequenceA.getBasePhrase().getTokens()) {
        if (witnessTokenToMatch.containsKey(witnessToken)) {
          ITokenMatch tokenMatch = witnessTokenToMatch.remove(witnessToken);
          witnessTokenToTranspositionMatch.put(witnessToken, tokenMatch);
        } else {
          throw new RuntimeException("Could not remove match from map!");
        }
      }
    }
    addWitnessToGraph(witness, witnessTokenToMatch, witnessTokenToTranspositionMatch);
  }

  private void addWitnessToGraph(IWitness witness, Map<INormalizedToken, ITokenMatch> witnessTokenToMatch, Map<INormalizedToken, ITokenMatch> witnessTokenToTranspositionMatch) {
    IVariantGraphVertex current = graph.getStartVertex();
    for (INormalizedToken token : witness.getTokens()) {
      IVariantGraphVertex end;
      if (!witnessTokenToMatch.containsKey(token)) {
        // NOTE: here we determine that the token is an addition/replacement!
        INormalizedToken vertexKey = (witnessTokenToTranspositionMatch.containsKey(token)) ? witnessTokenToTranspositionMatch.get(token).getTokenA() : token;
        end = graph.addNewVertex(token.getNormalized(), vertexKey);
      } else {
        // NOTE: it is a match!
        ITokenMatch tokenMatch = witnessTokenToMatch.get(token);
        end = (IVariantGraphVertex) tokenMatch.getTokenB();
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
      graph.addNewEdge(begin, end, witness);
    }
  }

  public static IVariantGraph create(IWitness... witnesses) {
    List<IWitness> witnessList = Lists.newArrayList(witnesses);
    IWitness w1 = witnessList.remove(0);
    IWitness[] w2 = witnessList.toArray(new IWitness[witnessList.size()]);
    VariantGraph2 graph = VariantGraph2.create(w1);
    VariantGraph2Creator creator = new VariantGraph2Creator(graph);
    for (IWitness witness : w2) {
      creator.addWitness(witness);
    }
    return graph;
  }

}
