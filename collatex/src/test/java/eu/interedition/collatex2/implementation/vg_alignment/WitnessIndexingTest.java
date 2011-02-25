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

package eu.interedition.collatex2.implementation.vg_alignment;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.containers.witness.WitnessIndex;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.nonpublic.alignment.ITokenIndex;

public class WitnessIndexingTest {
  private CollateXEngine factory;
  private Logger log = LoggerFactory.getLogger(WitnessIndexingTest.class);
  
  @Before
  public void setup() {
    factory = new CollateXEngine();
  }

  @Test
  public void test() {
    final IWitness witness = new CollateXEngine().createWitness("a", "a b a d b f a");
    final List<String> repeatedTokens = TokenIndexUtil.getRepeatedTokens(witness);
    assertEquals(2, repeatedTokens.size());
    assertTrue(repeatedTokens.contains("a"));
    assertTrue(repeatedTokens.contains("b"));
    assertFalse(repeatedTokens.contains("d"));
    assertFalse(repeatedTokens.contains("f"));
  }

  @Test
  public void test1a() {
    final IWitness a = factory.createWitness("A", "tobe or not tobe");
    final List<String> repeatedTokens = TokenIndexUtil.getRepeatedTokens(a);
    final ITokenIndex index = new WitnessIndex(a, repeatedTokens);
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

  @Test
  public void test2() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    log.debug("witness = [the big black cat and the big black rat]");
    final List<String> repeatedTokens = TokenIndexUtil.getRepeatedTokens(a);
    final ITokenIndex index = new WitnessIndex(a, repeatedTokens);
    assertTrue(index.contains("# the"));
    assertTrue(index.contains("the big black cat"));
    assertTrue(index.contains("# the big"));
    assertTrue(index.contains("big black cat"));
    assertTrue(index.contains("# the big black"));
    assertTrue(index.contains("black cat"));
    assertTrue(index.contains("cat"));
    assertTrue(index.contains("and"));
    assertTrue(index.contains("and the"));
    assertTrue(index.contains("the big black rat"));
    assertTrue(index.contains("and the big"));
    assertTrue(index.contains("big black rat"));
    assertTrue(index.contains("and the big black")); 
    assertTrue(index.contains("black rat"));
    assertTrue(index.contains("rat"));
    assertEquals(15, index.size());
  }

  //TODO: there is no way that these expectations could be correct!
  @Ignore
  @Test
  public void testTwoWitnesses() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitness b = factory.createWitness("B", "and the big black cat ate the big rat");
    log.debug("witness a = [the big black cat and the big black rat]");
    log.debug("witness b = [and the big black cat ate the big rat]");
    final List<String> repeatedTokens = TokenIndexUtil.getRepeatedTokens(a);
    repeatedTokens.addAll(TokenIndexUtil.getRepeatedTokens(b));
    final ITokenIndex indexA = new WitnessIndex(a, repeatedTokens);
    final ITokenIndex indexB = new WitnessIndex(b, repeatedTokens);
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

  private void assertContains(final ITokenIndex index, final String phrase) {
    assertTrue("phrase '" + phrase + "' not found in index [" + Joiner.on(", ").join(index.keys()) + "]", index.contains(phrase));
  }

  private void assertDoesNotContain(final ITokenIndex index, final String phrase) {
    assertFalse("phrase '" + phrase + "' found in index " + index.keys().iterator().next() + ", shouldn't be there!", index.contains(phrase));
  }
}
