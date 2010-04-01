package eu.interedition.collatex2.interfaces;

public interface IReplacement extends IModification {

  IColumns getOriginalColumns();

  IPhrase getReplacementPhrase();

  IColumn getNextColumn();

}
