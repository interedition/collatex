package com.sd_editions.collatex.Collate;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;

public class TestIndexTable extends TestCase  {
  
  public void testIndexTableUnsorted1() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a red cat or black cat sat").readFile();
    IndexTable iTabbase = new IndexTable(base);
    assertEquals("{a=[1], red=[2], cat=[3, 6], or=[4], black=[5], sat=[7]}", iTabbase.getIndexTable().toString());
  }
  
  public void testIndexTableSorted1() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a red cat or black cat sat").readFile();
    IndexTable iTabbase = new IndexTable(base);
    iTabbase.makeSortedMap();
    assertEquals("{a=[1], black=[5], cat=[3, 6], or=[4], red=[2], sat=[7]}", iTabbase.getIndexTableSorted().toString());
  }
}