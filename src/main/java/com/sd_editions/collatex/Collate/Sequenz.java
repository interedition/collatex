package com.sd_editions.collatex.Collate;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.collect.Lists;

public class Sequenz implements Comparable<Sequenz> {

  private final ArrayList<Tuple> seq;
  private int isTransposSeqValue;

  public Sequenz() {
    this.seq = Lists.newArrayList();
    this.isTransposSeqValue = 0;
  }

  public int getTransposSeqValue() {
    return isTransposSeqValue;
  }

  public void setTransposSeqValue() {
    this.isTransposSeqValue = 1;
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
    //1. length 2.TransPosSeq or no 3.differenz betwein baseIndex and witnessIndex
    if (this.seq.size() > tmp.seq.size()) {
      return -1;
    } else if (this.seq.size() < tmp.seq.size()) {
      return 1;
    } else if (this.seq.size() == tmp.seq.size()) {
      if (this.getTransposSeqValue() > tmp.getTransposSeqValue()) {
        return -1;
      } else if (this.seq.size() < tmp.seq.size()) {
        return 1;
      } else if (this.seq.size() == tmp.seq.size()) {
        double newer = tmp.getSeq().get(0).baseIndex - tmp.getSeq().get(0).witnessIndex;
        double me = this.getSeq().get(0).baseIndex - this.getSeq().get(0).witnessIndex;
        if (me == newer) {
          return 0;
        } else if (me < newer) {
          return -1;
        } else if (me > newer) {
          return 1;
        }
        return 0;
      }
      return 0;
    }
    return 0;
  }

  public int compareTo_old(Sequenz tmp) {
    if (this.seq.size() > tmp.seq.size()) {
      return -1;
    } else if (this.seq.size() < tmp.seq.size()) {
      return 1;
    } else if (this.seq.size() == tmp.seq.size()) {
      //double newer_p2 = 100.0 + (10 / levSumOfSeq + 1.0);
      //double me_p2 = 100.0 + (10 / levSumOfSeq + 1.0);
      //double newer_p1 = 1 / (Math.abs(tmp.getSeq().get(0).baseIndex - tmp.getSeq().get(0).witnessIndex) + 1.0);
      //double me_p1 = 1 / (Math.abs(this.getSeq().get(0).baseIndex - this.getSeq().get(0).witnessIndex) + 1.0);
      double newer_p1 = tmp.getSeq().get(0).baseIndex - tmp.getSeq().get(0).witnessIndex;
      double me_p1 = this.getSeq().get(0).baseIndex - this.getSeq().get(0).witnessIndex;
      double newer = newer_p1;
      double me = me_p1;
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
