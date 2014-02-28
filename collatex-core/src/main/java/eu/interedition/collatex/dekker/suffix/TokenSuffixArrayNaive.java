package eu.interedition.collatex.dekker.suffix;

import java.util.Arrays;

/*
 * Naive implementation of a token based suffix array
 * 
 * @author: Ronald Haentjens Dekker
 */
public class TokenSuffixArrayNaive {
  int len;
  int[] a;

  public TokenSuffixArrayNaive(Sequence s) {
    len = s.length();
    TokenSuffixComparator comp = new TokenSuffixComparator(s);
    Integer[] suffixes = new Integer[len];
    for (int i = 0; i < len; i++) suffixes[i] = new Integer(i);
    Arrays.sort(suffixes, comp); // build suffix array the naive way
    a = new int[len];
    for (int i = 0; i < len; i++) a[i] = suffixes[i].intValue();
  }

  public int get(int i) {
    return a[i];
  }
  
  public boolean arrayEquals(int[] other) {
    return Arrays.equals(a, other);
  }

  public int length() {
    return len;
  }
}
