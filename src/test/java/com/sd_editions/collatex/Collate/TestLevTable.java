package com.sd_editions.collatex.Collate;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;

public class TestLevTable extends TestCase {

  public void testLevTableUnsorted1() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("the hair is from the cat").readFile();
    BlockStructure witness = new StringInputPlugin("the chair stood on the hat").readFile();
    IndexTable iTabbase = new IndexTable(base);
    IndexTable iTabwitness = new IndexTable(witness);
    iTabbase.makeSortedMap();
    iTabwitness.makeSortedMap();
    LevTable levTab = new LevTable(iTabbase.getIndexTable(), iTabwitness.getIndexTable());
    levTab.fillArrayCells();
    //Output
    getOut(levTab, iTabbase, iTabwitness);
  }

  public void testIndexTableSorted1() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("the hair is from the cat").readFile();
    BlockStructure witness = new StringInputPlugin("the chair stood on the hat").readFile();
    IndexTable iTabbase = new IndexTable(base);
    IndexTable iTabwitness = new IndexTable(witness);
    iTabbase.makeSortedMap();
    iTabwitness.makeSortedMap();
    LevTable levTab = new LevTable(iTabbase.getIndexTableSorted(), iTabwitness.getIndexTableSorted());
    levTab.fillArrayCells();
    //Output
    getOut(levTab, iTabbase, iTabwitness);
  }

  public void testIndexTableSorted2() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("the drought of march hath perced to the root").readFile();
    BlockStructure witness = new StringInputPlugin("the march of drought hath perced to the root").readFile();
    IndexTable iTabbase = new IndexTable(base);
    IndexTable iTabwitness = new IndexTable(witness);
    iTabbase.makeSortedMap();
    iTabwitness.makeSortedMap();
    LevTable levTab = new LevTable(iTabbase.getIndexTableSorted(), iTabwitness.getIndexTableSorted());
    levTab.fillArrayCells();
    //Output
    getOut(levTab, iTabbase, iTabwitness);
  }

  public void testIndexTableSorted3() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("the black cat sat on the mat").readFile();
    BlockStructure witness = new StringInputPlugin("the black sat on the mat").readFile();
    IndexTable iTabbase = new IndexTable(base);
    IndexTable iTabwitness = new IndexTable(witness);
    iTabbase.makeSortedMap();
    iTabwitness.makeSortedMap();
    LevTable levTab = new LevTable(iTabbase.getIndexTableSorted(), iTabwitness.getIndexTableSorted());
    levTab.fillArrayCells();
    //Output
    getOut(levTab, iTabbase, iTabwitness);
  }

  public void testIndexTableSorted4() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("Ich hab auch hier wieder ein Pläzchen").readFile();
    BlockStructure witness = new StringInputPlugin("Auch hier hab ich wieder ein Plätzchen").readFile();
    IndexTable iTabbase = new IndexTable(base);
    IndexTable iTabwitness = new IndexTable(witness);
    iTabbase.makeSortedMap();
    iTabwitness.makeSortedMap();
    LevTable levTab = new LevTable(iTabbase.getIndexTableSorted(), iTabwitness.getIndexTableSorted());
    levTab.fillArrayCells();
    //Output
    getOut(levTab, iTabbase, iTabwitness);
  }

  public void testIndexTableSorted5() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("Auch hier hab ich wieder ein Plätzchen").readFile();
    BlockStructure witness = new StringInputPlugin("Ich hab auch hier wieder ein Pläzchen").readFile();
    IndexTable iTabbase = new IndexTable(base);
    IndexTable iTabwitness = new IndexTable(witness);
    iTabbase.makeSortedMap();
    iTabwitness.makeSortedMap();
    LevTable levTab = new LevTable(iTabbase.getIndexTableSorted(), iTabwitness.getIndexTableSorted());
    levTab.fillArrayCells();
    //Output
    getOut(levTab, iTabbase, iTabwitness);
  }

  public void testIndexTableSorted2_1() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("the drought of march hath perced to the root").readFile();
    BlockStructure witness = new StringInputPlugin("the march of the drought hath perced to the root").readFile();
    IndexTable iTabbase = new IndexTable(base);
    IndexTable iTabwitness = new IndexTable(witness);
    iTabbase.makeSortedMap();
    iTabwitness.makeSortedMap();
    LevTable levTab = new LevTable(iTabbase.getIndexTableSorted(), iTabwitness.getIndexTableSorted());
    levTab.fillArrayCells();
    //Output
    getOut(levTab, iTabbase, iTabwitness);
  }

  public void testIndexTableSorted6() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a cat and dog").readFile();
    BlockStructure witness = new StringInputPlugin("a cat or dog and").readFile();
    IndexTable iTabbase = new IndexTable(base);
    IndexTable iTabwitness = new IndexTable(witness);
    iTabbase.makeSortedMap();
    iTabwitness.makeSortedMap();
    LevTable levTab = new LevTable(iTabbase.getIndexTableSorted(), iTabwitness.getIndexTableSorted());
    levTab.fillArrayCells();
    //Output
    getOut(levTab, iTabbase, iTabwitness);
  }

  /*  public void testSentence() throws FileNotFoundException, IOException, BlockStructureCascadeException {
      Tuple[] tuples = new Tuple[] { new Tuple(1, 4), new Tuple(2, 5), new Tuple(5, 6), new Tuple(6, 7), new Tuple(7, 8), new Tuple(8, 9), new Tuple(9, 10) };
      Table table = wordAlignmentTable("the drought of march hath perced to the root", "the march of the drought hath perced to the root", tuples);
      assertEquals("addition: the march of", table.get(1, 1).toString());
      assertEquals("identical: the", table.get(1, 2).toString());
      assertEquals("identical: drought", table.get(1, 4).toString());
      assertEquals("omission: of", table.get(1, 6).toString());
      assertEquals("omission: march", table.get(1, 8).toString());
      assertEquals("identical: hath", table.get(1, 10).toString());
      assertEquals("identical: perced", table.get(1, 12).toString());
      assertEquals("identical: to", table.get(1, 14).toString());
      assertEquals("identical: the", table.get(1, 16).toString());
      assertEquals("identical: root", table.get(1, 18).toString());
      
    }
    
    public void testSentence2() throws FileNotFoundException, IOException, BlockStructureCascadeException {
      Tuple[] tuples = new Tuple[] { new Tuple(1, 1), new Tuple(2, 2), new Tuple(4, 3), new Tuple(5, 4), new Tuple(6, 5), new Tuple(7, 6)};
      Table table = wordAlignmentTable("the black cat sat on the mat", "the black sat on the cat", tuples);
      assertEquals("identical: the", table.get(1, 2).toString());
      assertEquals("identical: black", table.get(1, 4).toString());
      assertEquals("omission: cat", table.get(1, 6).toString());
      assertEquals("identical: sat", table.get(1, 8).toString());
      assertEquals("identical: on", table.get(1, 10).toString());
      assertEquals("identical: the", table.get(1, 12).toString());
      assertEquals("variant-align: mat / cat", table.get(1, 14).toString());
          
    }
  */
  public void testSentence3() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    Tuple[] tuples = new Tuple[] { new Tuple(1, 1), new Tuple(2, 2), new Tuple(4, 4) };
    Table table = wordAlignmentTable("a cat and dog", "a cat or dog and", tuples);
    assertEquals("identical: a", table.get(1, 2).toString());
    assertEquals("identical: cat", table.get(1, 4).toString());
    assertEquals("replacement: and / or", table.get(1, 6).toString());
    assertEquals("identical: dog", table.get(1, 8).toString());
    assertEquals("addition: and", table.get(1, 9).toString());
  }

  private Table wordAlignmentTable(final String baseString, final String witnessString, Tuple[] tuples) throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin(baseString).readFile();
    BlockStructure variant = new StringInputPlugin(witnessString).readFile();
    TupleToTable tupleToTable = new TupleToTable(base, variant, tuples);
    Table table = tupleToTable.getTable();
    return table;
  }

  public void getIndexTableBase(IndexTable iTabbase) throws FileNotFoundException, IOException, BlockStructureCascadeException {
    System.out.println();
    System.out.println("Base :" + "\t" + iTabbase.getIndexTableSorted().toString());
  }

  public void getIndexTableWitn(IndexTable iTabwitness) throws FileNotFoundException, IOException, BlockStructureCascadeException {
    System.out.println("Witn :" + "\t" + iTabwitness.getIndexTableSorted().toString());
    System.out.println();
  }

  public void getTup(LevTable levTab) throws FileNotFoundException, IOException, BlockStructureCascadeException {
    Tuple[] arrT = levTab.getLevTuples();
    for (int i = 0; i < arrT.length; i++) {
      if (i == 0) {
        System.out.print("Tuples = ");
      }
      if (arrT[i] != null) {
        System.out.print(arrT[i].toString());
      }
    }
    System.out.println();
    System.out.println("-------------------------------------------------------------------");
  }

  public void getOut(LevTable levTab, IndexTable iTabbase, IndexTable iTabwitness) throws FileNotFoundException, IOException, BlockStructureCascadeException {
    levTab.toString();
    getIndexTableBase(iTabbase);
    getIndexTableWitn(iTabwitness);
    getTup(levTab);
  }

}