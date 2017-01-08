package eu.interedition.collatex.dekker.editgraphaligner;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.dekker.island.Coordinate;
import eu.interedition.collatex.dekker.island.Island;
import eu.interedition.collatex.dekker.token_index.TokenIndex;
import eu.interedition.collatex.dekker.token_index.TokenIndexToMatches;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Ronald Haentjens Dekker on 08/01/17.
 *
 * This class builds a cube of matches, given a token index, a variant graph and the next witness.
 *
 * It builds on ideas in the TokenIndexToMatches class.
 *
 */
public class MatchCube {


    private final Map<MatchCoordinate, Match> matches;

    public MatchCube(TokenIndex tokenIndex, VariantGraph.Vertex[] vertexArray, VariantGraph graph, Iterable<Token> tokens) {
        // for now we build on the soon to be legacy TokenIndexToMatches class.
        Set<Island> allPossibleIslands = TokenIndexToMatches.createMatches(tokenIndex, vertexArray, graph, tokens);
        // apparently there are doubles in the coordinates This is caused by 1. longer islands -> only take
        // the first match, and 2. duplicate vertices, since one vertex can contain multiple tokens.

        // convert the ste of Island into a map of matches with as
        matches = new HashMap<>();
        for (Island i : allPossibleIslands) {
            Coordinate c = i.getLeftEnd();
            // System.out.println("y:"+c.row+", x:"+ c.column+":"+c.match.token);
            // we put the matches in a (y, x) fashion.
            MatchCoordinate coordinate = new MatchCoordinate(c.row, c.column);
            matches.put(coordinate, c.match);
        }
    }

    public boolean hasMatch(int y, int x) {
        MatchCoordinate c = new MatchCoordinate(y, x);
        return matches.containsKey(c);
    }

    class MatchCoordinate {
        protected final int tokenIndex; // position in witness, starting from zero
        protected final int rankInVG; // rank in the variant graph

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
