package com.sd_editions.collatex.Collate;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;

public class TupleToTableTest extends TestCase {
  public void testSentence() throws FileNotFoundException, IOException, BlockStructureCascadeException {
  	Tuple[] tuples = new Tuple[] { new Tuple(1,1), new Tuple(2,2), new Tuple(3,3) };
    Table table = wordAlignmentTable("a black cat", "a black cat", tuples);
    assertEquals("identical: a", table.get(1, 2).toString());
    assertEquals("identical: black", table.get(1, 4).toString());
    assertEquals("identical: cat", table.get(1, 6).toString());
  }

	
	
	private Table wordAlignmentTable(final String baseString, final String witnessString, Tuple[] tuples) throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin(baseString).readFile();
    BlockStructure variant = new StringInputPlugin(witnessString).readFile();
    TupleToTable tupleToTable = new TupleToTable(base, variant, tuples);
    Table table = (Table) tupleToTable.getTable();
    return table;
  }


}
