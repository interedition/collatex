package eu.interedition.collatex.dekker.suffix;

import java.util.Arrays;

public class SuffixArrayNaive {
  int len;
  int[] a;

  public SuffixArrayNaive(String s) {
    len = s.length();
    SuffixComparator comp = new SuffixComparator(s);
    Integer[] suffixes = new Integer[len];
    for (int i = 0; i < len; i++) suffixes[i] = new Integer(i);
    Arrays.sort(suffixes, comp); // build suffix array the naive way
    a = new int[len];
    for (int i = 0; i < len; i++) a[i] = suffixes[i].intValue();
  }

  public int get(int i) {
    return a[i];
  }
}
