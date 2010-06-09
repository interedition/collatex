package eu.interedition.collatex2.interfaces;

import java.util.List;

import eu.interedition.collatex2.experimental.table.CollateXVertex;

public interface IColumn extends Comparable<IColumn> {

  boolean containsWitness(String sigil);

  INormalizedToken getToken(String sigil);

  List<INormalizedToken> getVariants();

  void addVariant(INormalizedToken token);

  void addMatch(INormalizedToken token);

  int getPosition();

  void setPosition(int position);

  ColumnState getState();

  List<String> getSigli();

  void accept(IAlignmentTableVisitor visitor);

  //TODO: remove add methods from interface!
  void addVertex(CollateXVertex vertex);

}
