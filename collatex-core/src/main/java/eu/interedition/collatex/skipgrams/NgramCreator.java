package eu.interedition.collatex.skipgrams;

import eu.interedition.collatex.Token;

import java.util.ArrayList;
import java.util.List;

/*
 * Create Ngrams from a list of tokens
 *
 * 16-10-2018
 *
 * Created by Ronald Haentjens Dekker
 * Inspired by public open source n-gram code.
 */
public class NgramCreator {
    // I think no state is needed.

    public ArrayList<List<Token>> create(List<Token> tokens, int n) {
        ArrayList<List<Token>> ngrams = new ArrayList<>();

        int c = tokens.size();
        for (int i = 0; i < c; i++) {
            if ((i + n - 1) < c) {
                int stop = i + n;
                List<Token> ngramWords = new ArrayList<>();
                ngramWords.add(tokens.get(i));

                for (int j = i + 1; j < stop; j++) {
                    ngramWords.add(tokens.get(j));
                }

                ngrams.add(ngramWords);
            }
        }

        return ngrams;
    }
}
