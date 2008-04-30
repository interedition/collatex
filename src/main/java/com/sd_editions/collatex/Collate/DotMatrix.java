package com.sd_editions.collatex.Collate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class DotMatrix {

  private ArrayList<Sequenz> allSequenz;
  private Tuple[] LCS = new Tuple[] {};
  private int[][] dotMatrix;
  private ArrayList<Double> mark;
  private int maxH, maxV;

  public DotMatrix(Tuple[] arrT, int maxHor, int maxVer) {
    this.maxH = maxHor;
    this.maxV = maxVer;
    mark = new ArrayList<Double>();
    allSequenz = new ArrayList<Sequenz>();
    createDotMatrix(arrT);
    searchAllSequenzes();
    if (!allSequenz.isEmpty()) {
      beforeSearchLCS();
      searchLCS();
    }
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

  //remove matched Tuple in allSequenz from index 1 to end before call searchLCS()
  //and remodel allSequenz
  public void beforeSearchLCS() {

    boolean merk = false;
    ArrayList<Tuple> seq1 = allSequenz.get(0).getSeq();
    for (int i = 1; i < allSequenz.size(); i++) {
      if (merk) {
        i--;
      }
      merk = false;
      Collections.sort(allSequenz);
      removeIdleSequenz();
      ArrayList<Tuple> seq2 = allSequenz.get(i).getSeq();
      for (int v = 0; v < seq2.size(); v++) {
        if (merk) {
          merk = false;
          i--;
          break;
        }
        for (int h = 0; h < seq1.size(); h++) {
          if (seq2.get(v).baseIndex == seq1.get(h).baseIndex || seq2.get(v).witnessIndex == seq1.get(h).witnessIndex) {
            allSequenz.get(i).getSeq().remove(v);
            merk = true;
            break;
          }
        }
      }
    }
    Collections.sort(allSequenz);
    removeIdleSequenz();
  }

  public void removeIdleSequenz() {
    for (int i = 0; i < allSequenz.size(); i++) {
      if (allSequenz.get(i).getSeq().isEmpty()) {
        allSequenz = new ArrayList<Sequenz>(allSequenz.subList(0, i));
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void searchLCS() {

    int count = 0;
    Tuple help = new Tuple(0, 0);
    ArrayList<Tuple> seq1 = allSequenz.get(0).getSeq();
    Sequenz longestSeq = new Sequenz();
    longestSeq.addTupelArray(seq1.toArray(new Tuple[seq1.size()]));
    for (int i = 1; i < allSequenz.size(); i++) {
      ArrayList<Tuple> seq2 = allSequenz.get(i).getSeq();
      for (int v = 0; v < seq2.size(); v++) {
        if (count == longestSeq.getSize() && help.baseIndex != 0) {
          seq1.add(help);
          longestSeq.addNext(help);
        }
        count = 0;
        help = seq2.get(v);
        for (int h = 0; h < seq1.size(); h++) {
          if ((seq2.get(v).baseIndex > seq1.get(h).baseIndex) && (seq2.get(v).witnessIndex > seq1.get(h).witnessIndex) || (seq2.get(v).baseIndex < seq1.get(h).baseIndex)
              && (seq2.get(v).witnessIndex < seq1.get(h).witnessIndex)) {
            count++;
          }
        }
      }
      if (count == longestSeq.getSize() && help.baseIndex != 0) {
        count = 0;
        seq1.add(help);
        longestSeq.addNext(help);
      }

    }
    Collections.sort(seq1);
    System.out.println("LCS_vor: " + seq1.toString());
    this.LCS = seq1.toArray(new Tuple[seq1.size()]);
    Comparator<Tuple> byBaseIndex = new TupelComparatorBI();
    Arrays.sort(LCS, byBaseIndex);
    showLCS();
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
