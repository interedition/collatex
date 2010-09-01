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

package eu.interedition.collatex2.implementation.indexing;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public class WitnessIndexTest {
  private static final Logger LOG = LoggerFactory.getLogger(WitnessIndexTest.class);
  private CollateXEngine factory;

  @Before
  public void setup() {
    factory = new CollateXEngine();
  }
  
  @Test
  public void testBigrams1() {
    final IWitness witnessA = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitnessIndex index = new WitnessIndex(witnessA, witnessA.findRepeatingTokens());
    // test bigrams
    assertTrue(index.contains("# the"));
    assertFalse(index.contains("big black")); 
    assertTrue(index.contains("black cat"));
    assertTrue(index.contains("cat and"));
    assertTrue(index.contains("and the"));
    assertTrue(index.contains("black rat"));
    assertTrue(index.contains("rat #"));
    // test trigrams
    assertTrue(index.contains("# the big"));
    assertFalse(index.contains("the big black"));
    assertTrue(index.contains("big black cat"));
    assertTrue(index.contains("black cat and"));
    assertTrue(index.contains("cat and the"));
    assertTrue(index.contains("and the big"));
    assertTrue(index.contains("big black rat"));
    assertTrue(index.contains("black rat #"));
  }


  @Ignore
  @Test
  public void test2() {
    final IWitness witnessA = factory.createWitness("A", "the big black");
    final IWitnessIndex index = new WitnessIndex(witnessA, Lists.newArrayList("the", "big", "black"));
    LOG.debug(index.getPhrases().toString());
    assertEquals(6, index.size());
    assertTrue(index.contains("# the"));
    assertTrue(index.contains("# the big"));
    assertTrue(index.contains("# the big black"));
    assertTrue(index.contains("the big black #"));
    assertTrue(index.contains("big black #"));
    assertTrue(index.contains("black #"));
    assertEquals(6, index.size());
  }

  @Ignore
  @Test
  public void test1() {
    final IWitness a = factory.createWitness("A", "tobe or not tobe");
    final IWitnessIndex index = CollateXEngine.createWitnessIndex(a);
    assertEquals(6, index.size());
    assertTrue(index.contains("# tobe"));
    assertTrue(index.contains("tobe or"));
    assertTrue(index.contains("or"));
    assertTrue(!index.contains("or not"));
    assertTrue(index.contains("not"));
    assertTrue(!index.contains("or tobe"));
    assertTrue(index.contains("not tobe"));
    assertTrue(index.contains("tobe #"));
  }

}
