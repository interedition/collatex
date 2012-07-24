/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright 2010-2012 The Interedition Development Group.
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
package eu.interedition.collatex.dekker;

import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.GraphRelationshipType;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;

/**
 *
 * @author Ronald
 */
public class PhraseMatchDetector {

  public List<List<Match>> detect(Map<Token, VariantGraphVertex> linkedTokens, VariantGraph base, Iterable<Token> tokens) {
    List<List<Match>> phraseMatches = Lists.newArrayList();
    List<VariantGraphVertex> basePhrase = Lists.newArrayList();
    List<Token> witnessPhrase = Lists.newArrayList();
    VariantGraphVertex previous = base.getStart();

    for (Token token : tokens) {
      if (!linkedTokens.containsKey(token)) {
        continue;
      }
      VariantGraphVertex baseVertex = linkedTokens.get(token);
      // requirements:
      // - previous and base vertex should have the same witnesses
      // - there should be a directed edge between previous and base vertex
      // - there may not be a longer path between previous and base vertex
      boolean sameWitnesses = previous.witnesses().equals(baseVertex.witnesses());
      boolean directedEdge = directedEdgeBetween(previous, baseVertex);
      boolean isNear = sameWitnesses && directedEdge && (Iterables.size(previous.outgoing()) == 1 || Iterables.size(baseVertex.incoming()) == 1);
      if (!isNear) {
        if (!basePhrase.isEmpty()) {
          phraseMatches.add(Match.createPhraseMatch(basePhrase, witnessPhrase));
          basePhrase.clear();
          witnessPhrase.clear();
        }
      }
      basePhrase.add(baseVertex);
      witnessPhrase.add(token);
      previous = baseVertex;
    }
    if (!basePhrase.isEmpty()) {
      phraseMatches.add(Match.createPhraseMatch(basePhrase, witnessPhrase));
    }
    return phraseMatches;
  }

  private boolean directedEdgeBetween(VariantGraphVertex a, VariantGraphVertex b) {
    final Node aNode = a.getNode();
    final Node bNode = b.getNode();
    for (Relationship r : aNode.getRelationships(Direction.OUTGOING, GraphRelationshipType.PATH)) {
      if (r.getEndNode().equals(bNode)) {
        return true;
      }
    }
    return false;
  }
}
