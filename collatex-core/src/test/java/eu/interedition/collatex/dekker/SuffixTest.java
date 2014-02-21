package eu.interedition.collatex.dekker;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex.dekker.suffix.LCPArray;
import eu.interedition.collatex.dekker.suffix.SuffixArrayNaive;
import eu.interedition.collatex.dekker.suffix.Utils;

public class SuffixTest {

  // first we find the suffixes for one witness
  @Test
  public void testHI() {
    String a = "a b c d a b";
    String[] t = a.split("\\s+");
    List<String> tokens = Lists.newArrayList(t);
    System.out.println(tokens);
    
    //TODO: use the tokens instead of a String
    SuffixArrayNaive sa = new SuffixArrayNaive("abcdab");
    Utils.debug(sa);
    
    LCPArray lcp = new LCPArray("abcdab", sa);
    Utils.debug(lcp);
  }
}
