package eu.interedition.collatex2.interfaces;

public enum ColumnState {
  NEW, MATCH, VARIANT;

  public ColumnState addMatch() {
    if (this == VARIANT) return VARIANT;
    return MATCH;
  }

  public ColumnState addVariant() {
    return VARIANT;
  }
}
