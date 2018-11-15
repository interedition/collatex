package eu.interedition.collatex.skipgrams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @author: Ronald Haentjens Dekker
 *
 * @date: 15-11-2018
 * Simple class to gather some statistics about the skipgrams we create...
 */
public class StaticsCollector {
    private int numberOfTokens;
    private int repeats;

    public void gather(List<NewSkipgram> skipgrams) {
        Map<String, Integer>  c = new HashMap<>();
        for (NewSkipgram nskgr : skipgrams) {
            String tokensNormalized = nskgr.getTokensNormalized();

            c.merge(tokensNormalized, 1, (oldValue, one) -> oldValue + one);
        }
        System.out.println(c);
    }
}
