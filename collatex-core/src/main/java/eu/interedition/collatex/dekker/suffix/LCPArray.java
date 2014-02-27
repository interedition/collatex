package eu.interedition.collatex.dekker.suffix;

public class LCPArray {
  public int lcp[];
  
  public LCPArray(String s, TokenSuffixArrayNaive sa) {
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
        while (i+l<n && j+l<n && s.charAt(i+l) == s.charAt(j+l)) l++;
        lcp[k] = l;
        if (l > 0) l = l -1;
      } 
    }
  }
}
