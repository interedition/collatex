package eu.interedition.collatex.alignment.multiple_witness;

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
