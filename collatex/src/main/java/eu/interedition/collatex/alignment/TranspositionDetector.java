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

package eu.interedition.collatex.alignment;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import eu.interedition.collatex.Tuple;
import eu.interedition.collatex.graph.VariantGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class TranspositionDetector {
  private static final Logger LOG = LoggerFactory.getLogger(TranspositionDetector.class);

  //  public List<Tuple<Tuple<List<INormalizedToken>>>> detect(List<Tuple<List<INormalizedToken>>> phraseMatches, ITokenContainer base) {
  //
  //    //    INormalizedToken currentBaseToken = NormalizedToken.START;
  //    List<Tuple<List<INormalizedToken>>> copy = Lists.newArrayList(phraseMatches);
  //
  //    List<Tuple<List<INormalizedToken>>> sortedPhraseMatches = Lists.newArrayList(); // sequences ordered by base
  //    Iterator<INormalizedToken> baseIterator = base.tokenIterator();
  //    while (baseIterator.hasNext()) {
  //      INormalizedToken baseToken = baseIterator.next();
  //
  //      Iterator<Tuple<List<INormalizedToken>>> iterator = copy.iterator();
  //      while (iterator.hasNext()) {
  //        Tuple<java.util.List<eu.interedition.collatex.interfaces.INormalizedToken>> tuple = iterator.next();
  //        List<INormalizedToken> left = tuple.left;
  //        INormalizedToken startToken = left.get(0);
  //        if (base.isNear(baseToken, startToken)) {
  //          sortedPhraseMatches.add(tuple);
  //          copy.remove(tuple);
  //          //          currentBaseToken = left.get(left.size() - 1);
  //          break;
  //        }
  //      }
  //    }
  //    if (!copy.isEmpty()) {
  //      throw new RuntimeException("copy should be empty!");
  //    }
  //
  //    final List<Tuple<Tuple<List<INormalizedToken>>>> transpositions = Lists.newArrayList();
  //    Preconditions.checkState(sortedPhraseMatches.size() == phraseMatches.size(), "Something went wrong in the linking process!");
  //    final Iterator<Tuple<List<INormalizedToken>>> unsortedIt = phraseMatches.iterator();
  //    final Iterator<Tuple<List<INormalizedToken>>> sortedIt = sortedPhraseMatches.iterator();
  //    while (unsortedIt.hasNext() && sortedIt.hasNext()) {
  //      final Tuple<List<INormalizedToken>> phraseMatchInWitness = unsortedIt.next();
  //      final Tuple<List<INormalizedToken>> phraseMatchInBase = sortedIt.next();
  //      if (!phraseMatchInWitness.equals(phraseMatchInBase)) {
  //        // TODO: I have got no idea why we have to mirror the sequences here!
  //        transpositions.add(new Tuple<Tuple<List<INormalizedToken>>>(phraseMatchInBase, phraseMatchInWitness));
  //      }
  //    }
  //    return transpositions;
  //  }

  public List<Tuple<List<Match>>> detect(List<List<Match>> phraseMatches, VariantGraph base) {
    // sort phrase matches by base token order
    final List<List<Match>> sortedPhraseMatches = Lists.newArrayList(phraseMatches);
    Collections.sort(sortedPhraseMatches, new Comparator<List<Match>>() {
      @Override
      public int compare(List<Match> o1, List<Match> o2) {
        Preconditions.checkArgument(!o1.isEmpty());
        Preconditions.checkArgument(!o2.isEmpty());
        return o1.get(0).vertex.getRank() - o2.get(0).vertex.getRank();
      }
    });

    // compare sorted to unsorted phrase matches in order to yield transpositions
    final List<Tuple<List<Match>>> transpositions = Lists.newArrayList();
    final Iterator<List<Match>> unsortedIt = phraseMatches.iterator();
    final Iterator<List<Match>> sortedIt = sortedPhraseMatches.iterator();
    while (unsortedIt.hasNext() && sortedIt.hasNext()) {
      final List<Match> phraseMatchInWitness = unsortedIt.next();
      final List<Match> phraseMatchInBase = sortedIt.next();
      if (!phraseMatchInWitness.equals(phraseMatchInBase)) {
        // TODO: I have got no idea why we have to mirror the sequences here!
        transpositions.add(new Tuple<List<Match>>(phraseMatchInBase, phraseMatchInWitness));
      }
    }

    if (LOG.isTraceEnabled()) {
      for (Tuple<List<Match>> transposition : transpositions) {
        LOG.trace("Detected transposition: {} <==> {}", Iterables.toString(transposition.left), Iterables.toString(transposition.right));
      }
    }
    return transpositions;
  }
}
