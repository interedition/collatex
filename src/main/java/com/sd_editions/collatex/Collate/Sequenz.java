package com.sd_editions.collatex.Collate;

import java.util.ArrayList;
import java.util.Iterator;

public class Sequenz implements Comparable<Sequenz> {

  private ArrayList<Tuple> seq;
  private int minIndexBase = 9999999;
  private int maxIndexWitn = 0;

  public Sequenz() {
    this.seq = new ArrayList<Tuple>();
  }

  public int getMinIndexBase() {
    return minIndexBase;
  }

  public int getMaxIndexWitn() {
    return maxIndexWitn;
  }

  public ArrayList<Tuple> getSeq() {
    return this.seq;
  }

  public int getSize() {
    return this.seq.size();
  }

  public void isBiggerOrLower(int baseIndex, int witnIndex) {
    if (this.minIndexBase > baseIndex) {
      this.minIndexBase = baseIndex;
    }
    if (this.maxIndexWitn < witnIndex) {
      this.maxIndexWitn = witnIndex;
    }
  }

  public void addNext(Tuple next) {
    isBiggerOrLower(next.baseIndex, next.witnessIndex);
    this.seq.add(next);
  }

  public void addTupelArray(Tuple[] next) {
    for (Tuple element : next) {
      addNext(element);
    }
  }

  public int compareTo(Sequenz tmp) {
    if (this.seq.size() > tmp.seq.size()) {
      return -1;
    } else if (this.seq.size() < tmp.seq.size()) {
      return 1;
    }
    return 0;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    Iterator<Tuple> it = this.seq.iterator();
    while (it.hasNext()) {
      Tuple tuple = it.next();
      sb.append("[" + tuple.baseIndex + "," + tuple.witnessIndex + "]");
    }
    return sb.toString();
  }

}
