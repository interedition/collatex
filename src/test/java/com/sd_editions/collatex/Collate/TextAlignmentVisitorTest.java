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
		Word word1base = (Word) base.getRootBlock().getFirstChild();
		Line line = (Line) variant.getRootBlock();
		Word firstWord = (Word) line.getFirstChild();
		assertEquals(word1base, firstWord.getAlignedWord());
		Word secondWord = (Word) firstWord.getNextSibling();
		Word word2base = (Word) word1base.getNextSibling();
		assertEquals(word2base, secondWord.getAlignedWord());
	}

	public void testPunctionation() throws FileNotFoundException, IOException, BlockStructureCascadeException {
		BlockStructure base = new StringInputPlugin("the black cat").readFile();
		BlockStructure variant = new StringInputPlugin("The, black cat").readFile();
		IntBlockVisitor visitor = new TextAlignmentVisitor(variant);
		base.accept(visitor);
		Word word1base = (Word) base.getRootBlock().getFirstChild();
		Line line = (Line) variant.getRootBlock();
		Word firstWord = (Word) line.getFirstChild();
		assertEquals(word1base, firstWord.getAlignedWord());
	}
	
	public void testVariant() throws FileNotFoundException, IOException, BlockStructureCascadeException {
		BlockStructure base = new StringInputPlugin("a white cat").readFile();
		BlockStructure variant = new StringInputPlugin("a black cat").readFile();
		base.accept(new TextAlignmentVisitor(variant));
		Line baseLine1 = (Line) base.getRootBlock();
		Line variantLine1 = (Line) variant.getRootBlock();
		Word word1base = (Word) baseLine1.getFirstChild();
		Word word1variant = (Word) variantLine1.getFirstChild();
		assertEquals(word1base, word1variant.getAlignedWord());
		Word word2variant = (Word) word1variant.getNextSibling();
		Word word2base = (Word) word1base.getNextSibling();
		assertEquals(word2base, word2variant.getAlignedWord());
		Word word3base = (Word) baseLine1.getLastChild();
		Word word3variant = (Word) variantLine1.getLastChild();
		assertEquals(word3base, word3variant.getAlignedWord());
	}

	public void testOmission() throws FileNotFoundException, IOException, BlockStructureCascadeException {
		BlockStructure base = new StringInputPlugin("a white cat").readFile();
		BlockStructure variant = new StringInputPlugin("a cat").readFile();
		base.accept(new TextAlignmentVisitor(variant));
		Line baseLine1 = (Line) base.getRootBlock();
		Line variantLine1 = (Line) variant.getRootBlock();
		Word word1base = (Word) baseLine1.getFirstChild();
		Word word1variant = (Word) variantLine1.getFirstChild();
		assertEquals(word1base, word1variant.getAlignedWord());
		Word word2variant = (Word) word1variant.getNextSibling();
		Word word2base = (Word) word1base.getNextSibling();
		Word word3base = (Word) baseLine1.getLastChild();
		assertEquals(word3base, word2variant.getAlignedWord());
	}

	public void testAddition() throws FileNotFoundException, IOException, BlockStructureCascadeException {
		BlockStructure base = new StringInputPlugin("a cat").readFile();
		BlockStructure variant = new StringInputPlugin("a calico cat").readFile();
		base.accept(new TextAlignmentVisitor(variant));
		Line baseLine1 = (Line) base.getRootBlock();
		Line variantLine1 = (Line) variant.getRootBlock();
		Word word1base = (Word) baseLine1.getFirstChild();
		Word word1variant = (Word) variantLine1.getFirstChild();
		assertEquals(word1base, word1variant.getAlignedWord());
		Word word2base = (Word) word1base.getNextSibling();
		Word word2variant = (Word) word1variant.getNextSibling();
	  assertNull(word2variant.getAlignedWord());
		Word word3variant = (Word) variantLine1.getLastChild();
		assertEquals(word2base, word3variant.getAlignedWord());
	}
//	public void testAddition() throws FileNotFoundException, IOException, BlockStructureCascadeException {
//		BlockStructure base = new StringInputPlugin("a cat").readFile();
//		BlockStructure variant = new StringInputPlugin("a black cat").readFile();
//		base.accept(new TextAlignmentVisitor(variant));
//		Line baseLine1 = (Line) base.getRootBlock();
//		Line variantLine1 = (Line) variant.getRootBlock();
//		Word word1base = (Word) baseLine1.getFirstChild();
//		Word word1variant = (Word) variantLine1.getFirstChild();
//		assertEquals(word1base, word1variant.getAlignedWord());
//		Word word2variant = (Word) word1variant.getNextSibling();
//		assertNull(word2variant.getAlignedWord());
//		Word word2base = (Word) baseLine1.getLastChild();
//		Word word3variant = (Word) variantLine1.getLastChild();
//		assertEquals(word2base, word3variant.getAlignedWord());
//	}

	
}
