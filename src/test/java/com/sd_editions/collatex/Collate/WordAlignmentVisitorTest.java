package com.sd_editions.collatex.Collate;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

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

  private String resultAsString(Tuple[] array) {
    StringBuffer result = new StringBuffer("[");
    String join = "";
    for (int i = 0; i < array.length; i++) {
      result.append(join + array[i].toString());
      join = ",";
    }
    result.append("]");
    return result.toString();
  }

}
