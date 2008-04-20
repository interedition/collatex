package com.sd_editions.collatex.Collate;

import java.util.ArrayList;
import java.util.Iterator;

public class Sequenz implements Comparable<Sequenz> {

  private ArrayList<Tuple> seq;
  private int maxBase = 0;
  private int minBase = 9999999;
  private int maxWitn = 0;
  private int minWitn = 9999999;

  public Sequenz() {
    this.seq = new ArrayList<Tuple>();
  }

  public int getMaxBase() {
    return maxBase;
  }

  public int getMinBase() {
    return minBase;
  }

  public int getMaxWitn() {
    return maxWitn;
  }

  public int getMinWitn() {
    return minWitn;
  }

  public ArrayList<Tuple> getSeq() {
    return this.seq;
  }

  public int getSize() {
    return this.seq.size();
  }

  private void setMaxMinIndexValues(int baseIndex, int witnIndex) {
    if (this.maxBase < baseIndex) {
      this.maxBase = baseIndex;
    }
    if (this.minBase > baseIndex) {
      this.minBase = baseIndex;
    }
    if (this.maxWitn < witnIndex) {
      this.maxWitn = witnIndex;
    }
    if (this.minWitn > witnIndex) {
      this.minWitn = witnIndex;
    }
  }

  public void addNext(Tuple next) {
    setMaxMinIndexValues(next.baseIndex, next.witnessIndex);
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
