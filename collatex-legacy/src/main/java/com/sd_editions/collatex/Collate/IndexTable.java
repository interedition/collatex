/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sd_editions.collatex.Collate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sd_editions.collatex.Block.Block;
import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.Word;

public class IndexTable {
  private LinkedHashMap<String, ArrayList<Integer>> indTable;
  private TreeMap<String, ArrayList<Integer>> indTableSorted;
  private Block base = null;
  private int baseIndex = 1;

  public IndexTable() {
    newHashMap();
  }

  public IndexTable(BlockStructure bs) {
    newHashMap(bs);
  }

  public void newHashMap() {
    this.indTable = Maps.newLinkedHashMap();
  }

  public void newHashMap(BlockStructure bs) {
    this.indTable = Maps.newLinkedHashMap();
    construct(bs);
  }

  public LinkedHashMap<String, ArrayList<Integer>> getIndexTable() {
    return indTable;
  }

  public boolean proofIfWordExist(Word w) {
    //return this.indTable.containsKey(w.getContent().CASE_INSENSITIVE_ORDER);
    Iterator<String> itKey = this.indTable.keySet().iterator();
    while (itKey.hasNext()) {
      if (itKey.next().equalsIgnoreCase(w.getContent())) {
        //System.out.println("Match for "+w.getContent());
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("boxing")
  public void addIndex(Word w) {
    this.indTable.get(w.getContent()).add(this.baseIndex);
  }

  public void addNext(String str, Integer ind) {
    ArrayList<Integer> arrL = Lists.newArrayList();
    arrL.add(ind);
    this.indTable.put(str, arrL);
  }

  public void makeSortedMap() {
    this.indTableSorted = new TreeMap<String, ArrayList<Integer>>(indTable);
  }

  public TreeMap<String, ArrayList<Integer>> getIndexTableSorted() {
    return indTableSorted;
  }

  void construct(BlockStructure bs) {
    this.base = bs.getRootBlock();
    fillIndexTable();
  }

  @SuppressWarnings("boxing")
  private void fillIndexTable() {
    Word w = (Word) base.getFirstChild();

    while (w.hasNextSibling()) {
      if (proofIfWordExist(w)) {
        addIndex(w);
      } else {
        addNext(w.getContent(), baseIndex);
      }
      w = (Word) w.getNextSibling();
      baseIndex++;
    }
    if (proofIfWordExist(w)) {
      addIndex(w);
    } else {
      addNext(w.getContent(), baseIndex);
    }
  }

  @Override
  public String toString() {
    Iterator<String> itKey = this.indTable.keySet().iterator();
    Iterator<ArrayList<Integer>> itVal = this.indTable.values().iterator();
    while (itKey.hasNext()) {
      System.out.println(itKey.next() + " , " + itVal.next().toString());
    }
    return "";
  }

}