package com.sd_editions.collatex.Collate;

import java.util.ArrayList;
import java.util.Iterator;

public class Sequenz implements Comparable {

  private ArrayList<Tuple> seq;

  public Sequenz() {
    this.seq = new ArrayList<Tuple>();
  }

  public ArrayList<Tuple> getSeq() {
    return this.seq;
  }

  public int getSize() {
    return this.seq.size();
  }

  public void addNext(Tuple next) {
    this.seq.add(next);
  }

  public void addTupelArray(Tuple[] next) {
    for (int i = 0; i < next.length; i++) {
      addNext(next[i]);
    }
  }

  @Override
  public int compareTo(Object obj) {
    Sequenz tmp = (Sequenz) obj;
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
