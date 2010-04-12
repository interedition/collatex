package eu.interedition.collatex2.interfaces;

public interface IAlignmentTableIndex {

  boolean containsNormalizedPhrase(final String normalized);

  IColumns getColumns(final String normalized);

  int size();

}