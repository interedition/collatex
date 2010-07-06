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

package eu.interedition.collatex2.indexing;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public class IndexingTest {
  private CollateXEngine factory;
  private Logger log = LoggerFactory.getLogger(IndexingTest.class);
  
  @Before
  public void setup() {
    factory = new CollateXEngine();
  }

  @Ignore
  @Test
  public void test2() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    log.debug("witness = [the big black cat and the big black rat]");
    final IWitnessIndex index = CollateXEngine.createWitnessIndex(a);
    assertContains(index, "# the big black");
    assertContains(index, "the big black cat");
    assertContains(index, "cat");
    assertContains(index, "and");
    assertContains(index, "and the big black");
    assertContains(index, "the big black rat");
    assertContains(index, "rat");
    assertEquals(7, index.size());
  }

  @Test
  public void test1a() {
    final IWitness a = factory.createWitness("A", "tobe or not tobe");
    final IWitnessIndex index = CollateXEngine.createWitnessIndex(a);
    assertEquals(6, index.size());
    assertContains(index, "# tobe");
    assertContains(index, "tobe or");
    assertContains(index, "or");
    assertDoesNotContain(index, "or not");
    assertContains(index, "not");
    assertDoesNotContain(index, "or tobe");
    assertContains(index, "not tobe");
    assertContains(index, "tobe #");
  }

  @Ignore
  @Test
  public void test2a() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitnessIndex index = CollateXEngine.createWitnessIndex(a);
    assertContains(index, "# the big black");
    assertContains(index, "the big black cat");
    assertContains(index, "cat");
    assertContains(index, "and");
    assertContains(index, "and the big black");
    assertContains(index, "the big black rat");
    assertContains(index, "rat");
    assertEquals(7, index.size());
  }

  @Ignore
  @Test
  public void testTwoWitnesses() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitness b = factory.createWitness("B", "and the big black cat ate the big rat");
    log.debug("witness a = [the big black cat and the big black rat]");
    log.debug("witness b = [and the big black cat ate the big rat]");
    final IWitnessIndex indexA = CollateXEngine.createWitnessIndex(a);
    final IWitnessIndex indexB = CollateXEngine.createWitnessIndex(b);
    assertContains(indexA, "# the big black");
    assertContains(indexB, "# the big black");
    assertContains(indexA, "the big black cat");
    assertContains(indexB, "the big black cat");
    assertContains(indexA, "cat");
    assertContains(indexA, "and");
    assertContains(indexB, "and");
    assertContains(indexA, "and the big black");
    assertContains(indexA, "the big black rat");
    assertContains(indexA, "rat");
    assertEquals(7, indexA.size());
  }

  //  @Test
  //  public void test3() {
  //    final IWitness a = factory.createWitness("A", "X C A B Y C A Z A B W");
  //    Log.info("witness = [X C A B Y C A Z A B W]");
  //    final IWitnessIndex index = Factory.createWitnessIndex(a);
  //    assertContains(index,"# the big black"));
  //    assertContains(index,"the big black cat"));
  //    assertContains(index,"and"));
  //    assertContains(index,"and the big black"));
  //    assertContains(index,"the big black rat"));
  //    assertEquals(5, index.size());
  //  }

  private void assertContains(final IWitnessIndex index, final String phrase) {
    assertTrue("phrase '" + phrase + "' not found in index [" + Joiner.on(", ").join(index.getPhrases()) + "]", index.contains(phrase));
  }

  private void assertDoesNotContain(final IWitnessIndex index, final String phrase) {
    assertFalse("phrase '" + phrase + "' found in index " + index.getPhrases().iterator().next().getSigil() + ", shouldn't be there!", index.contains(phrase));
  }
}
