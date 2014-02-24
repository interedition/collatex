package eu.interedition.collatex.dekker;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import eu.interedition.collatex.dekker.suffix.LCPArray;
import eu.interedition.collatex.dekker.suffix.SuffixArrayNaive;
import eu.interedition.collatex.dekker.suffix.Utils;

public class SuffixTest {

  // first we find the suffixes for one witness
  @Ignore
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
  
  @Test
  public void testHermansWitnessOrder() {
    //TODO: use the tokens instead of a String
    //W1: a b c d F g h i ! K ! q r s t
    //W2: a b c d F g h i ! q r s t
    String testcase = "abcdFghi!K!qrst$abcdFghi!qrst";
    SuffixArrayNaive sa = new SuffixArrayNaive(testcase);
    Utils.debug(sa);
    
    LCPArray lcp = new LCPArray(testcase, sa);
    Utils.debug(lcp);
    
    int bestFound;
    RangeSet<Integer> occupied = TreeRangeSet.create();
    do {
      bestFound=0;
      int bestPosition=0;
      for (int i=0; i< lcp.lcp.length; i++) {
        if (lcp.lcp[i]>bestFound) {
          bestFound = lcp.lcp[i];
          bestPosition = i;
        }
      }
      if (bestPosition!=0) {
        int piece1 = sa.get(bestPosition-1);
        int piece2 = sa.get(bestPosition);
        Range<Integer> p1 = Range.closed(piece1, piece1+bestFound-1);
        Range<Integer> p2 = Range.closed(piece2, piece2+bestFound-1);
        // clear highest value
        lcp.lcp[bestPosition-1]=0;
        lcp.lcp[bestPosition]=0;
        // check whether to add
        if (occupied.contains(p1.lowerEndpoint())||occupied.contains(p1.upperEndpoint())) {
          continue;
        } 
        if (occupied.contains(p2.lowerEndpoint())||occupied.contains(p2.upperEndpoint())) {
          continue;
        }
        System.out.println(String.format("piece1: %s piece2: %s length: %s", p1, p2, bestFound));
        // add to rangeset
        occupied.add(p1);
        occupied.add(p2);
      }
    } while(bestFound!=0);
  }

}
