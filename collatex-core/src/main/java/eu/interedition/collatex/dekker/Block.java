package eu.interedition.collatex.dekker;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleToken;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Block {
    // every Block has a token index as a parent
    private final TokenIndex tokenIndex;
    // length = number of tokens in this block of text
    int length;
    // start = start position in suffix array
    int start;
    // end = end position in suffix array
    int end;

    public Block(TokenIndex tokenIndex, int suffix_start_position, int length) {
        this.tokenIndex = tokenIndex;
        this.start = suffix_start_position;
        this.length = length;
    }

    public Block(TokenIndex tokenIndex, int start, int end, int length) {
        this.tokenIndex = tokenIndex;
        this.start = start;
        this.end = end;
        this.length = length;
    }

    // depth = number of times this block of text occurrences in the text
    public int depth() {
        if (end == 0) {
            throw new IllegalStateException("LCP interval is unclosed!");
        }
        return this.end - this.start + 1;
    }

    public List<Block.Instance> getAllInstances() {
        List<Block.Instance> instances = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            // every i is one occurrence
            int token_position = tokenIndex.suffix_array[i];
            Block.Instance instance = new Instance(token_position, this);
            instances.add(instance);
        }
        return instances;
    }

    // transform lcp interval into int stream range
    public IntStream getAllOccurrencesAsRanges() {
        IntStream result = IntStream.empty();
        // with/or without end
        for (int i = start; i < end; i++) {
            // every i is one occurrence
            int token_position = tokenIndex.suffix_array[i];
            IntStream range = IntStream.range(token_position, token_position + length);
            result = IntStream.concat(result, range);
        }
        return result;
    }

    protected String getNormalizedForm() {
        int suffix_start = this.start;
        int token_pos = tokenIndex.suffix_array[suffix_start];
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < this.length; i++) {
            Token t = tokenIndex.token_array.get(token_pos + i);
            tokens.add(t);
        }
        String normalized = "";
        for (Token t : tokens) {
            SimpleToken st = (SimpleToken) t;
            if (!normalized.isEmpty()) {
                normalized += " ";
            }
            normalized += st.getNormalized();
        }
        return normalized;
    }

    @Override
    public String toString() {
        return ("LCP interval start at: " + start + " , length: " + this.length + " depth:" + depth());
    }

    public static class Instance {
        private final int token_start;
        private final Block block;

        public Instance(int token_start, Block block) {
            this.token_start = token_start;
            this.block = block;
        }

        public int length() {
            return block.length;
        }

        public IntStream asRange() {
            return IntStream.range(token_start, token_start + length());
        }

        @Override
        public String toString() {
            List<Token> tokens = new ArrayList<>();
            for (int i = 0; i < this.length(); i++) {
                Token t = block.tokenIndex.token_array.get(token_start + i);
                tokens.add(t);
            }
            String normalized = "";
            for (Token t : tokens) {
                SimpleToken st = (SimpleToken) t;
                if (!normalized.isEmpty()) {
                    normalized += " ";
                }
                normalized += st.getNormalized();
            }
            return normalized;
        }
    }
}
