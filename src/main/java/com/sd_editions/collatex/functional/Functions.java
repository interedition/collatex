package com.sd_editions.collatex.functional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import com.sd_editions.collatex.Collate.LevTable;
import com.sd_editions.collatex.Collate.Sequenz;
import com.sd_editions.collatex.Collate.Tuple;
import com.sd_editions.collatex.Collate.TupleComparatorBI;

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
    //regard Lev-Distanz
    TuplesList newTuples = new TuplesList();

    //Sort-Start
    Sequenz seq1 = new Sequenz();
    for (Iterator iterator = tuples.iterator(); iterator.hasNext();) {
      Tuple type = (Tuple) iterator.next();
      seq1.addNext(type);
    }
    ArrayList<Tuple> arrList_1 = seq1.getSeq();
    System.out.println("arrList_1_vor" + arrList_1.toString());
    Collections.sort(arrList_1, new TupleComparatorBI());
    System.out.println("arrList_1_nach" + arrList_1.toString());
    //Sort-Ende

    Object[] arr1 = arrList_1.toArray();
    newTuples.add((Tuple) arr1[0]);
    for (int i = 0, j = 1; j < arr1.length; i++, j++) {
      Tuple tup1 = (Tuple) arr1[i];
      Tuple tup2 = (Tuple) arr1[j];
      if ((tup1.witnessIndex == tup2.witnessIndex && tup1.baseIndex == tup2.baseIndex - 1)) {
        System.out.println("Tupel" + tup2.toString() + " wurde aus der Tupelliste entfernt, da Tupel" + tup1.toString() + " die bessere Lev-Distanz hat");
        System.out.println("--->1" + tup1.toString() + tup2.toString());
      } else {
        System.out.println("tup2" + tup2.toString());
        newTuples.add(tup2);
      }
      if (j == arr1.length - 1) {
        newTuples.add(tup2);
      }
    }
    //**********************************
    TuplesList newTuples2 = new TuplesList();
    //Sort-Start
    Sequenz seq2 = new Sequenz();
    for (Iterator iterator = newTuples.iterator(); iterator.hasNext();) {
      Tuple type = (Tuple) iterator.next();
      seq2.addNext(type);
    }
    ArrayList<Tuple> arrList_2 = seq2.getSeq();
    System.out.println("arrList_2_vor" + arrList_2.toString());
    Collections.sort(arrList_2, new TupleComparatorBI());
    System.out.println("arrList_2_nach" + arrList_2.toString());
    //Sort-Ende

    Object[] arr2 = arrList_2.toArray();
    //newTuples_.add((Tuple) arr_[0]);
    for (int i = 0, j = 1; j < arr2.length; i++, j++) {
      Tuple tup11 = (Tuple) arr2[i];
      Tuple tup22 = (Tuple) arr2[j];
      if ((tup11.baseIndex == tup22.baseIndex && tup11.witnessIndex == tup22.witnessIndex - 1)) {
        System.out.println("Tupel" + tup11.toString() + " has been removed, because " + tup22.toString() + " has the better Lev-Dist.");
      } else {
        newTuples2.add(tup11);
      }
      if (j == arr2.length - 1) {
        newTuples2.add(tup22);
      }
    }
    for (Iterator iterator = newTuples2.iterator(); iterator.hasNext();) {
      Tuple tuple = (Tuple) iterator.next();
      System.out.println("newTuples2" + tuple.toString());
    }
    //**********************************
    //ENDE

    return newTuples2;
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
