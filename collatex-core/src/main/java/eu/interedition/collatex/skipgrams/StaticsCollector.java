package eu.interedition.collatex.skipgrams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * @author: Ronald Haentjens Dekker
 *
 * @date: 15-11-2018
 * Simple class to gather some statistics about the skipgrams we create...
 */
public class StaticsCollector {
    private int numberOfTokens;
    private int repeats;
    private Map<String, Integer> c;

    public void gather(List<NewSkipgram> skipgrams) {
        c = new HashMap<>();
        for (NewSkipgram nskgr : skipgrams) {
            String tokensNormalized = nskgr.getTokensNormalized();

            c.merge(tokensNormalized, 1, (oldValue, one) -> oldValue + one);
        }
//        System.out.println(c);
    }

    public List<Map.Entry<String, Integer>> getAllTheNormalizedSkipgramsThatOccurMoreThanOnce() {
        List<Map.Entry<String,Integer>> theOnesWeNeed = c.entrySet().stream().filter(e -> e.getValue() > 1).collect(Collectors.toList());
//        System.out.println(theOnesWeNeed);
        return theOnesWeNeed;
    }
}
