package eu.interedition.collatex2.experimental.graph;

import java.util.List;

public interface IAlignmentGraph {

  List<IAlignmentNode> getNodes();

  IAlignmentNode getStartNode();

  List<IAlignmentArc> getArcs();

  //TODO: implement!
  List<String>  findRepeatingTokens();

}