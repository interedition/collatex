package eu.interedition.collatex2.interfaces;

import java.util.List;

public interface ISuperbase extends IWitness {

  IColumn getColumnFor(INormalizedToken tokenA);

  List<IColumn> getColumnsFor(IPhrase phraseA);

  void addToken(INormalizedToken token, IColumn column);

  void addColumn(IColumn column);

}
