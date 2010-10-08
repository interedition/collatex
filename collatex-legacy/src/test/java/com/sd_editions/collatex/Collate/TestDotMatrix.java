/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sd_editions.collatex.Collate;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;
import com.sd_editions.collatex.functional.Functions;
import com.sd_editions.collatex.functional.TuplesArrayAndMaxHAndMaxV;

public class TestDotMatrix extends TestCase {

  public void testSeqenzMatrix_1() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("the drought of march hath perced to the root").readFile();
    BlockStructure witness = new StringInputPlugin("the march of the drought hath perced to the root").readFile();
    IndexTable iTabbase = new IndexTable(base);
    IndexTable iTabwitness = new IndexTable(witness);
    iTabbase.makeSortedMap();
    iTabwitness.makeSortedMap();
    LevTable levTab = new LevTable(iTabbase.getIndexTableSorted(), iTabwitness.getIndexTableSorted());
    levTab.fillArrayCells();
    TuplesArrayAndMaxHAndMaxV levTuples = Functions.getLevTuples(levTab);
    DotMatrix dotM = new DotMatrix(levTuples.getTuplesArray(), levTuples.getMaxH(), levTuples.getMaxV());
    dotM.toString();
  }

  public void testSeqenzMatrix_2() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("the big bug had a big head").readFile();
    BlockStructure witness = new StringInputPlugin("the bug had a small head").readFile();
    IndexTable iTabbase = new IndexTable(base);
    IndexTable iTabwitness = new IndexTable(witness);
    iTabbase.makeSortedMap();
    iTabwitness.makeSortedMap();
    LevTable levTab = new LevTable(iTabbase.getIndexTableSorted(), iTabwitness.getIndexTableSorted());
    levTab.fillArrayCells();
    TuplesArrayAndMaxHAndMaxV levTuples = Functions.getLevTuples(levTab);
    DotMatrix dotM = new DotMatrix(levTuples.getTuplesArray(), levTuples.getMaxH(), levTuples.getMaxV());
    dotM.toString();
  }

  public void testSeqenzMatrix_3() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a cat and dog").readFile();
    BlockStructure witness = new StringInputPlugin("a cat or dog and").readFile();
    IndexTable iTabbase = new IndexTable(base);
    IndexTable iTabwitness = new IndexTable(witness);
    iTabbase.makeSortedMap();
    iTabwitness.makeSortedMap();
    LevTable levTab = new LevTable(iTabbase.getIndexTableSorted(), iTabwitness.getIndexTableSorted());
    levTab.fillArrayCells();
    TuplesArrayAndMaxHAndMaxV levTuples = Functions.getLevTuples(levTab);
    DotMatrix dotM = new DotMatrix(levTuples.getTuplesArray(), levTuples.getMaxH(), levTuples.getMaxV());
    dotM.toString();
  }

  public void testSeqenzMatrix_3_3() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("the black cat sat on the mat").readFile();
    BlockStructure witness = new StringInputPlugin("the cat sat on the black mat").readFile();
    IndexTable iTabbase = new IndexTable(base);
    IndexTable iTabwitness = new IndexTable(witness);
    iTabbase.makeSortedMap();
    iTabwitness.makeSortedMap();
    LevTable levTab = new LevTable(iTabbase.getIndexTableSorted(), iTabwitness.getIndexTableSorted());
    levTab.fillArrayCells();
    TuplesArrayAndMaxHAndMaxV levTuples = Functions.getLevTuples(levTab);
    DotMatrix dotM = new DotMatrix(levTuples.getTuplesArray(), levTuples.getMaxH(), levTuples.getMaxV());
    dotM.toString();
  }

  /*  
   public void testSeqenzMatrix_4() throws FileNotFoundException, IOException, BlockStructureCascadeException {
     BlockStructure base = new StringInputPlugin("Auch hier hab ich wieder ein Pl�tzchen").readFile();
     BlockStructure witness = new StringInputPlugin("Ich hab auch hier wieder ein Pl�zchen").readFile();
     IndexTable iTabbase = new IndexTable(base);
     IndexTable iTabwitness = new IndexTable(witness);
     iTabbase.makeSortedMap();
     iTabwitness.makeSortedMap();
     LevTable levTab = new LevTable(iTabbase.getIndexTableSorted(),iTabwitness.getIndexTableSorted());
     levTab.fillArrayCells();
     DotMatrix dotM = new DotMatrix(levTab.getLevTuples(),levTab.getMaxH(), levTab.getMaxV());
     dotM.toString();
  }

   
   public void testSeqenzMatrix_5() throws FileNotFoundException, IOException, BlockStructureCascadeException {
     BlockStructure base = new StringInputPlugin("the black cat on the table").readFile();
     BlockStructure witness = new StringInputPlugin("the black saw the black cat on the table").readFile();
     IndexTable iTabbase = new IndexTable(base);
     IndexTable iTabwitness = new IndexTable(witness);
     iTabbase.makeSortedMap();
     iTabwitness.makeSortedMap();
     LevTable levTab = new LevTable(iTabbase.getIndexTableSorted(),iTabwitness.getIndexTableSorted());
     levTab.fillArrayCells();
     DotMatrix dotM = new DotMatrix(levTab.getLevTuples(),levTab.getMaxH(), levTab.getMaxV());
     dotM.toString();
  }  
   
  
  public void testSentence() throws FileNotFoundException, IOException, BlockStructureCascadeException {
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

   private Table wordAlignmentTable(final String baseString, final String witnessString, Tuple[] tuples) throws FileNotFoundException, IOException, BlockStructureCascadeException {
     BlockStructure base = new StringInputPlugin(baseString).readFile();
     BlockStructure variant = new StringInputPlugin(witnessString).readFile();
     TupleToTable tupleToTable = new TupleToTable(base, variant, tuples);
     Table table = tupleToTable.getTable();
     return table;
   }
  */

}
