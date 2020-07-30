package eu.interedition.collatex.dekker.token_index;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.simple.SimpleToken;

import java.util.*;
import java.util.stream.IntStream;

public class Block {
    // every Block has a token index as a parent
    private final TokenIndex tokenIndex;
    // length = number of tokens in this block of text
    public final int length;
    // start = start position in suffix array
    public final int start;
    // end = end position in suffix array
    public final int end;
    // depth = number of witnesses this block of text occurs in
    // Note: depth is lazy initialized
    private Integer depth;

    // For building blocks only
    public Block(TokenIndex tokenIndex, int suffix_start_position, int length) {
        this.tokenIndex = tokenIndex;
        this.start = suffix_start_position;
        this.length = length;
        this.end = 0;
        this.depth = 0;
    }

    public Block(TokenIndex tokenIndex, int start, int end, int length) {
        this.tokenIndex = tokenIndex;
        this.start = start;
        this.end = end;
        this.length = length;
        this.depth = null;
    }

    public int getDepth() {
        if (depth == null) {
            depth = calculateDepth();
        }
        return depth;
    }

    // frequency = number of times this block of text occurs in complete witness set
    public int getFrequency() {
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

    @Override
    public String toString() {
        if (end == 0) {
            return "Unclosed LCP interval start at: " + start + ",  length: " + length;
        }
        return ("LCP interval start at: " + start + ", depth: " + this.getDepth() + ", length: " + this.length + " getFrequency:" + getFrequency());
    }

    private int calculateDepth() {
        // the same block can occur multiple times in one witness
        Set<Witness> witnesses = new HashSet<>();
        for (Block.Instance instance : getAllInstances()) {
            witnesses.add(instance.getWitness());
        }
        return witnesses.size();
    }

    public static class Instance {
        // position in token array
        public final int start_token;
        public final Block block;

        public Instance(int start_token, Block block) {
            this.start_token = start_token;
            this.block = block;
        }

        public int length() {
            return block.length;
        }

        public IntStream asRange() {
            return IntStream.range(start_token, start_token + length());
        }

        @Override
        public String toString() {
            List<Token> tokens = getTokens();
            StringBuilder normalized = new StringBuilder();
            for (Token t : tokens) {
                SimpleToken st = (SimpleToken) t;
                if (normalized.length() > 0) {
                    normalized.append(" ");
                }
                normalized.append(st.getNormalized());
            }
            return normalized.toString();
        }

        public List<Token> getTokens() {
            List<Token> tokens = new ArrayList<>();
            tokens.addAll(Arrays.asList(block.tokenIndex.token_array)//
                    .subList(start_token, start_token + this.length() ));
            return tokens;
        }

        public Witness getWitness() {
            Token startToken = block.tokenIndex.token_array[start_token];
            return startToken.getWitness();
        }
    }
}
