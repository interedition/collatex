package eu.interedition.collatex.subst;

public class Score {
    public static enum Type {
        match, mismatch, addition, deletion, empty
    }

    public Type type;
    int x;
    int y;
    public Score parent;
    int previousX;
    int previousY;
    public int globalScore = 0;

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

}
