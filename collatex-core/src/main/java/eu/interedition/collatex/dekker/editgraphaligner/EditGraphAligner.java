package eu.interedition.collatex.dekker.editgraphaligner;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.dekker.token_index.TokenIndex;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.util.StreamUtil;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.max;
import static java.util.Comparator.comparingInt;

/**
 * Created by Ronald Haentjens Dekker on 06/01/17.
 * <p>
 * This class tries to combine ideas of the Java version of cx with the Python version of cx
 * It uses the TokenIndex (suffix array, lcp array, lcp intervals) that was first pioneered in the python version
 * But then it uses the TokenIndexToMatches 3d matches (cube) between the variant graph and the next witness,
 * using the token index.
 * <p>
 * For the Edit graph table and scorer code is used from the CSA branch (which is meant to deal with multiple
 * textual layers within one witness). The edit graph table and scorer were previously used in the Python version.
 * <p>
 * 1. Build a token index to find repeating patterns. Algorithm: Suffix Array, LCP array, LCP Intervals.
 * Present in the Java version of CX and in the Python version of CX. Class: TokenIndex
 * <p>
 * 2. Given a Variant Graph and the next witness to align, build a cube of matches.
 * Present in the Java version of CX. Class: TokenIndexToMatches.
 * a. Needs to be improved a bit to not return legacy classes Island and Coordinate.
 * b. Duplicates need to be removed (horizontal (first match only) en vertical (multiple vertices)).
 * c. Needs to be ported to Python version.
 * <p>
 * 3. A matrix/table for the edit operations needs to be created (work in progress).
 * <p>
 * 4. A scorer needs to be created that prefers depth over size, and as much higher depth nodes as possible.
 * Think: histogram experiment. The current Java and Python version are suboptimal.
 * Java version does not have a Scorer. The Python one does, but filters the blocks too soon (on a global level).
 * <p>
 * 5. Analysis: transposition detection
 */
public class EditGraphAligner extends CollationAlgorithm.Base {
    public TokenIndex tokenIndex;
    // tokens are mapped to vertices by their position in the token array
    public VariantGraph.Vertex[] vertex_array;
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
        match(witnesses);

        // phase 2: alignment phase
        align(graph, witnesses);
    }

    private void match(List<? extends Iterable<Token>> witnesses) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Building token index from the tokens of all witnesses");
        }

        this.tokenIndex = new TokenIndex(comparator, witnesses);
        tokenIndex.prepare();
    }

    private void align(VariantGraph graph, List<? extends Iterable<Token>> witnesses) {
        this.vertex_array = new VariantGraph.Vertex[tokenIndex.token_array.length];
        boolean firstWitness = true;

        for (Iterable<Token> tokens : witnesses) {
            final Witness witness = StreamUtil.stream(tokens)
                .findFirst()
                .map(Token::getWitness)
                .orElseThrow(() -> new IllegalArgumentException("Empty witness"));

            // first witness has a fast path
            if (firstWitness) {
                super.merge(graph, tokens, emptyMap());
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

            // now we can create the space for the edit graph.. using arrays and stuff
            // the horizontal size is the number of ranks in the graph starting from 0
            VariantGraphRanking variantGraphRanking = VariantGraphRanking.of(graph);
            Map<VariantGraph.Vertex, Integer> byVertex = variantGraphRanking.getByVertex();
            List<Integer> variantGraphRanks = StreamUtil.stream(graph.vertices())//
                .map(byVertex::get)//
                .distinct()//
                .collect(Collectors.toList());

            // we leave in the rank of the start vertex, but remove the rank of the end vertex
            variantGraphRanks.remove(variantGraphRanks.size() - 1);

            // System.out.println("horizontal (graph, rank): " + variantGraphRanks);

//            Map<Integer, Set<VariantGraph.Vertex>> vertexSetByRank = variantGraphRanking.getByRank();

            // now the vertical stuff
            List<Token> witnessTokens = StreamUtil.stream(tokens).collect(Collectors.toList());
            List<Integer> tokensAsIndexList = asIndexList(tokens);
            // System.out.println("vertical (next witness, token index): " + tokensAsIndexList);

            MatchCube cube = new MatchCube(tokenIndex, tokens, vertex_array, variantGraphRanking);
            fillNeedlemanWunschTable(variantGraphRanks, witnessTokens, tokensAsIndexList, cube);

            // debug only
            // printScoringTable(variantGraphRanks, tokensAsIndexList);

            Map<Token, VariantGraph.Vertex> aligned = alignMatchingTokens(cube);
            merge(graph, tokens, aligned);
            updateTokenToVertexArray(tokens, witness);
        }
    }

    private List<Integer> asIndexList(Iterable<Token> tokens) {
        List<Integer> tokensAsIndexList = new ArrayList<>();
        tokensAsIndexList.add(0);
        int counter = 1;
        for (Token t : tokens) {
            tokensAsIndexList.add(counter++);
        }
        return tokensAsIndexList;
    }

    private void fillNeedlemanWunschTable(List<Integer> variantGraphRanks, List<Token> witnessTokens, List<Integer> tokensAsIndexList, MatchCube cube) {
        // code below is partly taken from the CSA branch.
        // init cells and scorer
        this.cells = new Score[tokensAsIndexList.size()][variantGraphRanks.size()];
        Scorer scorer = new Scorer(cube);

        // init 0,0
        this.cells[0][0] = new Score(Score.Type.empty, 0, 0, null, 0);

        // fill the first row with gaps
        IntStream.range(1, variantGraphRanks.size()).forEach(x -> {
            int previousX = x - 1;
            this.cells[0][x] = scorer.gap(x, 0, this.cells[0][previousX]);
        });

        // fill the first column with gaps
        IntStream.range(1, tokensAsIndexList.size()).forEach(y -> {
            int previousY = y - 1;
            this.cells[y][0] = scorer.gap(0, y, this.cells[previousY][0]);
        });

        // fill the remaining cells
        // fill the rest of the cells in a y by x fashion
        IntStream.range(1, tokensAsIndexList.size()).forEach(//
            y -> IntStream.range(1, variantGraphRanks.size()).forEach(
                x -> {
                    int previousY = y - 1;
                    int previousX = x - 1;
                    Score fromUpperLeft = scorer.score(x, y, this.cells[previousY][previousX]);
                    Score fromLeft = scorer.gap(x, y, this.cells[y][previousX]);
                    Score fromUpper = calculateFromUpper(scorer, y, x, previousY, cube);
                    Score max = max(asList(fromUpperLeft, fromLeft, fromUpper), comparingInt(score -> score.globalScore));
                    this.cells[y][x] = max;
                }));
    }

    private Map<Token, VariantGraph.Vertex> alignMatchingTokens(MatchCube cube) {
        // using the score iterator..
        // find all the matches
        // later for the transposition detection, we also want to keep track of all the additions, omissions, and replacements
        Map<Token, VariantGraph.Vertex> aligned = new HashMap<>();
        ScoreIterator scores = new ScoreIterator(this.cells);
        Set<VariantGraph.Vertex> matchedVertices = new HashSet<>();
        while (scores.hasNext()) {
            Score score = scores.next();
            if (score.type == Score.Type.match) {
                int rank = score.x - 1;
                Match match = cube.getMatch(score.y - 1, rank);
                if (!matchedVertices.contains(match.vertex)) {
                    aligned.put(match.token, match.vertex);
                    matchedVertices.add(match.vertex);
                }
            }
        }
        return aligned;
    }

    private Score calculateFromUpper(Scorer scorer, int y, int x, int previousY, MatchCube matchCube) {
        boolean upperIsMatch = matchCube.hasMatch(previousY - 1, x - 1);
        return upperIsMatch //
            ? scorer.score(x, y, this.cells[previousY][x]) //
            : scorer.gap(x, y, this.cells[previousY][x]);
    }

    private void printScoringTable(List<Integer> verticesAsRankList, List<Integer> tokensAsIndexList) {
        // print the scoring table for debugging reasons
        for (int y = 0; y < tokensAsIndexList.size(); y++) {
            System.out.print("|");
            for (int x = 0; x < verticesAsRankList.size(); x++) {
                Score cell = cells[y][x];
                String value;
                if (cell == null) {
                    value = "unscored";
                } else {
                    value = "" + cell.getGlobalScore();
                    if (cell.type == Score.Type.match) {
                        value += "M";
                    } else {
                        value += " ";
                    }
                    if (cell.getGlobalScore() > -1) {
                        value = " " + value;
                    }
                }
                System.out.print(value + "|");
            }
            System.out.println();
            System.out.println("----------");
        }
        System.out.println();
    }

    //    private void printScoringTable(List<Integer> verticesAsRankList, List<Integer> tokensAsIndexList) {
//        // print the scoring table for debugging reasons
//        for (int y = 0; y < tokensAsIndexList.size(); y++) {
//            System.out.print("|");
//            for (int x = 0; x < verticesAsRankList.size(); x++) {
//                Score cell = cells[y][x];
//                String value;
//                if (cell == null) {
//                    value = "unscored";
//                } else {
//                    value = "" + cell.getGlobalScore();
//                    if (cell.type == Score.Type.match) {
//                        value += "M";
//                    } else {
//                        value += " ";
//                    }
//                    if (cell.getGlobalScore() > -1) {
//                        value = " " + value;
//                    }
//                }
//                System.out.print(value + "|");
//            }
//            System.out.println();
//            System.out.println("-");
//        }
//        System.out.println();
//    }

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

    @Override
    public void collate(VariantGraph against, Iterable<Token> witness) {
        collate(against, Arrays.asList(witness));
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

        public enum Type {
            match, mismatch, addition, deletion, empty
        }
    }

    class Scorer {
        private final MatchCube matchCube;

        public Scorer(MatchCube matchCube) {
            this.matchCube = matchCube;
        }

        public Score gap(int x, int y, Score parent) {
            Score.Type type = determineType(x, y, parent);
            return new Score(type, x, y, parent, parent.globalScore - 1);
        }

        public Score score(int x, int y, Score parent) {
            int rank = (x - 1);
            if (this.matchCube.hasMatch(y - 1, rank)) {
                Match match = this.matchCube.getMatch(y - 1, rank);
                return new Score(Score.Type.match, x, y, parent, parent.globalScore + 1);
            }
            return new Score(Score.Type.mismatch, x, y, parent, parent.globalScore - 1);
        }

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

    private static class ScoreIterator implements Iterator<Score> {
        Integer y;
        Integer x;
        private Score[][] matrix;

        ScoreIterator(Score[][] matrix) {
            this.matrix = matrix;
            this.x = matrix[0].length - 1;
            this.y = matrix.length - 1;
        }

        @Override
        public boolean hasNext() {
            return !(this.x == 0 && this.y == 0);
        }

        @Override
        public Score next() {
            Score currentScore = this.matrix[this.y][this.x];
            this.x = currentScore.previousX;
            this.y = currentScore.previousY;
            return currentScore;
        }
    }
}
