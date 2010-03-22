package eu.interedition.collatex2.interfaces;

import java.util.List;

public interface IColumn {

  boolean containsWitness(String sigil);

  INormalizedToken getToken(String sigil);

  List<INormalizedToken> getVariants();

  void addVariant(INormalizedToken token);

  void addMatch(INormalizedToken token);

  //TODO: remove this dependency
  void addToSuperbase(ISuperbase superbase);

}
