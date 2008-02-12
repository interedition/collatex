package com.sd_editions.collatex.Collate;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.Block.Line;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;

public class TableViewTest extends TestCase {
	public void testTableView() throws FileNotFoundException, IOException, BlockStructureCascadeException {
		BlockStructure base = new StringInputPlugin("a black cat").readFile();
		BlockStructure variant = new StringInputPlugin("a black cat").readFile();
		base.accept(new TextAlignmentVisitor(variant));
		Line baseLine1 = (Line) base.getRootBlock();
		Line variantLine1 = (Line) variant.getRootBlock();
		TableView view = new TableView(baseLine1, variantLine1);
		assertEquals("a", view.getWord(0, 0));
		assertEquals("black", view.getWord(0, 1));
		assertEquals("cat", view.getWord(0, 2));
		assertEquals("a", view.getWord(1, 0));
		assertEquals("black", view.getWord(1, 1));
		assertEquals("cat", view.getWord(1, 2));
	}
}
