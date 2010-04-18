package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import eu.interedition.collatex2.interfaces.IWitness;

public interface IAlignmentArc {

  IAlignmentNode getBeginNode();

  IAlignmentNode getEndNode();

  List<IWitness> getWitnesses();

}
