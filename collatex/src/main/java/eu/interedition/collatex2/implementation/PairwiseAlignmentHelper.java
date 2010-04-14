package eu.interedition.collatex2.implementation;

import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

public class PairwiseAlignmentHelper {
  public static IAlignment align(CollateXEngine engine, IWitness a, IWitness b) {
    IAlignmentTable table = engine.align(a);
    return engine.createAlignmentUsingIndex(table, b);
  }
}
