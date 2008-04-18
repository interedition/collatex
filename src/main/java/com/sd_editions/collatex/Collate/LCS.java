package com.sd_editions.collatex.Collate;

import java.util.List;

import com.sd_editions.collatex.Block.BlockStructure;

public class LCS {
  
  private Tuple[] LCS;
  private DotMatrix dotM;
  
  public LCS(BlockStructure base, List<BlockStructure> witnesses) {
    IndexTable iTabbase = new IndexTable(base);
    IndexTable iTabwitness = new IndexTable(witnesses.get(0));
    iTabbase.makeSortedMap();
    iTabwitness.makeSortedMap();
    LevTable levTab = new LevTable(iTabbase.getIndexTableSorted(),iTabwitness.getIndexTableSorted());
    levTab.fillArrayCells();
    this.dotM = new DotMatrix(levTab.getLevTuples(),levTab.getMaxH(), levTab.getMaxV());
    this.LCS=dotM.getLCS();
  }
  
  public Tuple[] getLCS() {
    return this.LCS;
  }
  

  @Override
  public String toString() {
    System.out.print("LCS: ");
    for (Tuple item : this.LCS) {
      System.out.print(item.toString());
    }
    System.out.println();
    return "";
  }
  
}
