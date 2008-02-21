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
    Tuple[] expected = { new Tuple(1, 1) };
    assertResultIsExpected(base, witness, expected);
  }

//  public void testAlignmentPhase_b() throws FileNotFoundException, IOException, BlockStructureCascadeException {
//    final String base = "a black cat";
//    final String witness = "a white cat";
//    Tuple[] expected = { new Tuple(1, 1), new Tuple(3, 3) };
//    assertResultIsExpected(base, witness, expected);
//  }
//
//  public void testAlignmentPhase_c() throws FileNotFoundException, IOException, BlockStructureCascadeException {
//    final String base = "a black cat";
//    final String witness = "on a white mat";
//    Tuple[] expected = { new Tuple(1, 2), new Tuple(3, 4) };
//    assertResultIsExpected(base, witness, expected);
//  }

  private Tuple[] phase1Table(String baseString, String witnessString) throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin(baseString).readFile();
    BlockStructure witness = new StringInputPlugin(witnessString).readFile();
    WordAlignmentVisitor visitor = new WordAlignmentVisitor(witness);
    base.accept(visitor);
    return visitor.getResult();
  }

  private void assertResultIsExpected(final String base, final String witness, Tuple[] expected) throws FileNotFoundException, IOException, BlockStructureCascadeException {
    assertEquals(resultAsString(expected), resultAsString(phase1Table(base, witness)));
  }

  private String resultAsString(Tuple[] array) {
    StringBuffer result = new StringBuffer("[");
    for (int i = 0; i < array.length; i++) {
      result.append(array[i].toString());
    }
    result.append("]");
    return result.toString();
  }

}
