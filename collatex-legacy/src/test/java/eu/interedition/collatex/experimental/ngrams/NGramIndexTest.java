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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.interedition.collatex.experimental.interfaces.WitnessF;
import eu.interedition.collatex.experimental.ngrams.data.Token;
import eu.interedition.collatex2.interfaces.IWitness;

public class NGramIndexTest {

  @Test
  public void testNGramIndex() {
    final IWitness a = WitnessF.create("A", "a b c d");
    final BiGramIndex index = BiGramIndex.create(a);
    final List<NGram> ngrams = NGramIndex.concatenateBiGramToNGram(index);
    Assert.assertEquals(1, ngrams.size());
    // Note: next assert checks that the original index
    // Note: is not affected!
    Assert.assertEquals(5, index.size());
  }

  @Test
  public void testNGramIndex2() {
    final IWitness a = WitnessF.create("A", "a b c d GAP e f g");
    final BiGramIndex biGramI = BiGramIndex.create(a);
    final BiGramIndex indexWithGap = biGramI.removeBiGramsWithToken(new Token("A", "GAP", 5));
    final List<NGram> ngrams = NGramIndex.concatenateBiGramToNGram(indexWithGap);
    Assert.assertEquals(2, ngrams.size());

  }
}
