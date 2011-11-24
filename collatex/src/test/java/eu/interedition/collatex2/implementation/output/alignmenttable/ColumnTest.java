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

package eu.interedition.collatex2.implementation.output.alignmenttable;

import eu.interedition.collatex2.AbstractTest;
import eu.interedition.collatex2.interfaces.ColumnState;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IWitness;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class ColumnTest extends AbstractTest {

  @Test
  public void firstToken() {
    final IWitness witness = createWitnesses("a test string")[0];
    final IColumn c = align(witness).getColumns().get(0);

    assertTrue(c.containsWitness(witness));
    assertFalse(c.containsWitness(createWitnesses("")[0]));
    assertEquals(ColumnState.INVARIANT, c.getState());
  }

  @Test
  public void addVariant() {
    final IWitness[] w = createWitnesses("first", "second", "third");
    final IColumn c = align(w).getColumns().get(0);

    assertTrue(c.containsWitness(w[0]));
    assertTrue(c.containsWitness(w[1]));
    assertTrue(c.containsWitness(w[2]));
    assertFalse(c.containsWitness(createWitnesses("")[0]));
    assertEquals(ColumnState.VARIANT, c.getState());
  }

  @Test
  public void addMatch() {
    final IWitness[] w = createWitnesses("match", "match");
    final IColumn c = align(w).getColumns().get(0);

    assertTrue(c.containsWitness(w[0]));
    assertTrue(c.containsWitness(w[1]));
    assertFalse(c.containsWitness(createWitnesses("")[0]));
    assertEquals(ColumnState.INVARIANT, c.getState());
  }
  
  @Test
  public void mixedColumn() {
    final IWitness[] w = createWitnesses("match", "match", "variant");
    final IColumn c = align(w).getColumns().get(0);

    assertTrue(c.containsWitness(w[0]));
    assertTrue(c.containsWitness(w[1]));
    assertTrue(c.containsWitness(w[2]));
    assertFalse(c.containsWitness(createWitnesses("")[0]));
    assertEquals(ColumnState.VARIANT, c.getState());
  }

  @Test(expected = NoSuchElementException.class)
  public void getNonExistingWordGivesException() {
    align("a test string").getColumns().get(0).getToken(createWitnesses("")[0]);
  }
}
