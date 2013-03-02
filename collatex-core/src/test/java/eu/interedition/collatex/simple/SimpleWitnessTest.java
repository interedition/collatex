/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.simple;

import eu.interedition.collatex.AbstractTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleWitnessTest extends AbstractTest {

  @Test
  public void normalize() {
    assertNormalized("Hello", "hello");
    assertNormalized("Now!", "now");
    assertNormalized("later?", "later");
    assertNormalized("#$@!?", "#$@!?");
    assertNormalized("&", "&");
  }

  private static void assertNormalized(String content, String expected) {
    assertEquals(expected, SimpleWitness.TOKEN_NORMALIZER.apply(content));
  }

}
