package com.sd_editions.collatex.Collate;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.Block.Word;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;

public class TextAlignmentVisitorTest extends TestCase {
  public void testAlignment() {
    Word base = new Word("cat");
    Word variant = new Word("cat");
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    visitor.visitWord(base);
    BlockStructure alignmentInformation = visitor.getResult();
    Table table = (Table) alignmentInformation.getRootBlock();
    assertEquals("identical: cat", table.get(1, 2).toString());
  }
  
  public void testAlignmentVariant() {
    Word base = new Word("cat");
    Word variant = new Word("mat");
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    visitor.visitWord(base);
    BlockStructure alignmentInformation = visitor.getResult();
    Table table = (Table) alignmentInformation.getRootBlock();
    assertEquals("variant-align: cat / mat", table.get(1, 2).toString());
  }

  public void testNonAlignment() {
    Word base = new Word("cat");
    Word variant = new Word("boat");
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    visitor.visitWord(base);
    BlockStructure alignmentInformation = visitor.getResult();
    Table table = (Table) alignmentInformation.getRootBlock();
    assertEquals("non-alignment: cat, boat", table.get(1, 2).toString());
  }

  public void testCapital() {
    Word base = new Word("the");
    Word variant = new Word("The");
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    visitor.visitWord(base);
    BlockStructure alignmentInformation = visitor.getResult();
    Table table = (Table) alignmentInformation.getRootBlock();
    assertEquals("identical: the", table.get(1, 2).toString());
  }

  public void testSentence() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a black cat").readFile();
    BlockStructure variant = new StringInputPlugin("a black cat").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Table table = (Table) alignmentInformation.getRootBlock();
    assertEquals("identical: a", table.get(1, 2).toString());
    assertEquals("identical: black", table.get(1, 4).toString());
    assertEquals("identical: cat", table.get(1, 6).toString());
  }

  public void testPunctuation() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("the black cat").readFile();
    BlockStructure variant = new StringInputPlugin("The, black cat").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Table table = (Table) alignmentInformation.getRootBlock();
    assertEquals("identical: the", table.get(1, 2).toString());
    assertEquals("identical: black", table.get(1, 4).toString());
    assertEquals("identical: cat", table.get(1, 6).toString());
  }

  public void testVariant() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a white cat").readFile();
    BlockStructure variant = new StringInputPlugin("a black cat").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Table table = (Table) alignmentInformation.getRootBlock();
    assertEquals("identical: a", table.get(1, 2).toString());
    assertEquals("variant-align: white / black", table.get(1, 4).toString());
    assertEquals("identical: cat", table.get(1, 6).toString());
  }

  public void testOmission_InTheMiddle() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a white horse").readFile();
    BlockStructure variant = new StringInputPlugin("a horse").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Table table = (Table) alignmentInformation.getRootBlock();
    assertEquals("identical: a", table.get(1, 2).toString());
    assertEquals("omission: white", table.get(1, 4).toString());
    assertEquals("identical: horse", table.get(1, 6).toString());
  }

  public void testOmission_AtTheStart() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a certain death").readFile();
    BlockStructure variant = new StringInputPlugin("certain death").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Table table = (Table) alignmentInformation.getRootBlock();
    assertEquals("omission: a", table.get(1, 2).toString());
    assertEquals("identical: certain", table.get(1, 4).toString());
    assertEquals("identical: death", table.get(1, 6).toString());
  }

    public void testOmission_AtTheEnd() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a calico cat").readFile();
    BlockStructure variant = new StringInputPlugin("a calico").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Table table = (Table) alignmentInformation.getRootBlock();
    assertEquals("identical: a", table.get(1, 2).toString());
    assertEquals("identical: calico", table.get(1, 4).toString());
    assertEquals("omission: cat", table.get(1, 6).toString());
  }

  public void testAddition_InTheMiddle() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a cat").readFile();
    BlockStructure variant = new StringInputPlugin("a calico cat").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Table table = (Table) alignmentInformation.getRootBlock();
    assertEquals("identical: a", table.get(1, 2).toString());
    assertEquals("addition: calico", table.get(1, 3).toString());
    assertEquals("identical: cat", table.get(1, 4).toString());
  }

  public void testAddition_AtTheEnd() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("to be").readFile();
    BlockStructure variant = new StringInputPlugin("to be lost").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Table table = (Table) alignmentInformation.getRootBlock();
    assertEquals("identical: to", table.get(1, 2).toString());
    assertEquals("identical: be", table.get(1, 4).toString());
    assertEquals("addition: lost", table.get(1, 5).toString());
  }

  public void testAddition_AtTheStart() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("to be").readFile();
    BlockStructure variant = new StringInputPlugin("not to be").readFile();
    TextAlignmentVisitor visitor = new TextAlignmentVisitor(variant);
    base.accept(visitor);
    BlockStructure alignmentInformation = visitor.getResult();
    Table table = (Table) alignmentInformation.getRootBlock();
    assertEquals("addition: not", table.get(1, 1).toString());
    assertEquals("identical: to", table.get(1, 2).toString());
    assertEquals("identical: be", table.get(1, 4).toString());
  }

}
