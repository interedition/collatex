package eu.interedition.collatex.dekker;

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

    private void assertLCP_Interval(int start, int length, int depth, Block lcp_interval) {
        assertEquals(start, lcp_interval.start);
        assertEquals(length, lcp_interval.length);
        assertEquals(depth, lcp_interval.depth());
    }


    @Test
    public void testCaseDanielStoekl() {
        // 1: a, b, c, d, e
        // 2: a, e, c, d
        // 3: a, d, b
        final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d", "a d b");
        Dekker21Aligner aligner = new Dekker21Aligner(w);
        //Note: the suffix array can have multiple forms
        //outcome of sorting is not guaranteed
        //however the LCP array is fixed we can assert that
        assertEquals("[-1, 1, 1, 0, 1, 0, 2, 0, 1, 1, 0, 1]", Arrays.toString(aligner.tokenIndex.LCP_array));
    }

    @Test
    public void testCaseDanielStoeklLCPIntervals() {
        // 1: a, b, c, d, e
        // 2: a, e, c, d
        // 3: a, d, b
        final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d", "a d b");
        Dekker21Aligner aligner = new Dekker21Aligner(w);
        List<Block> blocks = aligner.tokenIndex.splitLCP_ArrayIntoIntervals();
        assertLCP_Interval(0, 1, 3, blocks.get(0)); // a
        assertLCP_Interval(3, 1, 2, blocks.get(1)); // b
        assertLCP_Interval(5, 2, 2, blocks.get(2)); // c d
        assertLCP_Interval(7, 1, 3, blocks.get(3)); // d
        assertLCP_Interval(10, 1, 2, blocks.get(4)); // e
        assertEquals(5, blocks.size());
    }


}
