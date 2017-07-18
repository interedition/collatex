package eu.interedition.collatex.dekker.editgraphaligner;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph.Vertex;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static eu.interedition.collatex.util.StreamUtil.parallelStream;
import static eu.interedition.collatex.util.StreamUtil.stream;

/**
 * Created by Ronald Haentjens Dekker on 08/01/17.
 *
 * This class builds a cube of matches, given a VariantGraphRanking, a TokenComparator and the next witness.
 */
public class MatchCube {
    private final Map<MatchCoordinate, Match> matches = new HashMap<>();
    private final Comparator<Token> comparator;

//    @Deprecated
//    public MatchCube(Iterable<Token> tokens, //
//            TokenIndex tokenIndex, //
//            VariantGraph.Vertex[] vertexArray, //
//            VariantGraph graph) {
//        comparator = null;
//
//        Set<Island> allPossibleIslands = TokenIndexToMatches.createMatches(tokenIndex, vertexArray, graph, tokens);
//        // apparently there are doubles in the coordinates This is caused by 1. longer islands -> only take
//        // the first match, and 2. duplicate vertices, since one vertex can contain multiple tokens.
//
//        // convert the set of Island into a map of matches with as
//        for (Island i : allPossibleIslands) {
//            Coordinate c = i.getLeftEnd();
//            // System.out.println("y:"+c.row+", x:"+ c.column+":"+c.match.token);
//            // we put the matches in a (y, x) fashion.
//            MatchCoordinate coordinate = new MatchCoordinate(c.row, c.column);
//            matches.put(coordinate, c.match);
//        }
//    }

    public MatchCube(Iterable<Token> tokens, //
                     VariantGraphRanking variantGraphRanking, //
                     Comparator<Token> comparator) {

        this.comparator = comparator;

        AtomicInteger witnessTokenCounter = new AtomicInteger(0);
        stream(tokens).forEach(token -> {

            int tokenIndex = witnessTokenCounter.getAndIncrement();
            AtomicInteger rank = new AtomicInteger(-1);
            stream(variantGraphRanking).forEach(vertexSet -> {

                int vgRank = rank.getAndIncrement();
                parallelStream(vertexSet)//
                    .filter(this::hasTokens)//
                    .filter(vertex -> matches(vertex, token))//
                    .forEach(vertex -> {
                        MatchCoordinate coordinate = new MatchCoordinate(tokenIndex, vgRank);
                        Match match = new Match(vertex, token);
                        matches.put(coordinate, match);
                    });
            });
        });
    }

    private boolean hasTokens(Vertex vertex) {
        return !vertex.tokens().isEmpty();
    }

    private boolean matches(Vertex vertex, Token token) {
        Token vertexToken = vertex.tokens().iterator().next();
        return comparator.compare(vertexToken, token) == 0;
    }

    public boolean hasMatch(int y, int x) {
        MatchCoordinate c = new MatchCoordinate(y, x);
        return matches.containsKey(c);
    }

    public Match getMatch(int y, int x) {
        MatchCoordinate c = new MatchCoordinate(y, x);
        return matches.get(c);
    }

    class MatchCoordinate {
        final int tokenIndex; // position in witness, starting from zero
        final int rankInVG; // rank in the variant graph

        public MatchCoordinate(int tokenIndex, int rankInVG) {
            this.tokenIndex = tokenIndex;
            this.rankInVG = rankInVG;
        }

        @Override
        public int hashCode() {
            return tokenIndex * 72 + rankInVG;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MatchCoordinate)) {
                return false;
            }
            MatchCoordinate other = (MatchCoordinate) obj;
            return this.tokenIndex == other.tokenIndex && this.rankInVG == other.rankInVG;
        }
    }
}
