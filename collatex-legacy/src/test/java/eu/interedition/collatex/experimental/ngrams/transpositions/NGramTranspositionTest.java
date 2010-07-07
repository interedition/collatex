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

package eu.interedition.collatex.experimental.ngrams.transpositions;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import eu.interedition.collatex.experimental.interfaces.WitnessF;
import eu.interedition.collatex.experimental.ngrams.BiGramIndex;
import eu.interedition.collatex.experimental.ngrams.BiGramIndexGroup;
import eu.interedition.collatex.experimental.ngrams.NGram;
import eu.interedition.collatex2.interfaces.IWitness;

public class NGramTranspositionTest {

  @Test
  public void testTransposition1() {
    final IWitness a = WitnessF.create("A", "The black dog chases a red cat.");
    final IWitness b = WitnessF.create("B", "A red cat chases the black dog.");
    final BiGramIndex index = BiGramIndex.create(a);
    Assert.assertEquals(8, index.size());
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    // Bigrams test
    final BiGramIndex uindex = group.getUniqueBigramsForWitnessA();
    //Assert.assertEquals(4, index.size());
    // TODO I can also make a method that gives back the bigramindex
    // in normalized form as a list of strings
    // NOTE: maybe that method is already there! It is called keys!
    Assert.assertEquals("# the", uindex.get(0).getNormalized());
    Assert.assertEquals("dog chases", uindex.get(1).getNormalized());
    Assert.assertEquals("chases a", uindex.get(2).getNormalized());
    Assert.assertEquals("cat #", uindex.get(3).getNormalized());

    // NGrams test
    final List<NGram> uniqueNGramsForWitnessA = group.getUniqueNGramsForWitnessA();
    Assert.assertEquals(3, uniqueNGramsForWitnessA.size());
    Assert.assertEquals("# the", uniqueNGramsForWitnessA.get(0).getNormalized());
    Assert.assertEquals("dog chases a", uniqueNGramsForWitnessA.get(1).getNormalized());
    Assert.assertEquals("cat #", uniqueNGramsForWitnessA.get(2).getNormalized());
  }

}
