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

package eu.interedition.collatex.experimental.ngrams;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public class NGramIndex {

  //TODO rename to create?
  //TODO make return type a NGramIndex
  static List<NGram> concatenateBiGramToNGram(final BiGramIndex biGramIndex) {
    final List<BiGram> biGrams = Lists.newArrayList(biGramIndex);
    if (biGrams.isEmpty()) {
      return Collections.emptyList();
    }
    final List<NGram> newNGrams = Lists.newArrayList();
    NGram currentNGram = NGram.create(biGrams.remove(0));
    newNGrams.add(currentNGram);
    for (final BiGram nextBiGram : biGrams) {
      if (nextBiGram.getFirstToken().getPosition() - currentNGram.getLastToken().getPosition() > 1) {
        currentNGram = NGram.create(nextBiGram);
        newNGrams.add(currentNGram);
      } else {
        currentNGram.add(nextBiGram);
      }
    }
    return newNGrams;
  }
}
