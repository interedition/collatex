package com.sd_editions.collatex.Collate;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;

public class TextAlignmentTest extends TestCase {
  public void testAlignment() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    Table table = alignmentTable("cat", "cat");
    assertEquals("identical: cat", table.get(1, 2).toString());
  }

  public void testAlignmentVariant() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    Table table = alignmentTable("cat", "mat");
    assertEquals("variant-align: cat / mat", table.get(1, 2).toString());
  }

  public void testReplacement() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    Table table = alignmentTable("cat", "boat");
    assertEquals("replacement: cat / boat", table.get(1, 2).toString());
  }

  public void testCapital() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    Table table = alignmentTable("the", "The");
    assertEquals("identical: the", table.get(1, 2).toString());
  }

  public void testSentence() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    Table table = alignmentTable("a black cat", "a black cat");
    assertEquals("identical: a", table.get(1, 2).toString());
    assertEquals("identical: black", table.get(1, 4).toString());
    assertEquals("identical: cat", table.get(1, 6).toString());
  }

  public void testPunctuation() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    Table table = alignmentTable("the black cat", "The, black cat");
    assertEquals("identical: the", table.get(1, 2).toString());
    assertEquals("identical: black", table.get(1, 4).toString());
    assertEquals("identical: cat", table.get(1, 6).toString());
  }

  public void testVariant() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    Table table = alignmentTable("a white cat", "a black cat");
    assertEquals("identical: a", table.get(1, 2).toString());
    assertEquals("replacement: white / black", table.get(1, 4).toString());
    assertEquals("identical: cat", table.get(1, 6).toString());
  }

  public void testOmission_InTheMiddle() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    Table table = alignmentTable("a white working horse", "a horse");
    assertEquals("identical: a", table.get(1, 2).toString());
    assertEquals("omission: white", table.get(1, 4).toString());
    assertEquals("omission: working", table.get(1, 6).toString());
    assertEquals("identical: horse", table.get(1, 8).toString());
  }

  public void testOmission_AtTheStart() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    Table table = alignmentTable("an almost certain death", "certain death");
    assertEquals("omission: an", table.get(1, 2).toString());
    assertEquals("omission: almost", table.get(1, 4).toString());
    assertEquals("identical: certain", table.get(1, 6).toString());
    assertEquals("identical: death", table.get(1, 8).toString());
  }

  public void testOmission_AtTheEnd() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    Table table = alignmentTable("a calico, or tortoiseshell cat", "a calico");
    assertEquals("identical: a", table.get(1, 2).toString());
    assertEquals("identical: calico", table.get(1, 4).toString());
    assertEquals("omission: or", table.get(1, 6).toString());
    assertEquals("omission: tortoiseshell", table.get(1, 8).toString());
    assertEquals("omission: cat", table.get(1, 10).toString());
  }

  public void testAddition_InTheMiddle() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    Table table = alignmentTable("a cat", "a calico cat");
    assertEquals("identical: a", table.get(1, 2).toString());
    assertEquals("addition: calico", table.get(1, 3).toString());
    assertEquals("identical: cat", table.get(1, 4).toString());
  }

  public void testAddition_AtTheEnd() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    Table table = alignmentTable("to be", "to be lost");
    assertEquals("identical: to", table.get(1, 2).toString());
    assertEquals("identical: be", table.get(1, 4).toString());
    assertEquals("addition: lost", table.get(1, 5).toString());
  }

  public void testAddition_AtTheStart() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    Table table = alignmentTable("to be", "not to be");
    assertEquals("addition: not", table.get(1, 1).toString());
    assertEquals("identical: to", table.get(1, 2).toString());
    assertEquals("identical: be", table.get(1, 4).toString());
  }

  public void testPhraseAlignment_InTheMiddle() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    Table table = alignmentTable("i saw cloverfield yesterday", "i saw the moon get eclipsed yesterday");
    assertEquals("identical: i", table.get(1, 2).toString());
    assertEquals("identical: saw", table.get(1, 4).toString());
    assertEquals("identical: yesterday", table.get(1, 8).toString());
    assertEquals("replacement: cloverfield / the moon get eclipsed", table.get(1, 6).toString());
  }

  private Table alignmentTable(String baseString, String witnessString) throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin(baseString).readFile();
    BlockStructure variant = new StringInputPlugin(witnessString).readFile();
    WordAlignmentVisitor visitor = new WordAlignmentVisitor(variant);
    base.accept(visitor);
    Table table = new TupleToTable(base, variant, visitor.getResult()).getTable();
    return table;
  }

  public void testMultipleAdditions() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    // black cat
    // the black and white cat
    Table table = alignmentTable("black cat", "the black and white cat");
    assertEquals("addition: the", table.get(1, 1).toString());
    assertEquals("identical: black", table.get(1, 2).toString());
    assertEquals("identical: cat", table.get(1, 4).toString());
    assertEquals("addition: and white", table.get(1, 3).toString());
  }

}
