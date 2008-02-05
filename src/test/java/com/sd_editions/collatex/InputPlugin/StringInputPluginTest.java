package com.sd_editions.collatex.InputPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;

import junit.framework.TestCase;

public class StringInputPluginTest extends TestCase {
	public void testHaha() throws FileNotFoundException, IOException, BlockStructureCascadeException {
		String text ="one two three words";
		IntInputPlugin plugin = new StringInputPlugin(text);
		BlockStructure document = plugin.readFile();
		assertEquals(5, document.getNumberOfBlocks());
	}
}
