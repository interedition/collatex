package eu.interedition.collatex2.experimental.graph;

import java.util.List;

public interface IVariantGraph {

  List<IVariantGraphNode> getNodes();

  IVariantGraphNode getStartNode();

  List<IVariantGraphArc> getArcs();

  //TODO: implement!
  List<String>  findRepeatingTokens();

}