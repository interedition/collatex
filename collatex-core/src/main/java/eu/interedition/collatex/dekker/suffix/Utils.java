package eu.interedition.collatex.dekker.suffix;

import java.util.List;

import com.google.common.collect.Lists;

public class Utils {

  public static void debug(TokenSuffixArrayNaive sa) {
    for (int i = 0; i < sa.len; i++) {
      System.out.print(sa.a[i] + ",");
    }
    System.out.println();
  }
  
  public static void debugPrefixes(TokenSuffixArrayNaive sa, Sequence s) {
    // transform the sa array into a list of prefixes...
    List<Sequence> suffixes = Lists.newArrayList();
    for (int i=0; i< sa.length(); i++) {
      int suffixIndex = sa.get(i);
      suffixes.add(s.subsequence(suffixIndex));
    }
    System.out.println(suffixes);
  }
  
  public static void debug(LCPArray lcp) {  
    for (int i = 0; i < lcp.lcp.length; i++) {
      System.out.print(lcp.lcp[i] + ",");
    }
    System.out.println();
  }
}
