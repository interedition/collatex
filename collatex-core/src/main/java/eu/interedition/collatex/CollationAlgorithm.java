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

package eu.interedition.collatex;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.interedition.collatex.VariantGraph.Vertex;
import eu.interedition.collatex.dekker.Match;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface CollationAlgorithm {

  void collate(VariantGraph against, Iterable<Token> witness);

  void collate(VariantGraph against, Iterable<Token>... witnesses);

  void collate(VariantGraph against, List<? extends Iterable<Token>> witnesses);

  abstract class Base implements CollationAlgorithm {
    protected final Logger LOG = Logger.getLogger(getClass().getName());
    protected Map<Token, VariantGraph.Vertex> witnessTokenVertices;

    @Override
    public void collate(VariantGraph against, Iterable<Token>... witnesses) {
      collate(against, Arrays.asList(witnesses));
    }

    @Override
    public void collate(VariantGraph against, List<? extends Iterable<Token>> witnesses) {
      for (Iterable<Token> witness : witnesses) {
        if (LOG.isLoggable(Level.FINE)) {
          LOG.log(Level.FINE, "heap space: {0}/{1}", new Object[] {
                  Runtime.getRuntime().totalMemory(),
                  Runtime.getRuntime().maxMemory()
          });
        }
        collate(against, witness);
      }
    }

    protected void merge(VariantGraph into, Iterable<Token> witnessTokens, Map<Token, VariantGraph.Vertex> alignments) {
      Preconditions.checkArgument(!Iterables.isEmpty(witnessTokens), "Empty witness");
      final Witness witness = Iterables.getFirst(witnessTokens, null).getWitness();

      if (LOG.isLoggable(Level.FINE)) {
        LOG.log(Level.FINE, "{0} + {1}: Merge comparand into graph", new Object[] { into, witness });
      }
      witnessTokenVertices = Maps.newHashMap();
      VariantGraph.Vertex last = into.getStart();
      final Set<Witness> witnessSet = Collections.singleton(witness);
      for (Token token : witnessTokens) {
        VariantGraph.Vertex matchingVertex = alignments.get(token);
        if (matchingVertex == null) {
          matchingVertex = into.add(token);
        } else {
          if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Match: {0} to {1}", new Object[] { matchingVertex, token });
          }
          matchingVertex.add(Collections.singleton(token));
        }
        witnessTokenVertices.put(token, matchingVertex);

        into.connect(last, matchingVertex, witnessSet);
        last = matchingVertex;
      }
      into.connect(last, into.getEnd(), witnessSet);
    }

    protected void mergeTranspositions(VariantGraph into, List<List<Match>> transpositions) {
      for (List<Match> transposedPhrase : transpositions) {
        if (LOG.isLoggable(Level.FINE)) {
          LOG.log(Level.FINE, "Transposition: {0}", transposedPhrase);
        }
        final Set<VariantGraph.Vertex> transposed = Sets.newHashSet();
        for (Match match : transposedPhrase) {
          transposed.add(witnessTokenVertices.get(match.token));
          transposed.add(match.vertex);
        }
        into.transpose(transposed);
      }
    }
    
    // returns the created vertices for each witness token
    protected Map<Token, Vertex> mergeTokens(VariantGraph into, Iterable<Token> witnessTokens, Map<Token, VariantGraph.Vertex> alignments) {
      merge(into, witnessTokens, alignments);
      return witnessTokenVertices;
    }
  }
}
