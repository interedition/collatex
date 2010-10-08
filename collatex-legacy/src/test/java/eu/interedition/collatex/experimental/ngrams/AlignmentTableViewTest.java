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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.interedition.collatex.experimental.interfaces.WitnessF;
import eu.interedition.collatex.experimental.ngrams.alignment.Alignment;
import eu.interedition.collatex.experimental.ngrams.table.AlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableViewTest {
  @Test
  public void testSimple() {
    final String w1 = "a b";
    final String w2 = "a b";
    final String expected = "<xml>a b</xml>";
    assertEquals(expected, getGoing(w1, w2));
  }

  @Test
  public void testAddition() {
    final String w1 = "a b";
    final String w2 = "a c b";
    final String expected = "<xml>a <app>c</app> b</xml>";
    assertEquals(expected, getGoing(w1, w2));
  }

  @Test
  public void testOmmission() {
    final String w1 = "a c b";
    final String w2 = "a b";
    final String expected = "<xml>a <app>c</app> b</xml>";
    assertEquals(expected, getGoing(w1, w2));
  }

  @Test
  public void testReplacement() {
    final String w1 = "a c b";
    final String w2 = "a d b";
    final String expected = "<xml>a <app><lemma>c</lemma><reading>d</reading></app> b</xml>";
    assertEquals(expected, getGoing(w1, w2));
  }

  public String getGoing(final String w1, final String w2) {
    // TODO make a constructor for Witness, Witness
    //    Witness base = new Witness(w1);
    //    Witness witness = new Witness(w2);
    //WitnessBuilder builder = new WitnessBuilder();
    //CollateCore core = new CollateCore(builder.build(w1), builder.build(w2));
    //Modifications modifications = core.compareWitness(1, 2);
    final IWitness a = WitnessF.create("A", w1);
    final IWitness b = WitnessF.create("B", w2);
    final Alignment alignment = Alignment.create(a, b);
    final AlignmentTable table = new AlignmentTable(alignment);
    final String xml = table.toXML();
    return xml;
  }
}
