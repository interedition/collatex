package eu.interedition.collatex.dekker.suffix;

import java.util.Comparator;

class TokenSuffixComparator implements Comparator<Integer> {
  Sequence s;

  public int compare(Integer o, Integer p) {
    Sequence str1 = s.subsequence(o.intValue());
    Sequence str2 = s.subsequence(p.intValue());
    return str1.compareTo(str2);
  }

  TokenSuffixComparator(Sequence s) {
    this.s = s;
  }
}
