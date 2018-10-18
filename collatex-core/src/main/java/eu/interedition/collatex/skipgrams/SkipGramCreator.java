package eu.interedition.collatex.skipgrams;

import eu.interedition.collatex.Token;

import java.util.ArrayList;
import java.util.List;

/*
 * This is the old style  of Skipgram creator
 * and it does not work right...
 * I would rather have the keras way of dealing with things
 */
public class SkipGramCreator {

    public ArrayList<List<Token>> createSkipGrams(List<Token> tokens, int size, int distance) {
        NgramCreator ngramCreator = new NgramCreator();
        ArrayList<List<Token>> ngrams = ngramCreator.create(tokens, size);

        ArrayList<List<Token>> skipgrams = new ArrayList<>();

        int n = ngrams.size();
        for (int i = 0; i < n; i++) {
            int end = i + 1 + distance;

            for (int pos = i + 1; pos < end; pos++) {
                if (pos < n) {
                    //TODO: pair is not really the right word here!
                    List<Token> pair = new ArrayList<>();
                    pair.addAll(ngrams.get(i));
                    pair.addAll(ngrams.get(pos));

                    skipgrams.add(pair);
                } else {
                    break;
                }
            }
        }

        return skipgrams;
    }
}
