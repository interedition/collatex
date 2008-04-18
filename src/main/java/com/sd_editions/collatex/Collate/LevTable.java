package com.sd_editions.collatex.Collate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import com.sd_editions.collatex.Block.Word;

public class LevTable {
  private Object[][] array;
  private Tuple[] tuples;
  private int anz, maxH, maxV;
  private Map<String, ArrayList<Integer>> base;
  private Map<String, ArrayList<Integer>> witness;

  @SuppressWarnings("hiding")
  public LevTable(Map<String, ArrayList<Integer>> base, Map<String, ArrayList<Integer>> witness) {
    this.base = base;
    this.witness = witness;
    this.array = new Object[this.witness.size() + 1][this.base.size() + 1];
  }

  public int getMaxH() {
    return maxH;
  }

  public int getMaxV() {
    return maxV;
  }

  @SuppressWarnings("boxing")
  public Integer getLevDist(String base, String wit) {
    Word bas = new Word(base);
    Word witn = new Word(wit);
    return bas.alignmentFactor(witn);
  }

  public Tuple[] getLevTuples() {
    ArrayList<Tuple> arrL = tableToTuple();
    tuples = new Tuple[this.anz];
    Iterator<Tuple> it = arrL.iterator();
    int i = 0;

    while (it.hasNext()) {
      this.tuples[i] = it.next();
      if (tuples[i].baseIndex > this.maxH) {
        this.maxH = tuples[i].baseIndex;
      }
      if (tuples[i].witnessIndex > this.maxV) {
        this.maxV = tuples[i].witnessIndex;
      }
      //System.out.println(this.maxH+"---"+this.maxV+""+tuples[i]);
      i++;
    }
    /* Sort array */
    Arrays.sort(tuples);
    showLevTable();
    return tuples;
  }

  public void showLevTable() {
    System.out.print("Lev: ");
    for (Tuple element : tuples) {
      System.out.print(element.toString());
    }
  }

  @SuppressWarnings("boxing")
  public void saveNewTuple(ArrayList<Tuple> resultList, ArrayList<Integer> arrLBaseValues, ArrayList<Integer> arrLWitnValues) {
    for (int i = 0; i < arrLBaseValues.size(); i++) {
      for (int j = 0; j < arrLWitnValues.size(); j++) {
        resultList.add(new Tuple(arrLBaseValues.get(i), arrLWitnValues.get(j)));
        this.anz++;
      }
    }
  }

  @SuppressWarnings("boxing")
  public ArrayList<Tuple> tableToTuple() {
    int pos_h = 0;
    int pos_v = 0;
    boolean merk_1 = false;
    ArrayList<Tuple> arrL = new ArrayList<Tuple>();
    //look for matches
    for (int h = 1; h < this.base.size() + 1; h++) {
      for (int v = 1; v < this.witness.size() + 1; v++) {
        if ((Integer) array[v][h] == 0) {
          saveNewTuple(arrL, this.base.get(array[0][h]), this.witness.get(array[v][0]));
          //if match, go to the next column  
          //break;
        } else if ((Integer) array[v][h] == 1 && !merk_1) {
          //note just the first 1-match in a column
          pos_h = h;
          pos_v = v;
          merk_1 = true;
        } else {}
      }
      if (merk_1) {
        merk_1 = false;
        saveNewTuple(arrL, this.base.get(array[0][pos_h]), this.witness.get(array[pos_v][0]));
      }
    }
    return arrL;
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
