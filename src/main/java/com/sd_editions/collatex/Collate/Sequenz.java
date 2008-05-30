package com.sd_editions.collatex.Collate;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.collect.Lists;

public class Sequenz implements Comparable<Sequenz> {

  private ArrayList<Tuple> seq;

  public Sequenz() {
    this.seq = Lists.newArrayList();
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
    for (Tuple element : next) {
      addNext(element);
    }
  }

  public int compareTo(Sequenz tmp) {
    if (this.seq.size() > tmp.seq.size()) {
      return -1;
    } else if (this.seq.size() < tmp.seq.size()) {
      return 1;
    } else if (this.seq.size() == tmp.seq.size()) {
      double newer = 1 / (Math.abs(tmp.getSeq().get(0).baseIndex - tmp.getSeq().get(0).witnessIndex) + 1.0);
      double me = 1 / (Math.abs(this.getSeq().get(0).baseIndex - this.getSeq().get(0).witnessIndex) + 1.0);
      if (me == newer) {
        return 0;
      } else if (me > newer) {
        return -1;
      } else if (me < newer) {
        return 1;
      }
      return 0;
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
