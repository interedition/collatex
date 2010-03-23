package eu.interedition.collatex2.interfaces;

public interface IReplacement extends IModification {

  IPhrase getOriginalWords();

  IPhrase getReplacementWords();

}
