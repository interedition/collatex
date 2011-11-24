package eu.interedition.collatex2.implementation.graph.edit;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import eu.interedition.collatex2.implementation.matching.Matches;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenLinker;
import eu.interedition.collatex2.interfaces.IWitness;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EditGraphLinker implements ITokenLinker {
  
  @Override
  public Map<INormalizedToken, INormalizedToken> link(IWitness superbase, IWitness b, Comparator<INormalizedToken> comparator) {
    EditGraphCreator egCreator = new EditGraphCreator();
    EditGraph editGraph = egCreator.buildEditGraph(superbase, b, comparator);
    EditGraphVisitor visitor = new EditGraphVisitor(editGraph);
    List<EditGraphEdge> shortestPath = visitor.getShortestPath();
    Iterator<EditGraphEdge> edges = shortestPath.iterator();
//    for (DGEdge edge : shortestPath) {
//      System.out.println(edge.getTargetVertex().toString());
//    }
    //Note: This is the second time the matcher function is called
    Multimap<INormalizedToken, INormalizedToken> matches = Matches.between(superbase, b, comparator).getAll();
    Map<INormalizedToken, INormalizedToken> linkedTokens = Maps.newLinkedHashMap();
    List<INormalizedToken> tokens = b.getTokens();
    for (INormalizedToken token : tokens) {
      if (matches.containsKey(token)) {
        EditGraphEdge edge = edges.next();
        linkedTokens.put(token, edge.getTargetVertex().getBaseToken());
      }
    }
    return linkedTokens;
  }
}
