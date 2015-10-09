package eu.interedition.collatex.dekker.experimental_aligner;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by ronald on 4/20/15.
 */
public class TokenIndexTest extends AbstractTest {

    private void assertLCP_Interval(int start, int length, int depth, int numberOfTimes, Block lcp_interval) {
        assertEquals(lcp_interval.toString(), start, lcp_interval.start);
        assertEquals(lcp_interval.toString(), length, lcp_interval.length);
        assertEquals(lcp_interval.toString(), depth, lcp_interval.depth);
        assertEquals(lcp_interval.toString(), numberOfTimes, lcp_interval.numberOfTimes());
    }


    @Test
    public void testCaseDanielStoekl() {
        // 1: a, b, c, d, e
        // 2: a, e, c, d
        // 3: a, d, b
        final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d", "a d b");
        TokenIndex tokenIndex = new TokenIndex(w);
        tokenIndex.prepare();
        //Note: the suffix array can have multiple forms
        //outcome of sorting is not guaranteed
        //however the LCP array is fixed we can assert that
        assertEquals("[-1, 0, 0, 0, 1, 1, 0, 1, 0, 2, 0, 1, 1, 0, 1]", Arrays.toString(tokenIndex.LCP_array));
    }

    @Test
    public void testCaseDanielStoeklLCPIntervals() {
        // 1: a, b, c, d, e
        // 2: a, e, c, d
        // 3: a, X, X, d, b
        final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d", "a d b");
        TokenIndex tokenIndex = new TokenIndex(w);
        tokenIndex.prepare();
        List<Block> blocks = tokenIndex.splitLCP_ArrayIntoIntervals();
        assertLCP_Interval(3, 1, 3, 3, blocks.get(0)); // a
        assertLCP_Interval(6, 1, 2, 2, blocks.get(1)); // b
        assertLCP_Interval(8, 2, 2, 2, blocks.get(2)); // c d
        assertLCP_Interval(10, 1, 3, 3, blocks.get(3)); // d
        assertLCP_Interval(0, 0, 4, 15, blocks.get(4)); // TODO: remove!
        assertLCP_Interval(13, 1, 2, 2, blocks.get(5)); // e
        assertEquals(6, blocks.size());
    }

    @Test
    public void testDepthAndNumberOfTimes() {
        final SimpleWitness[] w = createWitnesses("the a the", "the a");
        TokenIndex tokenIndex = new TokenIndex(w);
        tokenIndex.prepare();
        List<Block> blocks = tokenIndex.splitLCP_ArrayIntoIntervals();
        assertLCP_Interval(2, 1, 2, 2, blocks.get(0)); // a
        assertLCP_Interval(0, 0, 3, 7, blocks.get(1)); // TODO: remove!
        assertLCP_Interval(4, 1, 2, 3, blocks.get(2)); // the
        assertLCP_Interval(5, 2, 2, 2, blocks.get(3)); // the a
        assertEquals(4, blocks.size());
    }

}
