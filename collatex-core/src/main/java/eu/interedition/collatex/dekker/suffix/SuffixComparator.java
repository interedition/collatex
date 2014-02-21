package eu.interedition.collatex.dekker.suffix;

import java.util.Comparator;

class SuffixComparator implements Comparator<Integer> {
  String s;

  public int compare(Integer o, Integer p) {
    String str1 = s.substring((o).intValue());
    String str2 = s.substring((p).intValue());
    return str1.compareTo(str2);
  }

  SuffixComparator(String str) {
    s = str;
  }
}
