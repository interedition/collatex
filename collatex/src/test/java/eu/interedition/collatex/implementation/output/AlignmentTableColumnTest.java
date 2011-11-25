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

package eu.interedition.collatex.implementation.output;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.interfaces.ColumnState;
import eu.interedition.collatex.interfaces.IWitness;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class AlignmentTableColumnTest extends AbstractTest {

  @Test
  public void firstToken() {
    final IWitness[] w = createWitnesses("a test string", "");
    final Column c = align(w[0]).getColumns().get(0);

    assertTrue(c.containsWitness(w[0]));
    assertFalse(c.containsWitness(w[1]));
    assertEquals(ColumnState.INVARIANT, c.getState());
  }

  @Test
  public void addVariant() {
    final IWitness[] w = createWitnesses("first", "second", "third", "");
    final Column c = align(w[0], w[1], w[2]).getColumns().get(0);

    assertTrue(c.containsWitness(w[0]));
    assertTrue(c.containsWitness(w[1]));
    assertTrue(c.containsWitness(w[2]));
    assertFalse(c.containsWitness(w[3]));
    assertEquals(ColumnState.VARIANT, c.getState());
  }

  @Test
  public void addMatch() {
    final IWitness[] w = createWitnesses("match", "match", "");
    final Column c = align(w[0], w[1]).getColumns().get(0);

    assertTrue(c.containsWitness(w[0]));
    assertTrue(c.containsWitness(w[1]));
    assertFalse(c.containsWitness(w[2]));
    assertEquals(ColumnState.INVARIANT, c.getState());
  }
  
  @Test
  public void mixedColumn() {
    final IWitness[] w = createWitnesses("match", "match", "variant", "");
    final Column c = align(w[0], w[1], w[2]).getColumns().get(0);

    assertTrue(c.containsWitness(w[0]));
    assertTrue(c.containsWitness(w[1]));
    assertTrue(c.containsWitness(w[2]));
    assertFalse(c.containsWitness(w[3]));
    assertEquals(ColumnState.VARIANT, c.getState());
  }

  @Test(expected = NoSuchElementException.class)
  public void getNonExistingWordGivesException() {
    final IWitness[] w = createWitnesses("a test string", "");
    align(w[0]).getColumns().get(0).getToken(w[1]);
  }
}
