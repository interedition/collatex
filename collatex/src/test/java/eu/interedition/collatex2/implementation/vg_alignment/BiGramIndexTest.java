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

import java.util.Iterator;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.input.Token;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.legacy.indexing.BiGram;
import eu.interedition.collatex2.legacy.indexing.BiGramIndex;

public class BiGramIndexTest {
  private static CollateXEngine factory;

  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }


  @Test
  public void testCreate() {
    final IWitness a = factory.createWitness("A", "a b c d");
    final BiGramIndex index = BiGramIndex.create(a);
    Assert.assertEquals(5, index.size());
  }

  @Test
  public void testRemoveTokenFromIndex() {
    final IWitness a = factory.createWitness("A", "a b c d");
    final BiGramIndex index = BiGramIndex.create(a);
    final BiGramIndex result = index.removeBiGramsWithToken(new Token("A", "c", 3));
    Assert.assertEquals(3, result.size());
  }

  //  TODO we might want to change this behavior!
  @Test
  public void testIterable() {
    final IWitness a = factory.createWitness("A", "a b c d");
    final BiGramIndex index = BiGramIndex.create(a);
    final Iterator<BiGram> iterator = index.iterator();
    iterator.next();
    iterator.remove();
  }
}
