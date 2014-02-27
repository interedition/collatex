package eu.interedition.collatex.dekker.suffix;

import java.util.Arrays;
import java.util.Comparator;

import eu.interedition.collatex.Token;

public class LCPArray {
  public int lcp[];
  
  public LCPArray(Sequence s, TokenSuffixArrayNaive sa, Comparator<Token> tokenComparator) {
    int[] a = sa.a;
    int n = s.length();
    lcp = new int[n];

    // build inverse suffix array I:
    int[] I = new int[n];
    for (int i = 0; i < n; i++) I[a[i]] = i;

    // build LCP:
    int l = 0; lcp[0] = 0;
    for (int i = 0; i < n; i++) {
      int k = I[i];
      if (k == 0) {
        lcp[k] = -1;
      } else {  
        int j = a[k-1];
        while (i+l<n && j+l<n && tokenComparator.compare(s.tokenAt(i+l), s.tokenAt(j+l))==0) l++;
        lcp[k] = l;
        if (l > 0) l = l -1;
      } 
    }
  }

  public boolean arrayEquals(int[] other) {
    return Arrays.equals(lcp, other);
  }
}
