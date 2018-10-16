package eu.interedition.collatex.skipgrams;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/*
 16-10-2018
 We create n-grams from Tokens of one Witness
 From that we move on to skipgrams of one witness
 Then skipgrams for multiple witnesses and so on and so on.
 */
public class SkipGramMWATest {

    /*
     *
     * We tokenize one witness
     * Then we create b-grams out of them
     */
    @Test
    public void testNGramsOneWitness() {
        String firstWitness = "a b c d e ";
        SimpleWitness w1 = new SimpleWitness("w1", "a b c d e");
        List<Token> tokens = w1.getTokens();
        System.out.println(tokens);
        NgramCreator c = new NgramCreator();
        ArrayList<List<Token>> ngrams = c.create(tokens, 2);
        System.out.println(ngrams);
    }
}
