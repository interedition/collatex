package eu.interedition.collatex.dekker.suffix;

public class Utils {

  public static void debug(TokenSuffixArrayNaive sa) {
    for (int i = 0; i < sa.len; i++) {
      System.out.print(sa.a[i] + ",");
    }
    System.out.println();
  }
  
  public static void debug(LCPArray lcp) {  
    for (int i = 0; i < lcp.lcp.length; i++) {
      System.out.print(lcp.lcp[i] + ",");
    }
    System.out.println();
  }
}
