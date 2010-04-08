package eu.interedition.collatex2.interfaces;

import java.util.List;

import eu.interedition.collatex.alignment.multiple_witness.ColumnState;

public interface IColumn {

  boolean containsWitness(String sigil);

  INormalizedToken getToken(String sigil);

  List<INormalizedToken> getVariants();

  void addVariant(INormalizedToken token);

  void addMatch(INormalizedToken token);

  int getPosition();

  void setPosition(int position);

  //TODO: move column state enumeration class to this package!
  ColumnState getState();

}
