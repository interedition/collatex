package com.sd_editions.collatex.Collate;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.Block.IntBlockVisitor;
import com.sd_editions.collatex.Block.Line;
import com.sd_editions.collatex.Block.Word;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;

public class TextAlignmentVisitorTest extends TestCase {
  public void testAlignment() {
    Word base = new Word("cat");
    Word variant = new Word("cat");
    IntBlockVisitor visitor = new TextAlignmentVisitor(variant);
    visitor.visitWord(base);
    assertEquals(base, variant.getAlignedWord());
  }

  public void testNonAlignment() {
    Word base = new Word("cat");
    Word variant = new Word("boat");
    IntBlockVisitor visitor = new TextAlignmentVisitor(variant);
    visitor.visitWord(base);
    assertNull(variant.getAlignedWord());
  }

  public void testCapital() {
    Word base = new Word("the");
    Word variant = new Word("The");
    IntBlockVisitor visitor = new TextAlignmentVisitor(variant);
    visitor.visitWord(base);
    assertEquals(base, variant.getAlignedWord());
  }

  public void testSentence() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a black cat").readFile();
    BlockStructure variant = new StringInputPlugin("a black cat").readFile();
    IntBlockVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    Word baseWord1 = (Word) base.getRootBlock().getFirstChild();
    Line line = (Line) variant.getRootBlock();
    Word firstWord = (Word) line.getFirstChild();
    Word secondWord = (Word) firstWord.getNextSibling();
    Word baseWord2 = (Word) baseWord1.getNextSibling();
    assertEquals(baseWord1, firstWord.getAlignedWord());
    assertEquals(baseWord2, secondWord.getAlignedWord());
  }

  public void testPunctuation() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("the black cat").readFile();
    BlockStructure variant = new StringInputPlugin("The, black cat").readFile();
    IntBlockVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    Word baseWord1 = (Word) base.getRootBlock().getFirstChild();
    Line line = (Line) variant.getRootBlock();
    Word firstWord = (Word) line.getFirstChild();
    assertEquals(baseWord1, firstWord.getAlignedWord());
  }

  public void testVariant() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a white cat").readFile();
    BlockStructure variant = new StringInputPlugin("a black cat").readFile();
    base.accept(new TextAlignmentVisitor(variant));
    Line baseLine1 = (Line) base.getRootBlock();
    Line variantLine1 = (Line) variant.getRootBlock();
    Word baseWord1 = (Word) baseLine1.getFirstChild();
    Word baseWord2 = (Word) baseWord1.getNextSibling();
    Word baseWord3 = (Word) baseLine1.getLastChild();
    Word variantWord1 = (Word) variantLine1.getFirstChild();
    Word variantWord2 = (Word) variantWord1.getNextSibling();
    Word variantWord3 = (Word) variantLine1.getLastChild();
    assertEquals(baseWord1, variantWord1.getAlignedWord());
    assertEquals(baseWord2, variantWord2.getAlignedWord());
    assertEquals(baseWord3, variantWord3.getAlignedWord());
  }

  public void testOmission_InTheMiddle() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a white horse").readFile();
    BlockStructure variant = new StringInputPlugin("a horse").readFile();
    base.accept(new TextAlignmentVisitor(variant));
    Line baseLine1 = (Line) base.getRootBlock();
    Line variantLine1 = (Line) variant.getRootBlock();
    Word baseWord1 = (Word) baseLine1.getFirstChild();
    Word baseWord3 = (Word) baseLine1.getLastChild();
    Word variantWord1 = (Word) variantLine1.getFirstChild();
    Word variantWord2 = (Word) variantWord1.getNextSibling();
    assertEquals(baseWord1, variantWord1.getAlignedWord());
    assertEquals(baseWord3, variantWord2.getAlignedWord());
  }

  public void testOmission_AtTheStart() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a certain death").readFile();
    BlockStructure variant = new StringInputPlugin("certain death").readFile();
    base.accept(new TextAlignmentVisitor(variant));
    Line baseLine1 = (Line) base.getRootBlock();
    Line variantLine1 = (Line) variant.getRootBlock();
    Word baseWord1 = (Word) baseLine1.getFirstChild();
    Word baseWord2 = (Word) baseWord1.getNextSibling();
    Word baseWord3 = (Word) baseLine1.getLastChild();
    Word variantWord1 = (Word) variantLine1.getFirstChild();
    Word variantWord2 = (Word) variantWord1.getNextSibling();
    assertEquals(baseWord2, variantWord1.getAlignedWord());
    assertEquals(baseWord3, variantWord2.getAlignedWord());
  }

    public void testOmission_AtTheEnd() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a calico cat").readFile();
    BlockStructure variant = new StringInputPlugin("a calico").readFile();
    base.accept(new TextAlignmentVisitor(variant));
    Line baseLine1 = (Line) base.getRootBlock();
    Line variantLine1 = (Line) variant.getRootBlock();
    Word baseWord1 = (Word) baseLine1.getFirstChild();
    Word baseWord2 = (Word) baseWord1.getNextSibling();
    Word variantWord1 = (Word) variantLine1.getFirstChild();
    Word variantWord2 = (Word) variantWord1.getNextSibling();
    assertEquals(baseWord1, variantWord1.getAlignedWord());
    assertEquals(baseWord2, variantWord2.getAlignedWord());
  }

  public void testAddition_InTheMiddle() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a cat").readFile();
    BlockStructure variant = new StringInputPlugin("a calico cat").readFile();
    base.accept(new TextAlignmentVisitor(variant));
    Line baseLine1 = (Line) base.getRootBlock();
    Line variantLine1 = (Line) variant.getRootBlock();
    Word baseWord1 = (Word) baseLine1.getFirstChild();
    Word baseWord2 = (Word) baseWord1.getNextSibling();
    Word variantWord1 = (Word) variantLine1.getFirstChild();
    Word variantWord2 = (Word) variantWord1.getNextSibling();
    Word variantWord3 = (Word) variantLine1.getLastChild();
    assertEquals(baseWord1, variantWord1.getAlignedWord());
    assertNull(variantWord2.getAlignedWord());
    assertEquals(baseWord2, variantWord3.getAlignedWord());
  }

  public void testAddition_AtTheEnd() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("to be").readFile();
    BlockStructure variant = new StringInputPlugin("to be lost").readFile();
    base.accept(new TextAlignmentVisitor(variant));
    Line baseLine1 = (Line) base.getRootBlock();
    Line variantLine1 = (Line) variant.getRootBlock();
    Word baseWord1 = (Word) baseLine1.getFirstChild();
    Word baseWord2 = (Word) baseWord1.getNextSibling();
    Word variantWord1 = (Word) variantLine1.getFirstChild();
    Word variantWord2 = (Word) variantWord1.getNextSibling();
    Word variantWord3 = (Word) variantLine1.getLastChild();
    assertEquals(baseWord1, variantWord1.getAlignedWord());
    assertEquals(baseWord2, variantWord2.getAlignedWord());
    assertNull(variantWord3.getAlignedWord());
  }

//  public void testAddition_AtTheStart() throws FileNotFoundException, IOException, BlockStructureCascadeException {
//    BlockStructure base = new StringInputPlugin("to be").readFile();
//    BlockStructure variant = new StringInputPlugin("not to be").readFile();
//    base.accept(new TextAlignmentVisitor(variant));
//    Line baseLine1 = (Line) base.getRootBlock();
//    Line variantLine1 = (Line) variant.getRootBlock();
//    Word baseWord1 = (Word) baseLine1.getFirstChild();
//    Word baseWord2 = (Word) baseWord1.getNextSibling();
//    Word variantWord1 = (Word) variantLine1.getFirstChild();
//    Word variantWord2 = (Word) variantWord1.getNextSibling();
//    Word variantWord3 = (Word) variantLine1.getLastChild();
//    assertNull(variantWord1.getAlignedWord());
//    assertEquals(baseWord1, variantWord2.getAlignedWord());
//    assertEquals(baseWord2, variantWord3.getAlignedWord());
//  }

  // public void testAddition() throws FileNotFoundException, IOException,
  // BlockStructureCascadeException {
  // BlockStructure base = new StringInputPlugin("a cat").readFile();
  // BlockStructure variant = new StringInputPlugin("a black cat").readFile();
  // base.accept(new TextAlignmentVisitor(variant));
  // Line baseLine1 = (Line) base.getRootBlock();
  // Line variantLine1 = (Line) variant.getRootBlock();
  // Word baseWord1 = (Word) baseLine1.getFirstChild();
  // Word variantWord1 = (Word) variantLine1.getFirstChild();
  // Word variantWord2 = (Word) variantWord1.getNextSibling();
  // Word baseWord2 = (Word) baseLine1.getLastChild();
  // Word variantWord3 = (Word) variantLine1.getLastChild();
  // assertEquals(baseWord1, variantWord1.getAlignedWord());
  // assertNull(variantWord2.getAlignedWord());
  // assertEquals(baseWord2, variantWord3.getAlignedWord());
  // }

}
