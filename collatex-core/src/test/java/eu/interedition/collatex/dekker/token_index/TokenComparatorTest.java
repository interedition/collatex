package eu.interedition.collatex.dekker.token_index;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TokenComparatorTest extends AbstractTest {

    @Test
    public void testComparatorWorksAsExpected(){
        TokenIndex.MarkerTokenComparator comparator = new TokenIndex.MarkerTokenComparator(new EqualityTokenComparator());
        SimpleWitness w1 = new SimpleWitness("A");
        SimpleWitness w2 = new SimpleWitness("B");
        SimpleWitness w3 = new SimpleWitness("C");
        TokenIndex.MarkerToken markerToken1 = new TokenIndex.MarkerToken(1);
        TokenIndex.MarkerToken markerToken2 = new TokenIndex.MarkerToken(2);
        TokenIndex.MarkerToken markerToken3 = new TokenIndex.MarkerToken(3);

        List<Token> tokens = new ArrayList<>();
        tokens.add(new SimpleToken(w1,"a","a"));
        tokens.add(markerToken1);

        tokens.add(new SimpleToken(w2,"b","b"));
        tokens.add(markerToken2);

        tokens.add(new SimpleToken(w3,"a","a"));
        tokens.add(new SimpleToken(w3,"b","b"));
        tokens.add(markerToken3);

        System.out.println(tokens);
        Collections.sort(tokens, comparator);
        System.out.println(tokens);
        assert(tokens.get(0).equals(markerToken1));
        assert(tokens.get(1).equals(markerToken2));
        assert(tokens.get(2).equals(markerToken3));
    }
}
