package com.sd_editions.collatex.functional;

import java.util.ArrayList;
import java.util.Iterator;

import com.sd_editions.collatex.Collate.LevTable;
import com.sd_editions.collatex.Collate.Tuple;

public class Functions {
  public static TuplesArrayAndMaxHAndMaxV getLevTuples(LevTable input) {
    TuplesList tuplesList = tableToTuple(input);
    TuplesArrayAndMaxHAndMaxV tuplesArray = new TuplesArrayAndMaxHAndMaxV(tuplesList.size());
    Iterator<Tuple> it = tuplesList.iterator();
    int i = 0;

    while (it.hasNext()) {
      tuplesArray.set(i, it.next());
      i++;
    }
    /* Sort */
    tuplesArray.sort();
    //    showLevTable();
    return tuplesArray;
  }

  @SuppressWarnings("boxing")
  //Search match-Cells and save it in tableToTuple-Object
  public static TuplesList tableToTuple(LevTable input) {
    int pos_h = 0;
    int pos_v = 0;
    boolean merk_1 = false;
    TuplesList tuples = new TuplesList();
    //look for matches
    for (int h = 1; h < input.base.size() + 1; h++) {
      for (int v = 1; v < input.witness.size() + 1; v++) {
        if ((Integer) input.array[v][h] == 0) {
          saveNewTuple(tuples, input.base.get(input.array[0][h]), input.witness.get(input.array[v][0]));
          //if match, go to the next column  
          //break;
        } else if ((Integer) input.array[v][h] == 1 && !merk_1) {
          //if ( h < input.base.size() && (Integer) input.array[v][h + 1] == 0) {
          //  break;
          //}
          //note just the first 1-match in a column
          pos_h = h;
          pos_v = v;
          merk_1 = true;
        } else {}
      }
      if (merk_1) {
        merk_1 = false;
        saveNewTuple(tuples, input.base.get(input.array[0][pos_h]), input.witness.get(input.array[pos_v][0]));
      }
    }
    return tuples;
  }

  @SuppressWarnings("boxing")
  public static void saveNewTuple(TuplesList tuples, ArrayList<Integer> arrLBaseValues, ArrayList<Integer> arrLWitnValues) {
    for (int i = 0; i < arrLBaseValues.size(); i++) {
      for (int j = 0; j < arrLWitnValues.size(); j++) {
        tuples.add(new Tuple(arrLBaseValues.get(i), arrLWitnValues.get(j)));
      }
    }
  }

}
