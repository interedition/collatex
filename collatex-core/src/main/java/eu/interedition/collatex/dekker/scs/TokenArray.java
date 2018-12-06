package eu.interedition.collatex.dekker.scs;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.util.StreamUtil;

import java.util.*;

/*
 * TokenArray
 *
 * @author: Ronald Haentjens Dekker
 *
 * Taken from TokenIndex code from 2014.
 *
 * Code that creates a token array out of a bunch of witnesses..
 *
 * This is later used for suffix array creation
 * Also this can be used in a bit array to see whether a token is already covered.
 *
 * we can also this array to create a list of list for the token to List<subsequence> mapping
 *
 *
 */
public class TokenArray {
    protected Map<Witness, Integer> witnessToStartToken;
    // TODO: could be removed!
    private Map<Witness, Integer> witnessToEndToken;
    public Token[] token_array;


    protected Token[] prepareTokenArray(List<? extends Iterable<Token>> witnesses) {
        List<Token> tempTokenList = new ArrayList<>();
        int counter = 0;
        witnessToStartToken = new HashMap<>();
        witnessToEndToken = new HashMap<>();
        for (Iterable<Token> tokens : witnesses) {
            final Witness witness = StreamUtil.stream(tokens)
                .findFirst()
                .map(Token::getWitness)
                .orElseThrow(() -> new IllegalArgumentException("Empty witness"));

            witnessToStartToken.put(witness, counter);
            for (Token t : tokens) {
                tempTokenList.add(t);
                counter++;
            }
            witnessToEndToken.put(witness, counter);
            tempTokenList.add(new MarkerToken(witnessToStartToken.size()));
            counter++;
        }
        return tempTokenList.toArray(new Token[tempTokenList.size()]);
    }

//    public int numberOfWitnesses() {
//        return witnessToStartToken.size();
//    }
//
//    // note this is in random order; this should not matter for the rest of the code.
//    public Set<Witness> witnesses() {
//        return witnessToStartToken.keySet();
//    }
//
//    public Iterator<Token> iteratorForWitness(Witness w) {
//        int startIndex = witnessToStartToken.get(w);
//        int endIndex = witnessToEndToken.get(w);
//
//        //TODO: this can be done much more efficiently
//        Token[] tokens = Arrays.copyOfRange(token_array, startIndex, endIndex);
//        return Arrays.asList(tokens).iterator();
//    }
//
//    public int startIndexForWitness(Witness w) {
//        return witnessToStartToken.get(w);
//    }

    public static class MarkerToken implements Token {
        private final int witnessIdentifier;

        public MarkerToken(int size) {
            this.witnessIdentifier = size;
        }

        @Override
        public String toString() {
            return "$" + witnessIdentifier;
        }

        @Override
        public Witness getWitness() {
            throw new RuntimeException("A marker token is not part of any witness! The call to this method should never have happened!");
        }
    }

    public static class MarkerTokenComparator implements Comparator<Token> {
        private Comparator<Token> delegate;

        public MarkerTokenComparator(Comparator<Token> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int compare(Token o1, Token o2) {
            boolean o1isMarker = o1 instanceof MarkerToken;
            boolean o2isMarker = o2 instanceof MarkerToken;
            if (o1isMarker) {
                // Both o1 and o2 could be marker tokens
                if (o2isMarker) {
                    MarkerToken mt1 = (MarkerToken) o1;
                    MarkerToken mt2 = (MarkerToken) o2;
                    // sort marker tokens from low to high
                    return mt1.witnessIdentifier - mt2.witnessIdentifier;
                }
                // or one of them could be a marker token
                // always put the marker token before the content
                return -1;
            }
            // or one of them could be a marker token
            // always put the content after the marker token
            if (o2isMarker) {
                return 1;
            }
            // Not a marker token; call delegate
            return delegate.compare(o1, o2);
        }
    }

//    // a skipgram is a token vector
//    // but then a special one, where a couple of vectors are masked out
//
//
//    static class TokenVector {
//        private final TokenArray array;
//        private final int start;
//        private final int length;
//
//        TokenVector(TokenArray array, int start, int length) {
//            this.array = array;
//            this.start = start;
//            this.length = length;
//        }
//    }
}