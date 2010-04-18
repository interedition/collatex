package eu.interedition.collatex2.experimental.graph;

import eu.interedition.collatex2.interfaces.IWitness;

public interface IAlignmentArc {

  //TODO: this should become multiple witnesses!
  IWitness getWitness();

  IAlignmentNode getBeginNode();

  IAlignmentNode getEndNode();

}
