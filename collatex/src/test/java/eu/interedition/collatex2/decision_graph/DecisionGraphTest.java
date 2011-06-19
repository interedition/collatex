package eu.interedition.collatex2.decision_graph;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.decision_graph.DGEdge;
import eu.interedition.collatex2.implementation.decision_graph.DGVertex;
import eu.interedition.collatex2.implementation.decision_graph.DecisionGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class DecisionGraphTest {

  @Test
  public void testDecisionGraph1() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("a", "The red cat and the black cat");
    IWitness b = engine.createWitness("b", "the black cat");
    
    //  the -> The
    //  the -> the
    //  black -> black
    //  cat -> cat
    //  cat -> cat
    // bij een decision tree zou de black wegvallen
    // we maken er een graaf van, dan krijgen we twee cirkels als het ware
    // shortest path
    // bij elke vertex bijhouden wat de minimum weight daar is
    // bij elke edge bijhouden of hij deel uitmaakt van het shortest path
    // dan zou het mogelijk moeten zijn om meerdere shortest paths 
    // te reconstrueren
    

    // we use a weighted DAG to make alignment decisions
    
    // eerst de vertices
    DecisionGraph graph = new DecisionGraph();
    DGVertex v1 = graph.getStartVertex();
    DGVertex vThe1 = new DGVertex(a.getTokens().get(0));
    DGVertex vThe2 = new DGVertex(a.getTokens().get(4));
    DGEdge e1 = new DGEdge(v1, vThe1, 0); // 0 = no gap -> ENumeration?
    DGEdge e2 = new DGEdge(v1, vThe2, 1); // 1 = gap
    graph.add(v1, vThe1, vThe2);
    graph.add(e1, e2);
    DGVertex vB = new DGVertex(a.getTokens().get(5));
    DGEdge e3 = new DGEdge(vThe1, vB, 1); 
    DGEdge e4 = new DGEdge(vThe2, vB, 0);
    graph.add(vB);
    graph.add(e3, e4);
    DGVertex vC1 = new DGVertex(a.getTokens().get(2));
    DGVertex vC2 = new DGVertex(a.getTokens().get(6));
    DGEdge e5 = new DGEdge(vB, vC1, 1);
    DGEdge e6 = new DGEdge(vB, vC2, 0);
    graph.add(vC1, vC2);
    graph.add(e5, e6);
    DGVertex end = graph.getEndVertex();
    DGEdge e7 = new DGEdge(vC1, end, 1);
    DGEdge e8 = new DGEdge(vC2, end, 0);
    graph.add(end);
    graph.add(e7, e8);
    
    Map<DGVertex, Integer> vertexToMinWeight = determineMinWeightForEachVertex(graph);
    
    
    // we moeten de weight van de end vertex hebben om de rest te filteren..
    DGVertex endVertex = graph.getEndVertex();
    int minGaps = vertexToMinWeight.get(endVertex);
    System.out.println("Mininum number of gaps in the alignment: "+minGaps);
    
    // ik kan nu een nieuwe graph maken waarbij ik alle vertices en edges die niet kleiner 
    // of gelijk de minimum weight zijn deleten
    // maar of dat echt nodig is
    // is nog maar de vraag
    
    
    
    // we moeten bijhouden welk pad we gelopen hebben in de vorm van edges
    // ook moeten we bijhouden welke stappen we nog moeten zetten
    // daar twijfel ik tussen de vertices en de edges
    // aangezien je wil recursen bij meerdere vertices..
    // en dan dus een bepaalde edge meegeven om te doen...
    // laten we initialisen met een bepaalde edge
//    DGVertex start = graph.getStartVertex();
//    Set<DGEdge> outgoingEdges = graph.outgoingEdgesOf(start);
//    for (DGEdge edge : outgoingEdges) {
//      edge.getEndVertex();
//      if (vertexToMinWeight )
//    }
    
  }

  private Map<DGVertex, Integer> determineMinWeightForEachVertex(DecisionGraph graph) {
    // bepalen minimaal aantal gaps in de decision graph
    Map<DGVertex, Integer> vertexToMinWeight = Maps.newLinkedHashMap();
    Iterator<DGVertex> iterator = graph.iterator();
    // setup map with startvertex
    DGVertex startVertex = iterator.next(); 
    vertexToMinWeight.put(startVertex, 0);
    while (iterator.hasNext()) {
      DGVertex next = iterator.next();
      Set<DGEdge> incomingEdgesOf = graph.incomingEdgesOf(next);
      // bepaal de nieuwe weight for elk van de incoming edges
      Map<DGEdge, Integer> edgeToTotalWeight = Maps.newLinkedHashMap();
      for (DGEdge incomingEdge : incomingEdgesOf) {
        edgeToTotalWeight.put(incomingEdge, vertexToMinWeight.get(incomingEdge.getBeginVertex())+incomingEdge.getWeight());
      }
      Integer min = Collections.min(edgeToTotalWeight.values());
      vertexToMinWeight.put(next, min);
    }
    System.out.println(vertexToMinWeight);
    return vertexToMinWeight;
  }
  
  // we kunnen natuurlijk de graph in een tree converten
  // door strategies de vertex te dupliceren
  // dan kun je alle paden vinden door de leaf nodes af te lopen
  // een dag zou meerdere start nodes kunne hebben
  // daar is er geen algoritme voor
  // maar mijn dag heeft maar 1 start node..
  // aargh
  
  
  // het moet wel met een graph want anders wordt het nix
  // in een nromale decision tree schuif je dan die ene optie in de bij de andere
  // dan ben ik echter de kost kwijt
  // of je kunt in dit geval zeggen dat die case niet bestaat
  // maar das niet echt mooi
}
