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

package eu.interedition.collatex.implementation.alignment;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.implementation.Tuple;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.ITokenContainer;


public class TranspositionDetector {

  public List<Tuple<Tuple<List<INormalizedToken>>>> detect(List<Tuple<List<INormalizedToken>>> phraseMatches, ITokenContainer base) {
    // sort phrase matches by base token order
    final Map<INormalizedToken, Tuple<List<INormalizedToken>>> tokenIndex = Maps.uniqueIndex(phraseMatches, new Function<Tuple<List<INormalizedToken>>, INormalizedToken>() {
      @Override
      public INormalizedToken apply(Tuple<List<INormalizedToken>> input) {
        return input.left.get(0);
      }
    });
    final List<Tuple<List<INormalizedToken>>> sortedPhraseMatches = Lists.newArrayList();
    for (Iterator<INormalizedToken> tokenIterator = base.tokenIterator(); tokenIterator.hasNext(); ) {
      final INormalizedToken token = tokenIterator.next();
      if (tokenIndex.containsKey(token)) {
        sortedPhraseMatches.add(tokenIndex.get(token));
      }
    }

    // compare sorted to unsorted phrase matches in order to yield transpositions
    final List<Tuple<Tuple<List<INormalizedToken>>>> transpositions = Lists.newArrayList();

    Preconditions.checkState(sortedPhraseMatches.size() == phraseMatches.size(), "Something went wrong in the linking process!");
    final Iterator<Tuple<List<INormalizedToken>>> unsortedIt = phraseMatches.iterator();
    final Iterator<Tuple<List<INormalizedToken>>> sortedIt = sortedPhraseMatches.iterator();
    while (unsortedIt.hasNext() && sortedIt.hasNext()) {
      final Tuple<List<INormalizedToken>> phraseMatchInWitness = unsortedIt.next();
      final Tuple<List<INormalizedToken>> phraseMatchInBase = sortedIt.next();
      if (!phraseMatchInWitness.equals(phraseMatchInBase)) {
        // TODO: I have got no idea why have to mirror the sequences here!
        transpositions.add(new Tuple<Tuple<List<INormalizedToken>>>(phraseMatchInBase, phraseMatchInWitness));
      }
    }

    return transpositions;
  }

}
