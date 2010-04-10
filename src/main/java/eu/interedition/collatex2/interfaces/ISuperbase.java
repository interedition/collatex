package eu.interedition.collatex2.interfaces;

//TODO: Obsolete; remove!
public interface ISuperbase extends IWitness {

  IColumn getColumnFor(INormalizedToken tokenA);

  IColumns getColumnsFor(IPhrase phraseA);

  void addToken(INormalizedToken token, IColumn column);

  void addColumn(IColumn column);

}
