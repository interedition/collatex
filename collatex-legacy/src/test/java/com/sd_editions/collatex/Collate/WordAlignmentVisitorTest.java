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

package com.sd_editions.collatex.Collate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;

public class WordAlignmentVisitorTest extends TestCase {

  public void testAlignmentPhase_a() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    final String base = "cat";
    final String witness = "cat";
    assertResultIsExpected(base, witness, "[[1,1]]");
  }

  public void testAlignmentPhase_b() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    final String base = "a black cat";
    final String witness = "a white cat";
    assertResultIsExpected(base, witness, "[[1,1],[3,3]]");
  }

  public void testAlignmentPhase_c() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    final String base = "a black cat";
    final String witness = "on a white mat";
    assertResultIsExpected(base, witness, "[[1,2],[3,4]]");
  }

  public void testAlignmentPhaseWithMultipleWitneses() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    final String base = "a black cat";
    final String[] witnessArray = new String[] { "on a white mat", "on a black mat", "a small black cat" };
    assertResultIsExpected(base, witnessArray, "[[[1,2],[3,4]],[[1,2],[2,3],[3,4]],[[1,1],[2,3],[3,4]]]");
  }

  //  public void testAlignmentPhase_join() throws FileNotFoundException, IOException, BlockStructureCascadeException {
  //    final String base = "a full blood cat";
  //    final String witness = "a fullblood cat";
  //    assertResultIsExpected(base, witness, "[[1,1],[2,2],[3,2][4,3]]");
  //  }
  //
  //  public void testAlignmentPhase_division() throws FileNotFoundException, IOException, BlockStructureCascadeException {
  //    final String base = "a fullblood cat";
  //    final String witness = "a full blood cat";
  //    assertResultIsExpected(base, witness, "[[1,1],[2,2],[2,3][3,4]]");
  //  }

  //  public void testAlignmentPhrase_variant() throws FileNotFoundException, IOException, BlockStructureCascadeException {
  //    final String base = "a cat and dog";
  //    final String[] witnessArray = new String[] { "a cat or dog and" };
  //    assertResultIsExpected(base, witnessArray, "[[[1,1],[2,2],[3,5],[4,4]]]");
  //  }
  //  
  //  public void testAlignmentPhrase_variant2() throws FileNotFoundException, IOException, BlockStructureCascadeException {
  //    final String base = "the drought of march hath perced to the root";
  //    final String[] witnessArray = new String[] { "the march of drought hath perced to the root" };
  //    assertResultIsExpected(base, witnessArray, "[[[1,1],[2,4],[3,3],[4,2],[5,5],[6,6],[7,7],[8,8],[9,9]]]");
  //  }

  private Tuple[] phase1Table(String baseString, String witnessString) throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin(baseString).readFile();
    BlockStructure witness = new StringInputPlugin(witnessString).readFile();
    WordAlignmentVisitor visitor = new WordAlignmentVisitor(witness);
    base.accept(visitor);
    return visitor.getResult();
  }

  private void assertResultIsExpected(final String base, final String witness, String expected) throws FileNotFoundException, IOException, BlockStructureCascadeException {
    assertEquals(expected, resultAsString(phase1Table(base, witness)));
  }

  private void assertResultIsExpected(final String base, final String[] witness, String expected) throws FileNotFoundException, IOException, BlockStructureCascadeException {
    assertEquals(expected, resultAsString(phase1Table(base, witness)));
  }

  private Tuple[][] phase1Table(String baseString, String[] witnessArray) throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin(baseString).readFile();
    List<Tuple[]> resultList = Lists.newArrayList();
    for (String witnessString : witnessArray) {
      BlockStructure witness = new StringInputPlugin(witnessString).readFile();
      WordAlignmentVisitor visitor = new WordAlignmentVisitor(witness);
      base.accept(visitor);
      resultList.add(visitor.getResult());
    }
    return resultList.toArray(new Tuple[][] {});
  }

  private String resultAsString(Tuple[] array) {
    StringBuffer result = new StringBuffer("[");
    String join = "";
    for (Tuple element : array) {
      result.append(join + element.toString());
      join = ",";
    }
    result.append("]");
    return result.toString();
  }

  private String resultAsString(Tuple[][] array) {
    StringBuffer result = new StringBuffer("[");
    String join = "";
    for (Tuple[] element : array) {
      result.append(join + resultAsString(element));
      join = ",";
    }
    result.append("]");
    return result.toString();
  }

}
