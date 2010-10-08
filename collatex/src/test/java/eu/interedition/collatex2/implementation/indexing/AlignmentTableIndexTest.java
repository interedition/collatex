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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAligner;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ICallback;
import eu.interedition.collatex2.interfaces.ITokenIndex;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.legacy.tokencontainers.AlignmentTableIndex;

public class AlignmentTableIndexTest {
  protected static final Logger LOG = LoggerFactory.getLogger(AlignmentTableIndexTest.class);
  private CollateXEngine factory;

  @Before
  public void setup() {
    factory = new CollateXEngine();
  }

  private IAligner createAligner() {
    IAligner aligner = factory.createAligner();
    aligner.setCallback(new ICallback() {
      @Override
      public void alignment(final IAlignment alignment) {
        LOG.debug(alignment.getMatches().toString());
      }
    });
    return aligner;
  }

  @Test
  public void test() {
    final IWitness witnessA = factory.createWitness("A", "the big black cat and the big black rat");
    final IAlignmentTable table = createAligner().add(witnessA).getResult();
    final ITokenIndex index = AlignmentTableIndex.create(table, table.getRepeatedTokens());
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

  @Test
  public void testCreateAlignmentTableIndex() {
    final IWitness a = factory.createWitness("A", "the first witness");
    final IAlignmentTable table = factory.align(a);
    final ITokenIndex index = AlignmentTableIndex.create(table, table.getRepeatedTokens());
    // test unigrams
    assertTrue(index.contains("the"));
    assertTrue(index.contains("first"));
    assertTrue(index.contains("witness"));
  }

  @Test
  public void testCreateAlignmentTableIndexWithVariation() {
    final IWitness a = factory.createWitness("A", "the first witness");
    final IWitness b = factory.createWitness("B", "the second witness");
    final IAlignmentTable table = factory.align(a, b);
    final ITokenIndex index = AlignmentTableIndex.create(table, table.getRepeatedTokens());
    // test unigrams
    assertTrue(index.contains("the"));
    assertTrue(index.contains("first"));
    assertTrue(index.contains("second"));
    assertTrue(index.contains("witness"));
  }

  @Test
  public void testAlignmentTableIndex() {
    final IWitness a = factory.createWitness("A", "first");
    final IWitness b = factory.createWitness("B", "second");
    final IWitness c = factory.createWitness("C", "third");
    final IAlignmentTable table = factory.align(a, b, c);
    final ITokenIndex index = AlignmentTableIndex.create(table, table.getRepeatedTokens());
    assertTrue(index.contains("# first"));
    assertTrue(index.contains("first #"));
    assertTrue(index.contains("# second"));
    assertTrue(index.contains("second #"));
    assertTrue(index.contains("# third"));
    assertTrue(index.contains("third #"));
  }

  @Test
  public void testAlignmentTableIndex2() {
    final IWitness a = factory.createWitness("A", "first");
    final IWitness b = factory.createWitness("B", "match");
    final IWitness c = factory.createWitness("C", "match");
    final IAlignmentTable table = factory.align(a, b, c);
    final ITokenIndex index = AlignmentTableIndex.create(table, table.getRepeatedTokens());
    assertTrue(index.contains("# first"));
    assertTrue(index.contains("first #"));
    assertTrue(index.contains("# match"));
    assertTrue(index.contains("match #"));
  }

}
