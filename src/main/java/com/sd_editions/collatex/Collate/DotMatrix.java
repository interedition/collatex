package com.sd_editions.collatex.Collate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import com.google.common.collect.Lists;

public class DotMatrix {

  private final ArrayList<Sequenz> allSequenz;
  private Tuple[] LCS = new Tuple[] {};
  private int[][] dotMatrix;
  private final ArrayList<Double> mark;
  private final int maxH, maxV;

  public DotMatrix(Tuple[] arrT, int maxHor, int maxVer) {
    this.maxH = maxHor;
    this.maxV = maxVer;
    mark = Lists.newArrayList();
    allSequenz = Lists.newArrayList();
    createDotMatrix(arrT);
    searchAllSequenzes();
    //searchAllSequenzesRtoL();
    if (!allSequenz.isEmpty()) {
      beforeSearchLCS();
      searchLCS();
      locateSeqOfTransposTupel();
    }
  }

  public Tuple[] getLCS() {
    return LCS;
  }

  public void createDotMatrix(Tuple[] arrT) {
    dotMatrix = new int[this.maxH + 2][this.maxV + 2];
    for (int i = 0; i < arrT.length; i++) {
      dotMatrix[arrT[i].getBaseIndex()][arrT[i].getWitnessIndex()] = 1;
    }
  }

  @SuppressWarnings("boxing")
  public Double getRound2(Double d) {
    BigDecimal bigDecimal = new BigDecimal(d);
    bigDecimal = bigDecimal.setScale(1, BigDecimal.ROUND_DOWN);
    return d;
  }

  public Double getRound(Double d) {
    Double newD = new Double((String.valueOf(d).substring(0, String.valueOf(d).toString().indexOf(".") + 2)));
    return newD;
  }

  public void searchAllSequenzes() {

    int anz = 1;
    int nH, nV = 0;
    boolean merk = true;
    for (int h = 1; h < this.maxH + 1; h++) {
      for (int v = 1; v < this.maxV + 1; v++) {
        if (dotMatrix[h][v] == 1 && !mark.contains(getRound(new Double(h + v * 0.1)))) {
          Sequenz seq = new Sequenz();
          seq.addNext(new Tuple(h, v));
          mark.add(getRound(new Double(h + v * 0.1)));
          nH = h + 1;
          nV = v + 1;
          while (dotMatrix[nH][nV] == 1 && (nH <= maxH || nV <= maxV)) {
            seq.addNext(new Tuple(nH, nV));
            mark.add(getRound(new Double(nH + nV * 0.1)));
            nH++;
            nV++;
          }
          if (seq.getSize() >= 1) {
            allSequenz.add(seq);
          }
          Sequenz seq2 = new Sequenz();
          nH = h + 1;
          nV = v - 1;

          //All Sequences from Right-up to Left-down too
          //(detect Transposition Tuple)
          if (nV >= 1) {
            if (dotMatrix[nH][nV] == 1) {
              merk = true;
            }
            while (dotMatrix[nH][nV] == 1 && nH <= maxH) {
              if (merk) {
                merk = false;
                Tuple tup = new Tuple(h, v);
                tup.markAsTransposTupel();
                seq2.addNext(tup);
                tup.markAsTransposTupel();
              }
              Tuple tup2 = new Tuple(nH, nV);
              tup2.markAsTransposTupel();
              seq2.addNext(tup2);
              mark.add(getRound(new Double(nH + nV * 0.1)));
              nH++;
              nV--;
            }
          }
          if (seq2.getSize() > 1) {
            System.out.println("TransPosSeq " + anz + ": " + seq2.toString());
            //rearrangeTransposTupel(seq2);
            seq2.setTransposSeqValue();
            allSequenz.add(seq2);
            anz++;
          }
        }
      }
    }
    Collections.sort(allSequenz);
    showAllSequenz();
    //System.out.println("markArray: " + mark.toString());
  }

  public void locateSeqOfTransposTupel() {
    Sequenz transposSeq = new Sequenz();
    for (int i = 0; i < this.LCS.length; i++) {
      int startPos = i;
      while (this.LCS[i].isTransposTupel()) {
        if (i < LCS.length - 1 && (this.LCS[i].baseIndex) + 1 == this.LCS[i + 1].baseIndex && (this.LCS[i].witnessIndex) - 1 == this.LCS[i + 1].witnessIndex) {
          transposSeq.addNext(this.LCS[i]);
        } else {
          transposSeq.addNext(this.LCS[i]);
          //System.out.println("locateSeqOfTransposTupel:128" + this.LCS[i]);
          break;
        }
        i++;
        if (i >= LCS.length - 1 && this.LCS[i].isTransposTupel()) {
          transposSeq.addNext(this.LCS[i]);
          break;
        }
      }
      if (transposSeq.getSize() > 0) {
        //System.out.println("transposSeq: " + transposSeq.toString());
        transposSeq = rearrangeTransposTupelSeq(transposSeq);
        for (int j = startPos, anz = 0; anz < transposSeq.getSize(); j++, anz++) {
          this.LCS[j] = transposSeq.getSeq().get(anz);
        }
      }
      transposSeq = new Sequenz();
    }
    showLCS("T");
  }

  //rearrange witness positions and mark Tupels as TransPosTup
  private Sequenz rearrangeTransposTupelSeq(Sequenz seq2) {
    ArrayList<Tuple> arrLTransposTup = new ArrayList<Tuple>();
    int count = 0;
    if (seq2.getSize() % 2 == 1) {
      for (int firstTup = 0, lastTup = seq2.getSize() - 1; firstTup < (seq2.getSize() - 1) / 2; firstTup++, lastTup--) {
        Tuple nTup = new Tuple(seq2.getSeq().get(firstTup).baseIndex, seq2.getSeq().get(lastTup).witnessIndex);
        arrLTransposTup.add(nTup);
        Tuple nTup2 = new Tuple(seq2.getSeq().get(lastTup).baseIndex, seq2.getSeq().get(firstTup).witnessIndex);
        arrLTransposTup.add(nTup2);
        count = firstTup;
      }
      if (seq2.getSize() == 1) {
        Tuple nTup = new Tuple(seq2.getSeq().get(0).baseIndex, seq2.getSeq().get(0).witnessIndex);
        arrLTransposTup.add(nTup);
      } else {
        Tuple nTup = seq2.getSeq().get(count + 1);
        arrLTransposTup.add(nTup);
      }

    } else {
      for (int firstTup = 0, lastTup = seq2.getSize() - 1; firstTup < seq2.getSize() / 2; firstTup++, lastTup--) {
        Tuple nTup = new Tuple(seq2.getSeq().get(firstTup).baseIndex, seq2.getSeq().get(lastTup).witnessIndex);
        arrLTransposTup.add(nTup);
        Tuple nTup2 = new Tuple(seq2.getSeq().get(lastTup).baseIndex, seq2.getSeq().get(firstTup).witnessIndex);
        arrLTransposTup.add(nTup2);
      }
    }
    //System.out.println("arrLTransposTup: " + arrLTransposTup.toString());
    Collections.sort(arrLTransposTup);
    Sequenz seq2Trans = new Sequenz();
    if (seq2.getSize() == 1) {
      for (Tuple tuple : arrLTransposTup) {
        Tuple tup = tuple;
        seq2Trans.addNext(tup);
        //System.out.println("seq2Trans Tup: " + i + " :" + seq2Trans.getSeq().get(i).isTransposTupel());
      }
    } else {
      int i = 0;
      for (Tuple tuple : arrLTransposTup) {
        Tuple tup = tuple;
        tup.markAsTransposTupel();
        seq2Trans.addNext(tup);
        //System.out.println("seq2Trans Tup: " + i + " :" + seq2Trans.getSeq().get(i).isTransposTupel());
        i++;
      }
      //allSequenz.add(seq2Trans);
    }

    return seq2Trans;
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
    showAllSequenz();
  }

  public void removeIdleSequenz() {
    for (int i = 0; i < allSequenz.size(); i++) {
      if (allSequenz.get(i).getSeq().isEmpty()) {
        allSequenz.remove(i);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void searchLCS() {

    boolean takeAllSeqTupIfFirstOK = false;
    int count = 0;
    Tuple help = new Tuple(0, 0);
    ArrayList<Tuple> seq1 = allSequenz.get(0).getSeq();
    Sequenz longestSeq = new Sequenz();
    longestSeq.addTupelArray(seq1.toArray(new Tuple[seq1.size()]));
    for (int i = 1; i < allSequenz.size(); i++) {
      ArrayList<Tuple> seq2 = allSequenz.get(i).getSeq();
      //System.out.println("seq2: " + seq2);
      //System.out.println("longestSeq: " + longestSeq);
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
        if (count == longestSeq.getSize() && help.baseIndex != 0 || takeAllSeqTupIfFirstOK) {
          count = 0;
          takeAllSeqTupIfFirstOK = true;
          seq1.add(help);
          longestSeq.addNext(help);
        }
      }
      takeAllSeqTupIfFirstOK = false;
    }
    Collections.sort(seq1);
    //System.out.println("LCS_vor_Sort: " + seq1.toString());
    this.LCS = seq1.toArray(new Tuple[seq1.size()]);
    Comparator<Tuple> byBaseIndex = new TupelComparatorBI();
    Arrays.sort(LCS, byBaseIndex);
    showLCS("-");
  }

  public void showLCS(String opt) {
    System.out.print("LCS: " + opt + " : ");
    for (Tuple tup : this.LCS) {
      //System.out.print(tup.toString() + " " + tup.isTransposTupel() + " ");
      System.out.print(tup.toString());
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
    System.out.println();
  }

  @SuppressWarnings("boxing")
  @Override
  public String toString() {
    System.out.println();
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
      System.out.println("");
    }
    System.out.println("================================================================================");
    return "done";
  }

}
