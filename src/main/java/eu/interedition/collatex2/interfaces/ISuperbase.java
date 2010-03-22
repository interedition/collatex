package eu.interedition.collatex2.interfaces;

public interface ISuperbase extends IWitness {

  void addToken(INormalizedToken token, IColumn column);

  IColumn getColumnFor(INormalizedToken tokenA);

  void addColumn(IColumn column);

}
