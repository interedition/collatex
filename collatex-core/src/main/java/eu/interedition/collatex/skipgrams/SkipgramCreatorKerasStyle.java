package eu.interedition.collatex.skipgrams;

import eu.interedition.collatex.Token;

import java.util.ArrayList;
import java.util.List;

/*
 * author: Ronald Haentjens Dekker
 * Date: 18-10-2018
 */
public class SkipgramCreatorKerasStyle {
    // walk over the squence form left to right
    // from every position in the index we choose a second position from a limited window.

    public List<Skipgram> create(List<Token> sequence, int windowSize) {
        List<Skipgram> skipgrams = new ArrayList<>();
        // ik kan hier gebruik maken van de sublist methode, oh nee toch niet... want dan heb je geen gap.
        for (int start = 0; start < sequence.size(); start++) {
            for (int skip = start+1; skip < Math.min(start+1+windowSize, sequence.size()); skip++) {
//                System.out.println(start+";"+skip);
                Token skipgramHead = sequence.get(start);
                Token skipgramTail = sequence.get(skip);
                Skipgram skipgram = new Skipgram(skipgramHead, skipgramTail);
                skipgrams.add(skipgram);
            }
        }

        return skipgrams;
    }
}
