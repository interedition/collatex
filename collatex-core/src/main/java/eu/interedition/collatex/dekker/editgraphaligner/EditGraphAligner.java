package eu.interedition.collatex.dekker.editgraphaligner;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.dekker.island.Coordinate;
import eu.interedition.collatex.dekker.island.Island;
import eu.interedition.collatex.dekker.token_index.TokenIndex;
import eu.interedition.collatex.dekker.token_index.TokenIndexToMatches;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * Created by Ronald Haentjens Dekker on 06/01/17.
 *
 * This class tries to combine idea's of the Java version of cox with the Python version of cx
 * It uses the TokenIndex (suffix array, lcp array, lcp intervals) that was first pioneered in the python version
 * But then it uses the TokenIndexToMatches 3d matches (cube) between the variant graph and the next witness,
 * using the token index.
 *
 * For the Edit graph table and scorer code is used from the CSA branch (which is meant to deal with multiple
 * textual layers within one witness). The edit graph table and scorer were previously used in the Python version.
 *
 */
public class EditGraphAligner extends CollationAlgorithm.Base {
    public TokenIndex tokenIndex;
    // tokens are mapped to vertices by their position in the token array
    protected VariantGraph.Vertex[] vertex_array;
    private final Comparator<Token> comparator;
    Score[][] cells;

    public EditGraphAligner() {
        this(new EqualityTokenComparator());
    }

    public EditGraphAligner(Comparator<Token> comparator) {
        this.comparator = comparator;
    }



    @Override
    public void collate(VariantGraph graph, List<? extends Iterable<Token>> witnesses) {
        // phase 1: matching phase
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Building token index from the tokens of all witnesses");
        }

        this.tokenIndex = new TokenIndex(comparator, witnesses);
        tokenIndex.prepare();

        // phase 2: alignment phase
        this.vertex_array = new VariantGraph.Vertex[tokenIndex.token_array.length];
        boolean firstWitness = true;

        for (Iterable<Token> tokens : witnesses) {
            final Witness witness = StreamSupport.stream(tokens.spliterator(), false)
                .findFirst()
                .map(Token::getWitness)
                .orElseThrow(() -> new IllegalArgumentException("Empty witness"));

            // first witness has a fast path
            if (firstWitness) {
                super.merge(graph, tokens, Collections.emptyMap());
                updateTokenToVertexArray(tokens, witness);
                firstWitness = false;
                continue;
            }

            // align second, third, fourth witness etc.
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "{0} + {1}: {2} vs. {3}", new Object[]{graph, witness, graph.vertices(), tokens});
            }

            // Phase 2a: Gather matches from the token index
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0} + {1}: Gather matches between variant graph and witness from token index", new Object[]{graph, witness});
            }

            Set<Island> allPossibleIslands = TokenIndexToMatches.createMatches(tokenIndex, vertex_array, graph, tokens);
                // apparently there are doubles in the coordinates


            // convert the ste of Island into a map of matches with as
            Map<MatchCoordinate, Match> matches = new HashMap<>();
            for (Island i : allPossibleIslands) {
                for (Coordinate c : i) {


                    // print all the possible islands as separate matches...
                    System.out.println(c);

                    //TODO: more work!
                }
            }



            // now we can create the space for the edit graph.. using arrays and stuff
            // the vertical size is the number of vertices in the graph minus the start and end vertices
            // NOTE: THe token index to matches already does a graph ranking!
            VariantGraphRanking variantGraphRanking = VariantGraphRanking.of(graph);
                // oh wait there are more methods on the java variant graph ranking!
            List<Integer> verticesAsRankList = new ArrayList<>();
            for (VariantGraph.Vertex vertex : graph.vertices()) {
                int rank = variantGraphRanking.getByVertex().get(vertex);
                verticesAsRankList.add(rank);
            }
            // we leave the start vertex in (that is an extra position that is needed in the edit graph)
            // we remove the end vertex though
            verticesAsRankList.remove(verticesAsRankList.size()-1);

            System.out.println("horizontal (graph): "+ verticesAsRankList);

            // now the vertical stuff
            List<Token> witnessTokens = new ArrayList<>();
            for (Token t : tokens) {
                witnessTokens.add(t);
            }
            List<Integer> tokensAsIndexList = new ArrayList<>();
            tokensAsIndexList.add(0);
            int counter = 1;
            for (Token t : tokens) {
                tokensAsIndexList.add(counter++);
            }
            System.out.println("vertical (next witness): "+tokensAsIndexList);

            // code below is partly taken from the CSA branch.
            // init cells and scorer
            this.cells = new Score[tokensAsIndexList.size() ][verticesAsRankList.size() ];
            Scorer scorer = new Scorer();

            // init 0,0
            this.cells[0][0] = new Score(Score.Type.empty, 0, 0, null, 0);

            // fill the first row with gaps
            IntStream.range(1, verticesAsRankList.size()).forEach(x -> {
                int previousX = x - 1;
                this.cells[0][x] = scorer.gap(x, 0, this.cells[0][previousX]);
            });

            // fill the first column with gaps
            IntStream.range(1, tokensAsIndexList.size()).forEach(y -> {
                int previousY = y - 1;
                this.cells[y][0] = scorer.gap(0, y, this.cells[previousY][0]);
            });

            for (int y = 0; y < tokensAsIndexList.size() ; y++) {
                for (int x = 0; x < verticesAsRankList.size()  ; x++) {
                    Score cell = cells[y][x];
                    String value;
                    if (cell == null) {
                        value = "unscored";
                    } else {
                        value = ""+cell.getGlobalScore();
                    }
                    System.out.print(value+"|");
                }
                System.out.println();
                System.out.println("----");
            }
            // TODO: remove this break!
            break;
        }

    }

    private void updateTokenToVertexArray(Iterable<Token> tokens, Witness witness) {
        // we need to update the token -> vertex map
        // that information is stored in protected map
        int tokenPosition = tokenIndex.getStartTokenPositionForWitness(witness);
        for (Token token : tokens) {
            VariantGraph.Vertex vertex = witnessTokenVertices.get(token);
            vertex_array[tokenPosition] = vertex;
            tokenPosition++;
        }
    }

    public void collate(VariantGraph against, Iterable<Token> witness) {

    }

    public static class Score {

        public Type type;
        public Score parent;
        public int globalScore = 0;
        int x;
        int y;
        int previousX;
        int previousY;
        public Score(Type type, int x, int y, Score parent, int i) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.previousX = parent == null ? 0 : parent.x;
            this.previousY = parent == null ? 0 : parent.y;
            this.globalScore = i;
        }

        public Score(Type type, int x, int y, Score parent) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.previousX = parent.x;
            this.previousY = parent.y;
            this.globalScore = parent.globalScore;
        }

        public int getGlobalScore() {
            return this.globalScore;
        }

        public void setGlobalScore(int globalScore) {
            this.globalScore = globalScore;
        }

        @Override
        public String toString() {
            return "[" + this.y + "," + this.x + "]:" + this.globalScore;
        }

        public static enum Type {
            match, mismatch, addition, deletion, empty
        }

    }

    class Scorer {
        public Score gap(int x, int y, Score parent) {
            Score.Type type = determineType(x, y, parent);
            return new Score(type, x, y, parent, parent.globalScore - 1);
        }

        public Score score(int x, int y, Score parent, Token tokenB, Token tokenA) {
            if (tokensMatch(tokenB, tokenA)) {
                return new Score(Score.Type.match, x, y, parent);
            }

            return new Score(Score.Type.mismatch, x, y, parent, parent.globalScore - 2);
        }

        private boolean tokensMatch(Token tokenB, Token tokenA) {
            throw new RuntimeException("Not implemented yet! Use the islands and coordinates!");
//            return normalized(tokenB).equals(normalized(tokenA));
        }

//        private String normalized(Token tokenB) {
//            return tokenB.text.data.toLowerCase().trim();
//        }

        private Score.Type determineType(int x, int y, Score parent) {
            if (x == parent.x) {
                return Score.Type.addition;
            }
            if (y == parent.y) {
                return Score.Type.deletion;
            }
            return Score.Type.empty;
        }
    }

    class MatchCoordinate {
        protected final int tokenIndex; // position in witness, starting from zero
        protected final int rankInVG; // rank in the variant graph

        public MatchCoordinate(int tokenIndex, int rankInVG) {
            this.tokenIndex = tokenIndex;
            this.rankInVG = rankInVG;
        }
    }
}
