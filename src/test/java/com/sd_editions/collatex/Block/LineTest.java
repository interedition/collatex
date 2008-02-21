package com.sd_editions.collatex.Block;

import junit.framework.TestCase;

public class LineTest extends TestCase {
	public void testGetIndex() throws BlockStructureCascadeException {
		Line line = new Line(1);
		BlockStructure structure = new BlockStructure();
		structure.setRootBlock(line);
		Word word1 = new Word("first");
		Word word2 = new Word("second");
		Word word3 = new Word("third");
		structure.setChildBlock(line, word1);
		structure.setChildBlock(line, word2);
		structure.setChildBlock(line, word3);
		assertEquals(word1, line.get(1));
		assertEquals(word2, line.get(2));
		assertEquals(word3, line.get(3));
	}

}
