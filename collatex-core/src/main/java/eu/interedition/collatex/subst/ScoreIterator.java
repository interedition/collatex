package eu.interedition.collatex.subst;

import java.util.Iterator;

public class ScoreIterator implements Iterator<Score> {
    private Score[][] matrix;
    Integer y;
    Integer x;

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
