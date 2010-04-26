package eu.interedition.collatex2.interfaces;

public interface IAlignmentTableIndex extends IWitnessIndex {

  boolean containsNormalizedPhrase(final String normalized);

  IColumn getColumn(final INormalizedToken token);

  int size();

}