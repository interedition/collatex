package eu.interedition.collatex.dekker.experimental_aligner;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.*;
import eu.interedition.collatex.dekker.matrix.Coordinate;
import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.dekker.matrix.IslandConflictResolver;
import eu.interedition.collatex.dekker.matrix.MatchTable;
import eu.interedition.collatex.suffixarray.SAIS;
import eu.interedition.collatex.suffixarray.SuffixArrays;
import eu.interedition.collatex.suffixarray.SuffixData;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.*;
import java.util.function.Predicate;

public class Dekker21Aligner extends CollationAlgorithm.Base {
    public TokenIndex tokenIndex;
    // tokens are mapped to vertices by their position in the token array
    protected VariantGraph.Vertex[] vertex_array;
    // map vertices to LCP
    // NOTE: vertices contain tokens... tokens are already mapped to LCP intervals
    // NOTE: It should be possible to remove this map

    // TODO: REMOVE REMOVE REMOVE!
    private Map<VariantGraph.Vertex, Block> vertexToLCP;
    // for debugging purposes only
    protected MatchTable table;
    protected List<Island> preferredIslands;
    protected List<List<Match>> transpositions;

    public Dekker21Aligner() {
        vertexToLCP = new HashMap<>();
    }

    @Override
    public void collate(VariantGraph graph, Iterable<Token> tokens) {
        throw new RuntimeException("Progressive alignment is not supported!");
    }

    // The algorithm contains two phases:
    // 1) Matching phase
    // This phase is implemented using a token array -> suffix array -> LCP array -> LCP intervals
    //
    // 2) Alignment phase
    // This phase uses a decision tree (implemented as a table) to find the optimal alignment and moves
    @Override
    public void collate(VariantGraph graph, List<? extends Iterable<Token>> witnesses) {
        // matching phase
        this.tokenIndex = new TokenIndex(witnesses);
        tokenIndex.prepare();

        this.vertex_array = new VariantGraph.Vertex[tokenIndex.token_array.size()];

        for (Iterable<Token> tokens : witnesses) {
            // first witness?
            boolean first_witness = vertexToLCP.isEmpty();
            if (first_witness) {
                super.merge(graph, tokens, new HashMap<>());
                // need to update vertex to lcp map

                // we need tokens token -> vertex
                // that information is stored in protected map
                int tokenPosition = 0;
                for (Token token : tokens) {
                    VariantGraph.Vertex vertex = witnessTokenVertices.get(token);
                    // remove
                    Block interval = tokenIndex.getLCP_intervalFor(tokenPosition);
                    vertexToLCP.put(vertex, interval);
                    // end remove
                    vertex_array[tokenPosition] = vertex;
                    tokenPosition++;
                }
                continue;
            }

            // rank the variant graph
            VariantGraphRanking ranking = VariantGraphRanking.of(graph);



            // We have to create a set of Islands and a MatchTableSelection here
            List<Island> islands = new ArrayList<>();
            // get all the blocks for this token
            // we have to iterate over all the token positions of this witness
            int startTokenPositionForWitness = tokenIndex.getStartTokenPositionForWitness(tokens.iterator().next().getWitness());
            int tokenPosition = startTokenPositionForWitness;
            for (Token token : tokens) {
                // gather block
                Block interval = tokenIndex.getLCP_intervalFor(tokenPosition);
                tokenPosition++;

                // We reuse Island class
                // for that we transform block instances into Island...
                for (Block.Instance instance : interval.getAllInstances()) {


                    if (instance.start_token < startTokenPositionForWitness) {
                        // we need to create an Island or a series of matches out of this instance..
                        // we got two pointers...
                        // one is the token position so the witness token pointer =
                        // token position - startTokenPositionForWitness
                        int witnessTokenPointer = tokenPosition - startTokenPositionForWitness;

                        // The other one is the start position in the token array
                        // No, we can just ask for the tokens on the instance
                        // the other pointer is the rank of the vertex associated with the token in the token array
                        // so we have to do some black magic here
                        // we go from token in token array -> token to vertex --> rank(vertex)
                        // no wait it is a vertex array; not a map
                        VariantGraph.Vertex baseVertex = vertex_array[instance.start_token];
                        int rank = ranking.apply(baseVertex);

                        // TODO; geen idee wat nu de rank is en wat de witness token pointer
                        Coordinate start = new Coordinate(rank, witnessTokenPointer);
                        Coordinate end = new Coordinate(rank+instance.length(), witnessTokenPointer+instance.length());
                        Island island = new Island(start, end);
                    }
                }
            }





            // OLD!!!!!!!!
            table = BlockBasedMatchTable.create(this, graph, tokens);


            // Phase 2: do the actual alignment and find transpositions
            IslandConflictResolver resolver = new IslandConflictResolver(table);
            preferredIslands = resolver.createNonConflictingVersion().getIslands();
            // we need to convert the islands into Map<Token, Vertex> for further processing
            // Here the result is put in a map
            Map<Token, VariantGraph.Vertex> alignments = new HashMap<>();
            for (Island island : preferredIslands) {
                for (Coordinate c : island) {
                    alignments.put(table.tokenAt(c.row, c.column), table.vertexAt(c.row, c.column));
                }
            }

            // detect phrases and transpositions
            // NOTE: It is probable that phrases can be replaced by Islands
            PhraseMatchDetector detector = new PhraseMatchDetector();
            List<List<Match>> phraseMatches = detector.detect(alignments, graph, tokens);

            TranspositionDetector detector2 = new TranspositionDetector();
            transpositions = detector2.detect(phraseMatches, graph);
            // Filter out transpositions from linked tokens
            for (List<Match> transposedPhrase : transpositions) {
                for (Match match : transposedPhrase) {
                    alignments.remove(match.token);
                }
            }

            // and merge
            merge(graph, tokens, alignments);

            // we filter out small transposed phrases over large distances
            List<List<Match>> falseTranspositions = new ArrayList<>();

            ranking = VariantGraphRanking.of(graph);

            for (List<Match> transposedPhrase : transpositions) {
                Match match = transposedPhrase.get(0);
                VariantGraph.Vertex v1 = witnessTokenVertices.get(match.token);
                VariantGraph.Vertex v2 = match.vertex;
                int distance = Math.abs(ranking.apply(v1) - ranking.apply(v2)) - 1;
                if (distance > transposedPhrase.size() * 3) {
                    falseTranspositions.add(transposedPhrase);
                }
            }

            for (List<Match> transposition : falseTranspositions) {
                transpositions.remove(transposition);
            }

            // merge transpositions
            mergeTranspositions(graph, transpositions);
        }
    }


    /**
     * Created by ronald on 4/20/15.
     */
    public static class TokenIndex {
        //TODO: not sure this functionality should be in this class or in a separate class
        private Map<Witness, Integer> witnessToStartToken;
        private Map<Witness, Integer> witnessToEndToken;
        private final List<? extends Iterable<Token>> w;
        public List<Token> token_array;
        //END witness data
        public int[] suffix_array;
        public int[] LCP_array;
        public List<Block> blocks;
        private Block[] block_array;


        public TokenIndex(List<? extends Iterable<Token>> w) {
            this.w = w;
        }

        // met deze constructor is er maar 1 witness.. ik weet niet of dit niet zo handig is
        public TokenIndex(Iterable<Token>[] w) {
            List<Iterable<Token>> witnesses = Arrays.asList(w);
            this.w = witnesses;
        }

        public int getStartTokenPositionForWitness(Witness witness) {
            return witnessToStartToken.get(witness);
        }

        // 1. prepare token array
        // 2. derive the suffix array
        // 3. derive LCP array
        // 4. derive LCP intervals
        public void prepare() {
            this.prepareTokenArray();
            Comparator<Token> comparator = new SimpleTokenNormalizedFormComparator();
            SuffixData suffixData = SuffixArrays.createWithLCP(token_array.toArray(new Token[0]), new SAIS(), comparator);
            this.suffix_array = suffixData.getSuffixArray();
            this.LCP_array = suffixData.getLCP();
            this.blocks = splitLCP_ArrayIntoIntervals();
            block_array = construct_LCP_interval_array();
        }

        private void prepareTokenArray() {
            token_array = new ArrayList<>();
            int counter = 0;
            witnessToStartToken = new HashMap<>();
            witnessToEndToken = new HashMap<>();
            for (Iterable<Token> tokens : w) {
                Witness witness = tokens.iterator().next().getWitness();
                witnessToStartToken.put(witness, counter);
                for (Token t : tokens) {
                    token_array.add(t);
                    counter++;
                }
                witnessToEndToken.put(witness, counter);
                //TODO: add witness separation marker token
            }
        }

        protected List<Block> splitLCP_ArrayIntoIntervals() {
            List<Block> closedIntervals = new ArrayList<>();
            int previousLCP_value = 0;
            Stack<Block> openIntervals = new Stack<Block>();
            for (int idx = 0; idx < LCP_array.length; idx++) {
                int lcp_value = LCP_array[idx];
                if (lcp_value > previousLCP_value) {
                    openIntervals.push(new Block(this, idx - 1, lcp_value));
                    previousLCP_value = lcp_value;
                } else if (lcp_value < previousLCP_value) {
                    // close open intervals that are larger than current LCP value
                    while (!openIntervals.isEmpty() && openIntervals.peek().length > lcp_value) {
                        Block a = openIntervals.pop();
                        closedIntervals.add(new Block(this, a.start, idx - 1, a.length));
                    }
                    // then: open a new interval starting with filtered intervals
                    if (lcp_value > 0) {
                        int start = closedIntervals.get(closedIntervals.size() - 1).start;
                        openIntervals.add(new Block(this, start, lcp_value));
                    }
                    previousLCP_value = lcp_value;
                }
            }
            // add all the open intervals to the result
            for (Block interval : openIntervals) {
                closedIntervals.add(new Block(this, interval.start, LCP_array.length - 1, interval.length));
            }
            return closedIntervals;
        }

        private Block[] construct_LCP_interval_array() {
            Block[] block_array = new Block[token_array.size()];
            for (Block interval : blocks) {
                //TODO: why are there empty LCP intervals in the LCP_interval_array ?
                if (interval.length==0) {
                    continue;
                }
                for (int i = interval.start; i <= interval.end; i++) {
                    int tokenPosition = suffix_array[i];
                    //Log("Adding interval: " + interval.toString() + " to token number: " + tokenIndex);
                    block_array[tokenPosition] = interval;
                }
            }
    //        //NOTE: For tokens that are not repeated we create new LCP intervals here
    //        //This is not very space efficient, but it makes life much easier for the code that follows
    //        for (int i=0; i< this.token_array.size(); i++) {
    //            if (lcp_interval_array[i]==null) {
    //                // create new LCP interval for token
    //                LCP_Interval lcp_interval;
    //                //NOTE: I have to know the start and end position of token in the suffix array... not easy!
    //                lcp_interval = new LCP_Interval()
    //            }
    //        }
            return block_array;
        }


        public Block getLCP_intervalFor(int tokenPosition) {
            return block_array[tokenPosition];
        }

        public boolean hasLCP_intervalFor(int i) {
            return block_array[i]!=null;
        }

        // lcp intervals can overlap horizontally
        // we prioritize the intervals with the biggest length
        // Note: with more than two witnesses we have to select the right instance of an interval
        public List<Block> getNonOverlappingBlocks() {
            // sort lcp intervals based on length in descending order
            Collections.sort(blocks, (Block interval1, Block interval2) -> interval2.length - interval1.length);
            //TODO: set size based on the length of the token array
            BitSet occupied = new BitSet();
            // set up predicate
            // why is length check needed? empty lcp intervals should not be there
            Predicate<Block> p = lcp_interval -> lcp_interval.length > 0 && !lcp_interval.getAllOccurrencesAsRanges().anyMatch(i -> occupied.get(i));

            List<Block> result = new ArrayList<>();
            for (Block interval : blocks) {
                // test whether the interval is in occupied
                //Note: filter
                if (p.test(interval)) {
                    result.add(interval);
                    // mark all the occurrences of the lcp interval in the occupied bit set
                    interval.getAllOccurrencesAsRanges().forEach(occupied::set);
                }
            }
            return result;
        }

        //NOTE: Much of the work done here can be prepared before hand
        //and stored in the lcp_array.. should be transformed in a block instance array
        public List<Block.Instance> getBlockInstancesForWitness(Witness w) {
            Integer witnessStart = witnessToStartToken.get(w);
            Integer witnessEnd = witnessToEndToken.get(w);
            //TODO: size: witnessEnd!
            BitSet witnessRange = new BitSet();
            witnessRange.set(witnessStart, witnessEnd);
            //TODO: rewrite!
            List<Block.Instance> result = new ArrayList<>();
            for (Block block : getNonOverlappingBlocks()) {
                for (Block.Instance instance : block.getAllInstances()) {
                    if (instance.asRange().anyMatch(witnessRange::get)) {
                        result.add(instance);
                    }
                }
            }
            result.sort((instance1, instance2) -> instance1.start_token - instance2.start_token);
            return result;
        }

        public int size() {
            return token_array.size();
        }
    }
}
