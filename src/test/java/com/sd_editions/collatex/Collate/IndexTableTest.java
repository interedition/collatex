package com.sd_editions.collatex.Collate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import junit.framework.TestCase;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;

public class IndexTableTest extends TestCase {

  @SuppressWarnings("boxing")
  public void testIndexTable() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a red cat or black cat sat").readFile();
    IndexTable iTabbase = new IndexTable(base);
    LinkedHashMap<String, ArrayList<Integer>> iTable = iTabbase.getIndexTable();
    assertEquals(Arrays.asList(3,6), iTable.get("cat"));
    assertEquals(Arrays.asList(2), iTable.get("red"));
    assertEquals(Arrays.asList(7), iTable.get("sat"));
    assertEquals(Arrays.asList(4), iTable.get("or"));
    assertEquals(Arrays.asList(1), iTable.get("a"));
    assertEquals(Arrays.asList(5), iTable.get("black"));
  }

  public void testIndexTableSorted() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a red cat or black cat sat").readFile();
    IndexTable iTabbase = new IndexTable(base);
    iTabbase.makeSortedMap();
    assertEquals("{a=[1], black=[5], cat=[3, 6], or=[4], red=[2], sat=[7]}", iTabbase.getIndexTableSorted().toString());
  }

}