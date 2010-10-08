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

package eu.interedition.collatex.input;

import junit.framework.Assert;

import org.junit.Test;

import eu.interedition.collatex.input.builders.WitnessBuilder;
import eu.interedition.collatex.input.visitors.JSONObjectVisitor;

public class JsonOutputTest {
  @Test
  public void testJsonObjectVisitor() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness w1 = builder.build("id", "a b c d");
    JSONObjectVisitor visitor = new JSONObjectVisitor();
    w1.accept(visitor);
    String expected = "[{\"ID\":\"id\",\"tokens\":[{\"token\":\"a\"},{\"token\":\"b\"},{\"token\":\"c\"},{\"token\":\"d\"}]}]";
    String result = visitor.getJsonArray().toString();
    Assert.assertEquals(expected, result);
  }
}
