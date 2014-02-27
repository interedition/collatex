package eu.interedition.collatex.dekker;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.suffix.LCPArray;
import eu.interedition.collatex.dekker.suffix.Sequence;
import eu.interedition.collatex.dekker.suffix.TokenSuffixArrayNaive;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleToken;

public class SuffixTest {

  // first we find the suffixes for one witness
  @Test
  public void testSuffixArrayAndLCPArray() {
    String a = "a b c d a b";
    List<Token> tokens = createSequenceFromString(a);
    Sequence s = new Sequence(tokens, new EqualityTokenComparator());
    TokenSuffixArrayNaive sa = new TokenSuffixArrayNaive(s);
    assertTrue(sa.arrayEquals(new int[] { 0, 4, 1, 5, 2, 3 }));
    LCPArray lcp = new LCPArray(s, sa, new EqualityTokenComparator());
    assertTrue(lcp.arrayEquals(new int[] { -1, 2, 0, 1, 0, 0 }));
  }

  private List<Token> createSequenceFromString(String a) {
    String[] t = a.split("\\s+");
    List<String> tokens = Lists.newArrayList(t);
    // have to convert a list of strings into a sequence/list of tokens
    // I would like it better if token was just a generic type
    List<Token> stokens = Lists.newArrayList();
    for (String to : tokens) {
      stokens.add(new SimpleToken(null, to, to));
    }
    return stokens;
  }
  
//  @Test
//  public void testHermansWitnessOrder() {
//    //TODO: use the tokens instead of a String
//    //W1: a b c d F g h i ! K ! q r s t
//    //W2: a b c d F g h i ! q r s t
//    String testcase = "abcdFghi!K!qrst$abcdFghi!qrst";
//    TokenSuffixArrayNaive sa = new TokenSuffixArrayNaive(testcase);
//    Utils.debug(sa);
//    
//    LCPArray lcp = new LCPArray(testcase, sa);
//    Utils.debug(lcp);
//    
//    NonOverlappingRepeatableBlocksBuilder.calculateBlocks(sa, lcp);
//  }

}
