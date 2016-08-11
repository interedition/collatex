package eu.interedition.collatex.subst;

import static eu.interedition.collatex.subst.Score.Type.addition;
import static eu.interedition.collatex.subst.Score.Type.deletion;
import static eu.interedition.collatex.subst.Score.Type.empty;
import static eu.interedition.collatex.subst.Score.Type.match;
import static eu.interedition.collatex.subst.Score.Type.mismatch;

public class Scorer {
    public Score gap(int x, int y, Score parent) {
        Score.Type type = determineType(x, y, parent);
        return new Score(type, x, y, parent, parent.globalScore - 1);
    }

    public Score score(int x, int y, Score parent, EditGraphTableLabel tokenB, EditGraphTableLabel tokenA) {
        if (tokensMatch(tokenB, tokenA)) {
            return new Score(match, x, y, parent);
        }

        return new Score(mismatch, x, y, parent, parent.globalScore - 2);
    }

    private boolean tokensMatch(EditGraphTableLabel tokenB, EditGraphTableLabel tokenA) {
        return normalized(tokenB).equals(normalized(tokenA));
    }

    private String normalized(EditGraphTableLabel tokenB) {
        return tokenB.text.data.toLowerCase().trim();
    }

    private Score.Type determineType(int x, int y, Score parent) {
        if (x == parent.x) {
            return addition;
        }
        if (y == parent.y) {
            return deletion;
        }
        return empty;
    }

}
