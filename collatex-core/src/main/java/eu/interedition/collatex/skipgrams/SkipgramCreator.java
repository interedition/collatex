package eu.interedition.collatex.skipgrams;

import eu.interedition.collatex.Token;

import java.util.ArrayList;
import java.util.List;

/*
 * Creates skipgrams of size 2 with variable window size
 * This is done in the style of the Keras library.
 *
 * author: Ronald Haentjens Dekker
 * Date: 18-10-2018
 */
public class SkipgramCreator {

    // walk over the squence form left to right
    // from every position in the index we choose a second position from a limited window.

    public List<Skipgram> create(List<Token> sequence, int windowSize) {
        List<Skipgram> skipgrams = new ArrayList<>();
        // ik kan hier gebruik maken van de sublist methode, oh nee toch niet... want dan heb je geen gap.
        for (int start = 0; start < sequence.size(); start++) {
            for (int skip = start+1; skip < Math.min(skip+windowSize, sequence.size()); skip++) {
//                System.out.println(start+";"+skip);
                Token skipgramHead = sequence.get(start);
                Token skipgramTail = sequence.get(skip);
                Skipgram skipgram = new Skipgram(skipgramHead, skipgramTail);
                skipgrams.add(skipgram);
            }
        }

        return skipgrams;
    }

    /*
     * This skipgram creation function creates skipgrams of n tokens... with one or more skips
     * n is the amount of tokens needed for a sequence
     * In the first approach we do not make this flexible
     * s is the maximum amount of skips...
     * This method is simply brute force, and generates all possible combinations..
     */
    public List<NewSkipgram> thirdCreate(List<Token> sequence, int n, int s) {
        // WE IGNORE N FOR NOW! and hard code it..
        // most left token // end maybe must be -1? if we want bigrams...
        List<NewSkipgram> skipgrams = new ArrayList<>();
        for (int start_left = 0; start_left < sequence.size(); start_left++) {
            Token left = sequence.get(start_left);
            // could emit unigrams here... if we want to...
            for (int start_middle = start_left+1; start_middle < Math.min(start_left+1+s, sequence.size()); start_middle++) {
                // we emit bigram here...
                Token middle = sequence.get(start_middle);
                // we put all the tokens in the skipgram head.
                // the tail idea was not so good
                List<Token> skipgramHead = new ArrayList<>();
                skipgramHead.add(left);
                skipgramHead.add(middle);
                NewSkipgram skipgram = new NewSkipgram(skipgramHead);
                skipgrams.add(skipgram);
                for (int start_end = start_middle+1; start_end < Math.min(start_middle+1+s, sequence.size()); start_end++) {
                    // we emit trigram here...
                    Token end = sequence.get(start_end);
                    // we put all the tokens in the skipgram head.
                    // the tail idea was not so good
                    skipgramHead = new ArrayList<>();
                    skipgramHead.add(left);
                    skipgramHead.add(middle);
                    skipgramHead.add(end);
                    skipgram = new NewSkipgram(skipgramHead);
                    skipgrams.add(skipgram);
                }
            }
        }
        return  skipgrams;
    }
}

