package eu.interedition.collatex.dekker.token_index;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.simple.SimpleToken;

import java.util.*;

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
    Block(TokenIndex tokenIndex, int suffix_start_position, int length) {
        this.tokenIndex = tokenIndex;
        this.start = suffix_start_position;
        this.length = length;
        this.end = 0;
        this.depth = 0;
    }

    Block(TokenIndex tokenIndex, int start, int end, int length) {
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
    int getFrequency() {
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

        Instance(int start_token, Block block) {
            this.start_token = start_token;
            this.block = block;
        }

        public int length() {
            return block.length;
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

        public Block getBlock() {
            return block;
        }
    }
}
