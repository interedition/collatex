/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;

import javax.xml.stream.XMLStreamWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ParallelSegmentationApparatus {

  public interface GeneratorCallback {

    void start();

    void segment(SortedMap<Witness, Iterable<Token>> contents);

    void end();
  }

  public static void generate(VariantGraphRanking ranking, GeneratorCallback callback) {

    callback.start();

    final Set<Witness> allWitnesses = ranking.witnesses();
    for (Iterator<Map.Entry<Integer, Collection<VariantGraph.Vertex>>> rowIt = ranking.getByRank().asMap().entrySet().iterator(); rowIt.hasNext(); ) {
      final Map.Entry<Integer, Collection<VariantGraph.Vertex>> row = rowIt.next();
      final int rank = row.getKey();
      final Collection<VariantGraph.Vertex> vertices = row.getValue();

      if (vertices.size() == 1 && Iterables.getOnlyElement(vertices).tokens().isEmpty()) {
        // skip start and end vertex
        continue;
      }

      // spreading vertices with same rank according to their registered transpositions
      final Multimap<Integer, VariantGraph.Vertex> verticesByTranspositionRank = HashMultimap.create();
      for (VariantGraph.Vertex v : vertices) {
        int transpositionRank = 0;
        for (VariantGraph.Transposition transposition : v.transpositions()) {
          for (VariantGraph.Vertex tv : transposition) {
            transpositionRank += (ranking.apply(tv).intValue() - rank);
          }
        }
        verticesByTranspositionRank.put(transpositionRank, v);
      }

      // render segments
      for (Iterator<Integer> transpositionRankIt = Ordering.natural().immutableSortedCopy(verticesByTranspositionRank.keySet()).iterator(); transpositionRankIt.hasNext() ;) {
        final Multimap<Witness, Token> tokensByWitness = HashMultimap.create();
        for (VariantGraph.Vertex v : verticesByTranspositionRank.get(transpositionRankIt.next())) {
          for (Token token : v.tokens()) {
            tokensByWitness.put(token.getWitness(), token);
          }
        }

        final SortedMap<Witness, Iterable<Token>> cellContents = Maps.newTreeMap(Witness.SIGIL_COMPARATOR);
        for (Witness witness : allWitnesses) {
          cellContents.put(witness, tokensByWitness.containsKey(witness) ? Iterables.unmodifiableIterable(tokensByWitness.get(witness)) : Collections.<Token>emptySet());
        }

        callback.segment(cellContents);
      }
    }

    callback.end();
  }
}
