package com.sd_editions.collatex.Collate;

import java.util.List;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.functional.Functions;
import com.sd_editions.collatex.functional.TuplesArrayAndMaxHAndMaxV;

public class LCS {

  private Tuple[] LCS;
  private DotMatrix dotM;

  public LCS(BlockStructure base, List<BlockStructure> witnesses, int i) {
    IndexTable iTabbase = new IndexTable(base);
    IndexTable iTabwitness = new IndexTable(witnesses.get(i));
    iTabbase.makeSortedMap();
    iTabwitness.makeSortedMap();
    LevTable levTab = new LevTable(iTabbase.getIndexTableSorted(), iTabwitness.getIndexTableSorted());
    levTab.fillArrayCells();
    TuplesArrayAndMaxHAndMaxV tuplesArray = Functions.getLevTuples(levTab);
    this.dotM = new DotMatrix(tuplesArray.getTuplesArray(), tuplesArray.getMaxH(), tuplesArray.getMaxV());
    this.LCS = dotM.getLCS();
  }

  public Tuple[] getLCS() {
    dotM.toString();
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
