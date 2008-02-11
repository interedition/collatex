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

	
}
