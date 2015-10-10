package eu.interedition.collatex.dekker.token_index;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by ronald on 4/20/15.
 */
public class TokenIndexTest extends AbstractTest {

    class MySpecialToken implements Token {
        private final Witness w;
        protected String specialContent;

        public MySpecialToken(Witness w, String specialContent) {
            this.specialContent = specialContent;
            this.w = w;
        }

        @Override
        public Witness getWitness() {
            return w;
        }

        @Override
        public String toString() {
            return specialContent;
        }
    }

    class MySpecialWitness implements Witness {
        @Override
        public String getSigil() {
            return "special";
        }
    }

    class MySpecialComparator implements Comparator<Token> {
      @Override
        public int compare(Token o1, Token o2) {
            return ((MySpecialToken)o1).specialContent.compareTo(((MySpecialToken)o2).specialContent);
        }
    }

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
        TokenIndex tokenIndex = new TokenIndex(new EqualityTokenComparator(), w);
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
        TokenIndex tokenIndex = new TokenIndex(new EqualityTokenComparator(), w);
        tokenIndex.prepare();
        List<Block> blocks = tokenIndex.splitLCP_ArrayIntoIntervals();
        assertLCP_Interval(3, 1, 3, 3, blocks.get(0)); // a
        assertLCP_Interval(6, 1, 2, 2, blocks.get(1)); // b
        assertLCP_Interval(8, 2, 2, 2, blocks.get(2)); // c d
        assertLCP_Interval(10, 1, 3, 3, blocks.get(3)); // d
        assertLCP_Interval(13, 1, 2, 2, blocks.get(4)); // e
        assertEquals(5, blocks.size());
    }

    @Test
    public void testDepthAndNumberOfTimes() {
        final SimpleWitness[] w = createWitnesses("the a the", "the a");
        TokenIndex tokenIndex = new TokenIndex(new EqualityTokenComparator(), w);
        tokenIndex.prepare();
        List<Block> blocks = tokenIndex.splitLCP_ArrayIntoIntervals();
        assertLCP_Interval(2, 1, 2, 2, blocks.get(0)); // a
        assertLCP_Interval(4, 1, 2, 3, blocks.get(1)); // the
        assertLCP_Interval(5, 2, 2, 2, blocks.get(2)); // the a
        assertEquals(3, blocks.size());
    }

    @Test
    public void testCustomTokensAndComparator() {
        Witness w1 = new MySpecialWitness();
        Token t1 = new MySpecialToken(w1, "interesting");
        Token t2 = new MySpecialToken(w1, "nice");
        Token t3 = new MySpecialToken(w1, "huh");
        Witness w2 = new MySpecialWitness();
        Token t21 = new MySpecialToken(w2, "very");
        Token t22 = new MySpecialToken(w2, "nice");
        Token t23 = new MySpecialToken(w2, "right");
        List<Token> tokens1 = new ArrayList<>();
        tokens1.add(t1);
        tokens1.add(t2);
        tokens1.add(t3);
        List<Token> tokens2 = new ArrayList<>();
        tokens2.add(t21);
        tokens2.add(t22);
        tokens2.add(t23);
        TokenIndex index = new TokenIndex(new MySpecialComparator(), tokens1, tokens2);
        index.prepare();
        assertEquals("[interesting, nice, huh, $1, very, nice, right, $2]", index.token_array.toString());
        Collections.sort(index.token_array, new TokenIndex.MarkerTokenComparatorWrapper(new MySpecialComparator()));
        assertEquals("[$1, $2, huh, interesting, nice, nice, right, very]", index.token_array.toString());
    }
}
