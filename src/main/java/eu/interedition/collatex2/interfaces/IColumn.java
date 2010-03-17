package eu.interedition.collatex2.interfaces;


public interface IColumn {

  boolean containsWitness(String sigil);

  INormalizedToken getToken(String sigil);

}
