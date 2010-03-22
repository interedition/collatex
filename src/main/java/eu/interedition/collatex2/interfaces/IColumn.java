package eu.interedition.collatex2.interfaces;

import java.util.List;

public interface IColumn {

  boolean containsWitness(String sigil);

  INormalizedToken getToken(String sigil);

  void addVariant(INormalizedToken token);

  //TODO: remove this dependency
  void addToSuperbase(ISuperbase superbase);

  List<INormalizedToken> getVariants();

}
