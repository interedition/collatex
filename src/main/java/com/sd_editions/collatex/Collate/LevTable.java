package com.sd_editions.collatex.Collate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.sd_editions.collatex.Block.Word;
import com.sd_editions.collatex.functional.Functions;

public class LevTable {
  public Object[][] array;
  public Map<String, ArrayList<Integer>> base;
  public Map<String, ArrayList<Integer>> witness;

  @SuppressWarnings("hiding")
  public LevTable(Map<String, ArrayList<Integer>> base, Map<String, ArrayList<Integer>> witness) {
    this.base = base;
    this.witness = witness;
    this.array = new Object[this.witness.size() + 1][this.base.size() + 1];
  }

  @SuppressWarnings("boxing")
  public Integer getLevDist(String base, String wit) {
    Word bas = new Word(base);
    Word witn = new Word(wit);
    return bas.alignmentFactor(witn);
  }

  public int getLevDistRelativ(int levDist, int strLength) {
    return 1 - (levDist / strLength) * 100;
  }

  public void showLevTable() {
    Tuple[] tuples = Functions.getLevTuples(this).getTuplesArray();
    System.out.print("Lev: ");
    for (Tuple element : tuples) {
      System.out.print(element.toString());
    }
  }

  @SuppressWarnings("boxing")
  public String[] mapToStringArray(Map<String, ArrayList<Integer>> source, Integer length) {
    String[] sArray = new String[length + 1];
    Iterator<String> itKey = source.keySet().iterator();

    sArray[0] = null;
    int i = 1;
    while (itKey.hasNext()) {
      sArray[i] = itKey.next();
      i++;
    }
    return sArray;
  }

  @SuppressWarnings("boxing")
  //Fill the Array-Cells with Lev-Distanz-Values
  public void fillArrayCells() {
    String[] base = mapToStringArray(this.base, this.base.size());
    String[] witn = mapToStringArray(this.witness, this.witness.size());
    for (int v = 0; v < this.witness.size() + 1; v++) {
      for (int h = 0; h < this.base.size() + 1; h++) {
        if (v == 0 && h == 0) {
          array[v][h] = "";
        } else if (v == 0) {
          array[v][h] = base[h];
        } else if (h == 0) {
          array[v][h] = witn[v];
        } else {
          if (getLevDist(base[h], witn[v]) == 0) {
            //this.anz++;
            //break;
          } else if (getLevDist(base[h], witn[v]) == 1) {
            //this.anz++;
          } else {}
          array[v][h] = getLevDist(base[h], witn[v]);
        }
      }
    }
  }

  @Override
  public String toString() {
    for (int i = 0; i < this.witness.size() + 1; i++) {
      for (int j = 0; j < this.base.size() + 1; j++) {
        System.out.printf("%10s", array[i][j]);
      }
      System.out.println();
    }
    return "done";
  }

}
