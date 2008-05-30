package com.sd_editions.collatex.functional;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.Collate.Tuple;

public class TuplesList {
  ArrayList<Tuple> arrL;

  public TuplesList() {
    this.arrL = Lists.newArrayList();
  }

  public int size() {
    return arrL.size();
  }

  public Iterator<Tuple> iterator() {
    return arrL.iterator();
  }

  public void add(Tuple tuple) {
    arrL.add(tuple);
  }

}
