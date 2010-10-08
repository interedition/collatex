package eu.interedition.collatex2.interfaces;

public interface IAligner {
  IAlignmentTable getResult();

  IAligner add(IWitness... witnesses);
  
  void setCallback(ICallback callback);
}
