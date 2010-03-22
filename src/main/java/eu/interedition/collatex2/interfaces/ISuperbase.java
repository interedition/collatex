package eu.interedition.collatex2.interfaces;

import eu.interedition.collatex2.implementation.alignmenttable.Column3;

public interface ISuperbase extends IWitness {

  void addToken(INormalizedToken variant, Column3 column3);

}
