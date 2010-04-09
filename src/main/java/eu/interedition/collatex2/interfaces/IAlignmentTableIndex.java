package eu.interedition.collatex2.interfaces;


public interface IAlignmentTableIndex {

  public abstract boolean containsNormalizedPhrase(final String normalized);

  public abstract IColumns getColumns(final String normalized);

  public abstract int size();

}