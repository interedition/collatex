package eu.interedition.collatex2.web.io;

import eu.interedition.collatex2.experimental.graph.IVariantGraph;
import eu.interedition.collatex2.web.ApiWitness;

public class GraphVisualisationWrapper {
  private final IVariantGraph graph;
  private final ApiWitness[] witnesses;

  public GraphVisualisationWrapper(ApiWitness[] witnesses, IVariantGraph graph) {
    this.witnesses = witnesses;
    this.graph = graph;
  }

  public String getJson() {

    //    [
    //     [#list graph.iterator() as vertex]
    //     {
    //       "id": "${vertex}",
    //       "name": "${vertex.normalized}",
    //       "data": {
    //        "$color": "[#if vertex.containsWitness('A')]#83548B[#else]#555555[/#if]",
    //        "$type": "[#if graph.inDegreeOf(vertex)=1 && graph.outDegreeOf(vertex)=1]circle[#else]triangle[/#if]",
    //        "$dim": 15 },
    //       "adjacencies": [
    //         [#list graph.outgoingEdgesOf(vertex) as edge]
    //         { "nodeFrom": "${vertex}", "nodeTo": "${edge.endVertex}", "data": { "$color": "#557EAA" } },
    //         [/#list]
    //       ]
    //     },
    //     [/#list]
    //   ]    

    return "";
  }

  public ApiWitness[] getWitnesses() {
    return witnesses;
  }
}
