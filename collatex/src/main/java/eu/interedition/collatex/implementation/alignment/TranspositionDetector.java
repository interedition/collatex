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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.collatex.implementation.Tuple;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IWitness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

  public List<Tuple<Tuple<List<INormalizedToken>>>> detect(List<Tuple<List<INormalizedToken>>> phraseMatches, IWitness base) {
    // sort phrase matches by base token order
    final Map<INormalizedToken, Tuple<List<INormalizedToken>>> tokenIndex = Maps.uniqueIndex(phraseMatches, new Function<Tuple<List<INormalizedToken>>, INormalizedToken>() {
      @Override
      public INormalizedToken apply(Tuple<List<INormalizedToken>> input) {
        return input.left.get(0);
      }
    });
    final List<Tuple<List<INormalizedToken>>> sortedPhraseMatches = Lists.newArrayList();
    for (INormalizedToken token : base.getTokens()) {
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
        // TODO: I have got no idea why we have to mirror the sequences here!
        transpositions.add(new Tuple<Tuple<List<INormalizedToken>>>(phraseMatchInBase, phraseMatchInWitness));
      }
    }

    if (LOG.isTraceEnabled()) {
      for (Tuple<Tuple<List<INormalizedToken>>> transposition : transpositions) {
        LOG.trace("Detected transposition: {} <==> {}", phraseMatchToString(transposition.left), phraseMatchToString(transposition.right));
      }
    }
    return transpositions;
  }

  private Object phraseMatchToString(Tuple<List<INormalizedToken>> phraseMatch) {
    return new StringBuilder("{").append(Iterables.toString(phraseMatch.left)).append(" = ").append(Iterables.toString(phraseMatch.right)).append("}").toString();
  }

}
