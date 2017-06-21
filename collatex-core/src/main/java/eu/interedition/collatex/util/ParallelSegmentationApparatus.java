/*
 * Copyright (c) 2015 The Interedition Development Group.
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

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;

import java.util.*;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
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
      for (final Map.Entry<Integer, Set<VariantGraph.Vertex>> row : ranking.getByRank().entrySet()) {
        final int rank = row.getKey();
        final Collection<VariantGraph.Vertex> verticesOfRank = row.getValue();


        if (verticesOfRank.size() == 1 && verticesOfRank.stream().findFirst().map(VariantGraph.Vertex::tokens).map(Set::isEmpty).orElse(false)) {
          // skip start and end vertex
          continue;
        }

        // spreading vertices with same rank according to their registered transpositions
        final SortedMap<Integer, List<VariantGraph.Vertex>> verticesByTranspositionRank = new TreeMap<>();
        for (VariantGraph.Vertex v : verticesOfRank) {
          int transpositionRank = 0;
          for (Set<VariantGraph.Vertex> transposition : v.transpositions()) {
            for (VariantGraph.Vertex tv : transposition) {
              transpositionRank += (ranking.apply(tv) - rank);
            }
          }
          verticesByTranspositionRank.computeIfAbsent(transpositionRank, r -> new LinkedList<>()).add(v);
        }

        // render segments
        verticesByTranspositionRank.values().forEach(vertices -> {
          final Map<Witness, List<Token>> tokensByWitness = new HashMap<>();
          for (VariantGraph.Vertex v : vertices) {
            for (Token token : v.tokens()) {
              tokensByWitness.computeIfAbsent(token.getWitness(), w -> new LinkedList<>()).add(token);
            }
          }

          final SortedMap<Witness, Iterable<Token>> cellContents = new TreeMap<>(Witness.SIGIL_COMPARATOR);
          for (Witness witness : allWitnesses) {
            cellContents.put(witness, Collections.unmodifiableCollection(tokensByWitness.getOrDefault(witness, Collections.emptyList())));
          }

          callback.segment(cellContents);
        });
      }

        callback.end();
    }
}
