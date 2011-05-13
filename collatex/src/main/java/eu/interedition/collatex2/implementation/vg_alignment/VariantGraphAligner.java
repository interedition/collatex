/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex2.implementation.vg_alignment;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.containers.graph.VariantGraphEdge;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraphVertex;
import eu.interedition.collatex2.implementation.vg_analysis.Analyzer;
import eu.interedition.collatex2.implementation.vg_analysis.IAnalysis;
import eu.interedition.collatex2.implementation.vg_analysis.ISequence;
import eu.interedition.collatex2.implementation.vg_analysis.ITransposition2;
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
    return new Alignment2(graph, witness, tokenMatches);
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
    if (graph.isEmpty()) {
      useShortCut(witness);
      return;
    }
    // align the witness
    IAlignment2 alignment = align(witness);
    // analyze the results
    Analyzer analyzer = new Analyzer();
    IAnalysis analysis = analyzer.analyze(alignment);
    List<ITransposition2> transpositions = analysis.getTranspositions();
    List<ITokenMatch> matches = alignment.getTokenMatches();
    makeEdgesForMatches(witness, matches, transpositions);
  }

  private void useShortCut(IWitness firstWitness) {
    List<IVariantGraphVertex> newVertices = Lists.newArrayList();
    for (INormalizedToken token : firstWitness.getTokens()) {
      final IVariantGraphVertex vertex = addNewVertex(token.getNormalized(), token);
      vertex.addToken(firstWitness, token);
      newVertices.add(vertex);
    }
    IVariantGraphVertex previous = graph.getStartVertex();
    for (IVariantGraphVertex vertex : newVertices) {
      addNewEdge(previous, vertex, firstWitness);
      previous = vertex;
    }
    addNewEdge(previous, graph.getEndVertex(), firstWitness);
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

  @Override
  public IAnalysis analyze(IWitness witness) {
    throw new RuntimeException("This functionality is not supported by this implementation!");
  }


}
