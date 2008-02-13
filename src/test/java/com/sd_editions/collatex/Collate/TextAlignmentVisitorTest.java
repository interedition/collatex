package com.sd_editions.collatex.Collate;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import com.sd_editions.collatex.Block.Block;
import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.Block.Line;
import com.sd_editions.collatex.Block.Word;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;

public class TextAlignmentVisitorTest extends TestCase {
  public void testAlignment() {
    Word base = new Word("cat");
    Word variant = new Word("cat");
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    visitor.visitWord(base);
    BlockStructure alignmentInformation = visitor.getResult();
    Block alignmentInfo1 = alignmentInformation.getRootBlock().getFirstChild();
    assertEquals("identical: cat", alignmentInfo1.toString());
  }

  public void testNonAlignment() {
    Word base = new Word("cat");
    Word variant = new Word("boat");
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    visitor.visitWord(base);
    BlockStructure alignmentInformation = visitor.getResult();
    Block alignmentInfo1 = alignmentInformation.getRootBlock().getFirstChild();
    assertEquals("non-alignment: cat, boat", alignmentInfo1.toString());
  }

  public void testCapital() {
    Word base = new Word("the");
    Word variant = new Word("The");
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    visitor.visitWord(base);
    BlockStructure alignmentInformation = visitor.getResult();
    Block alignmentInfo1 = alignmentInformation.getRootBlock().getFirstChild();
    assertEquals("identical: the", alignmentInfo1.toString());
  }

  public void testSentence() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a black cat").readFile();
    BlockStructure variant = new StringInputPlugin("a black cat").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Block alignmentInfo1 = alignmentInformation.getRootBlock().getFirstChild();
    Block alignmentInfo2 = alignmentInfo1.getNextSibling();
    Block alignmentInfo3 = alignmentInfo2.getNextSibling();
    assertEquals("identical: a", alignmentInfo1.toString());
    assertEquals("identical: black", alignmentInfo2.toString());
    assertEquals("identical: cat", alignmentInfo3.toString());
  }

  public void testPunctuation() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("the black cat").readFile();
    BlockStructure variant = new StringInputPlugin("The, black cat").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Block alignmentInfo1 = alignmentInformation.getRootBlock().getFirstChild();
    Block alignmentInfo2 = alignmentInfo1.getNextSibling();
    Block alignmentInfo3 = alignmentInfo2.getNextSibling();
    assertEquals("identical: the", alignmentInfo1.toString());
    assertEquals("identical: black", alignmentInfo2.toString());
    assertEquals("identical: cat", alignmentInfo3.toString());
  }

  public void testVariant() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a white cat").readFile();
    BlockStructure variant = new StringInputPlugin("a black cat").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Block alignmentInfo1 = alignmentInformation.getRootBlock().getFirstChild();
    Block alignmentInfo2 = alignmentInfo1.getNextSibling();
    Block alignmentInfo3 = alignmentInfo2.getNextSibling();
    assertEquals("identical: a", alignmentInfo1.toString());
    assertEquals("variant-align: white / black", alignmentInfo2.toString());
    assertEquals("identical: cat", alignmentInfo3.toString());
  }

  public void testOmission_InTheMiddle() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a white horse").readFile();
    BlockStructure variant = new StringInputPlugin("a horse").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Block alignmentInfo1 = alignmentInformation.getRootBlock().getFirstChild();
    Block alignmentInfo2 = alignmentInfo1.getNextSibling();
    Block alignmentInfo3 = alignmentInfo2.getNextSibling();
    assertEquals("identical: a", alignmentInfo1.toString());
    assertEquals("omission: white", alignmentInfo2.toString());
    assertEquals("identical: horse", alignmentInfo3.toString());
  }

  public void testOmission_AtTheStart() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a certain death").readFile();
    BlockStructure variant = new StringInputPlugin("certain death").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Block alignmentInfo1 = alignmentInformation.getRootBlock().getFirstChild();
    Block alignmentInfo2 = alignmentInfo1.getNextSibling();
    Block alignmentInfo3 = alignmentInfo2.getNextSibling();
    assertEquals("omission: a", alignmentInfo1.toString());
    assertEquals("identical: certain", alignmentInfo2.toString());
    assertEquals("identical: death", alignmentInfo3.toString());
  }

    public void testOmission_AtTheEnd() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a calico cat").readFile();
    BlockStructure variant = new StringInputPlugin("a calico").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Block alignmentInfo1 = alignmentInformation.getRootBlock().getFirstChild();
    Block alignmentInfo2 = alignmentInfo1.getNextSibling();
    Block alignmentInfo3 = alignmentInfo2.getNextSibling();
    assertEquals("identical: a", alignmentInfo1.toString());
    assertEquals("identical: calico", alignmentInfo2.toString());
    assertEquals("omission: cat", alignmentInfo3.toString());
  }

  public void testAddition_InTheMiddle() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a cat").readFile();
    BlockStructure variant = new StringInputPlugin("a calico cat").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Block alignmentInfo1 = alignmentInformation.getRootBlock().getFirstChild();
    Block alignmentInfo2 = alignmentInfo1.getNextSibling();
    Block alignmentInfo3 = alignmentInfo2.getNextSibling();
    assertEquals("identical: a", alignmentInfo1.toString());
    assertEquals("addition: calico", alignmentInfo2.toString());
    assertEquals("identical: cat", alignmentInfo3.toString());
  }

  public void testAddition_AtTheEnd() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("to be").readFile();
    BlockStructure variant = new StringInputPlugin("to be lost").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Block alignmentInfo1 = alignmentInformation.getRootBlock().getFirstChild();
    Block alignmentInfo2 = alignmentInfo1.getNextSibling();
    Block alignmentInfo3 = alignmentInfo2.getNextSibling();
    assertEquals("identical: to", alignmentInfo1.toString());
    assertEquals("identical: be", alignmentInfo2.toString());
    assertEquals("addition: lost", alignmentInfo3.toString());
  }

  public void testAddition_AtTheStart() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("to be").readFile();
    BlockStructure variant = new StringInputPlugin("not to be").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Block alignmentInfo1 = alignmentInformation.getRootBlock().getFirstChild();
    Block alignmentInfo2 = alignmentInfo1.getNextSibling();
    Block alignmentInfo3 = alignmentInfo2.getNextSibling();
    assertEquals("addition: not", alignmentInfo1.toString());
    assertEquals("identical: to", alignmentInfo2.toString());
    assertEquals("identical: be", alignmentInfo3.toString());
  }

}
