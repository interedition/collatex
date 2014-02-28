package eu.interedition.collatex.dekker.suffix;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

/*
* @author: Ronald Haentjens Dekker
*/
public class SuperMaximumRepeats {

  public static List<Range<Integer>> calculateBlocks(TokenSuffixArrayNaive sa, LCPArray lcp) {
    int bestFound;
    RangeSet<Integer> occupied = TreeRangeSet.create();
    List<Range<Integer>> result = Lists.newArrayList();
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
        // add ranges to result
        result.add(p1);
        result.add(p2);
      }
    } while(bestFound!=0);
    return result;
  }
}
