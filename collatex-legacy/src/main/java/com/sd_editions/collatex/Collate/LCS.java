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

import java.util.List;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.functional.Functions;
import com.sd_editions.collatex.functional.TuplesArrayAndMaxHAndMaxV;

public class LCS {

  private final Tuple[] LCS;
  private final DotMatrix dotM;

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
