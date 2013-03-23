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

package eu.interedition.collatex.needlemanwunsch;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NeedlemanWunschAlgorithm extends CollationAlgorithm.Base {

  private final Comparator<Token> comparator;

  public NeedlemanWunschAlgorithm(Comparator<Token> comparator) {
    this.comparator = comparator;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void collate(VariantGraph against, Iterable<Token> witness) {
    final DefaultNeedlemanWunschScorer scorer = new DefaultNeedlemanWunschScorer(comparator);

    final Set<VariantGraph.Vertex>[] ranks = Iterables.toArray(VariantGraphRanking.of(against), Set.class);
    final Token[] tokens = Iterables.toArray(witness, Token.class);

    final Map<Token, VariantGraph.Vertex> alignments = Maps.newHashMap();
    for (Map.Entry<Set<VariantGraph.Vertex>, Token> alignment : align(ranks, tokens, scorer).entrySet()) {
      boolean aligned = false;
      final Token token = alignment.getValue();
      for (VariantGraph.Vertex vertex : alignment.getKey()) {
        for (Token vertexToken : vertex.tokens()) {
          if (comparator.compare(vertexToken, token) == 0) {
            alignments.put(token, vertex);
            aligned = true;
            break;
          }
        }
        if (aligned) {
          break;
        }
      }
    }

    merge(against, witness, alignments);
  }

  public static <A,B> Map<A,B> align(A[] a, B[] b, NeedlemanWunschScorer<A, B> scorer) {

    final Map<A, B> alignments = Maps.newHashMap();
    final float[][] matrix = new float[a.length + 1][b.length + 1];

    int ac = 0;
    int bc = 0;
    while (ac < a.length) {
      matrix[ac++][0] = scorer.gap() * ac;
    }
    while (bc < b.length) {
      matrix[0][bc++] = scorer.gap() * bc;
    }

    ac = 1;
    for (A aElement : a) {
      bc = 1;
      for (B bElement : b) {
        final float k = matrix[ac - 1][bc - 1] + scorer.score(aElement, bElement);
        final float l = matrix[ac - 1][bc] + scorer.gap();
        final float m = matrix[ac][bc - 1] + scorer.gap();
        matrix[ac][bc++] = Math.max(Math.max(k, l), m);
      }
      ac++;
    }

    ac = a.length;
    bc = b.length;
    while (ac > 0 && bc > 0) {
      final float score = matrix[ac][bc];
      final float scoreDiag = matrix[ac - 1][bc - 1];
      final float scoreUp = matrix[ac][bc - 1];
      final float scoreLeft = matrix[ac - 1][bc];

      if (score == scoreDiag + scorer.score(a[ac - 1], b[bc - 1])) {
        // match
        alignments.put(a[ac - 1], b[bc - 1]);
        ac--;
        bc--;
      } else if (score == scoreLeft + scorer.gap()) {
        ac--;
      } else if (score == scoreUp + scorer.gap()) {
        bc--;
      }
    }

    return alignments;
  }
}