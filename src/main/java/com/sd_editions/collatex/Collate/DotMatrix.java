package com.sd_editions.collatex.Collate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

public class DotMatrix {

  public ArrayList<Sequenz> allSequenz;
  public Tuple[] LCS;
  int[][] dotMatrix;
  public ArrayList<Double> mark;
  int maxH, maxV;

  public DotMatrix(Tuple[] arrT, int maxHor, int maxVer) {
    this.maxH = maxHor;
    this.maxV = maxVer;
    mark = new ArrayList<Double>();
    allSequenz = new ArrayList<Sequenz>();
    createDotMatrix(arrT);
    searchAllSequenzes();
    searchLCS();
  }

  public Tuple[] getTuples() {
    return allSequenz.toArray(new Tuple[] {});
  }

  public Tuple[] getLCS() {
    return LCS;
  }

  public void createDotMatrix(Tuple[] arrT) {
    dotMatrix = new int[this.maxH + 3][this.maxV + 3];
    for (int i = 0; i < arrT.length; i++) {
      dotMatrix[arrT[i].getBaseIndex()][arrT[i].getWitnessIndex()] = 1;
    }
  }

  @SuppressWarnings("boxing")
  public Double getRound(Double d) {
    BigDecimal bigDecimal = new BigDecimal(d);
    bigDecimal = bigDecimal.setScale(1, BigDecimal.ROUND_DOWN);
    return bigDecimal.doubleValue();
  }

  public Double getRound2(Double d) {
    return new Double((String.valueOf(d).substring(0, String.valueOf(d).toString().indexOf(".") + 2)));
  }

  public void searchAllSequenzes() {

    int nH, nV = 0;
    for (int h = 1; h < this.maxH + 3; h++) {
      for (int v = 1; v < this.maxV + 3; v++) {
        if (dotMatrix[h][v] == 1 && !mark.contains(getRound(new Double(h + v * 0.1)))) {
          Sequenz seq = new Sequenz();
          seq.addNext(new Tuple(h, v));
          mark.add(getRound(new Double(h + v * 0.1)));
          nH = h + 1;
          nV = v + 1;
          while (dotMatrix[nH][nV] == 1) {
            seq.addNext(new Tuple(nH, nV));
            mark.add(getRound(new Double(nH + nV * 0.1)));
            nH++;
            nV++;
          }
          allSequenz.add(seq);
        }
      }
    }
    Collections.sort(allSequenz);
    showAllSequenz();
  }

  @SuppressWarnings("unchecked")
  public void searchLCS() {

    int anz = 0;
    Tuple help = new Tuple(0, 0);
    ArrayList<Tuple> seq1 = allSequenz.get(0).getSeq();
    for (int i = 1; i < allSequenz.size(); i++) {
      anz = 0;
      ArrayList<Tuple> seq2 = allSequenz.get(i).getSeq();
      for (int v = 0; v < seq2.size(); v++) {
        if (anz == seq1.size()) {
          anz = 0;
          seq1.add(help);
        }
        for (int h = 0; h < seq1.size(); h++) {
          //System.out.println(seq2.get(v).baseIndex+"<-base->"+seq1.get(h).baseIndex);
          //System.out.println(seq2.get(v).witnessIndex+"<-witn->"+seq1.get(h).witnessIndex);
          if (seq2.get(v).baseIndex == seq1.get(h).baseIndex || seq2.get(v).witnessIndex == seq1.get(h).witnessIndex) {
            break;
          }
          help = seq2.get(v);
          anz++;
        }
      }
    }
    if (anz == seq1.size()) {
      anz = 0;
      seq1.add(help);
    }
    Collections.sort(seq1);
    seq1 = removeSeqenzIrregularities(seq1);
    this.LCS = seq1.toArray(new Tuple[seq1.size()]);
    showLCS();
  }

  public ArrayList<Tuple> removeSeqenzIrregularities(ArrayList<Tuple> seq1) {
    ArrayList<Tuple> revisedSeq1 = new ArrayList<Tuple>();
    revisedSeq1 = seq1;
    int j = 1;
    for (int i = 0; i < seq1.size(); i++) {
      if (j < seq1.size()) {
        if (seq1.get(j).witnessIndex < seq1.get(i).witnessIndex) {
          revisedSeq1.remove(j);
          i--;
        } else {
          j++;
        }
      }
    }
    return revisedSeq1;
  }

  public void showLCS() {
    System.out.print("LCS: ");
    for (Tuple item : this.LCS) {
      System.out.print(item.toString());
    }
    System.out.println();
  }

  public void showAllSequenz() {
    System.out.println();
    int i = 1;
    for (Sequenz seq : allSequenz) {
      System.out.println("Seq " + i + ": " + seq.toString());
      i++;
    }
  }

  @SuppressWarnings("boxing")
  @Override
  public String toString() {
    for (int i = 0; i < this.maxH + 1; i++) {
      dotMatrix[i][0] = i;
    }
    for (int j = 0; j < this.maxV + 1; j++) {
      dotMatrix[0][j] = j;
    }
    for (int i = 0; i < this.maxV + 1; i++) {
      for (int j = 0; j < this.maxH + 1; j++) {
        System.out.printf("%3s", dotMatrix[j][i]);
      }
      System.out.println();
    }
    System.out.println();
    return "done";
  }

}
