package eu.interedition.collatex2.interfaces;

import java.util.List;

public interface IColumn extends Comparable<IColumn> {

  boolean containsWitness(String sigil);

  INormalizedToken getToken(String sigil);

  List<INormalizedToken> getVariants();

  void addVariant(INormalizedToken token);

  void addMatch(INormalizedToken token);

  int getPosition();

  void setPosition(int position);

  ColumnState getState();

}
