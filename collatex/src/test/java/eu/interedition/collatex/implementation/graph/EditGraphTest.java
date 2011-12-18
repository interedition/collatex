package eu.interedition.collatex.implementation.graph;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex.interfaces.IWitness;
import eu.interedition.collatex.interfaces.Token;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EditGraphTest extends AbstractTest {

  @Test
  public void testUsecase1() {
    final IWitness[] w = createWitnesses("The black cat", "The black and white cat");
    final VariantGraph graph = merge(w[0]);
    EditGraphLinker linker = new EditGraphLinker(graphFactory);
    Map<Token, VariantGraphVertex> link = linker.link(graph, w[1].getTokens(), new EqualityTokenComparator());
    assertEquals(3, link.size());
  }

  @Test
  public void testGapsEverythingEqual() {
    // All the witness are equal
    // There are choices to be made however, since there is duplication of tokens
    // Optimal alignment has no gaps
    final IWitness[] w = createWitnesses("The red cat and the black cat", "The red cat and the black cat");
    final VariantGraph graph = merge(w[0]);
    assertNumberOfGaps(0, graphFactory.newEditGraph(graph).build(graph, w[1].getTokens(), new EqualityTokenComparator()));
  }

  @Test
  public void testGapsOmission() {
    // There is an omission
    // Optimal alignment has 1 gap
    // Note: there are two paths here that contain 1 gap
    final IWitness[] w = createWitnesses("The red cat and the black cat", "the black cat");
    final VariantGraph graph = merge(w[0]);
    assertNumberOfGaps(1, graphFactory.newEditGraph(graph).build(graph, w[1].getTokens(), new EqualityTokenComparator()));
  }

  @Test
  public void testRemoveChoicesThatIntroduceGaps() {
    final IWitness[] w = createWitnesses("The red cat and the black cat", "the black cat");
    final VariantGraph graph = merge(w[0]);
    EditGraph eg = graphFactory.newEditGraph(graph).build(graph, w[1].getTokens(), new EqualityTokenComparator());
    assertShortestPathVertices(eg, "the", "black", "cat");

    final List<EditGraphEdge> edges = Lists.newArrayList(shortestPathIn(eg));
    assertEquals(4, edges.size());
    assertEquals(4, ((SimpleToken) edges.get(1).from().getWitness()).getIndex());
    assertEquals(5, ((SimpleToken) edges.get(2).from().getWitness()).getIndex());
    assertEquals(6, ((SimpleToken) edges.get(3).from().getWitness()).getIndex());
  }

  @Test
  public void testShortestPathOneOmissionRepetition() {
    final IWitness[] w = createWitnesses("The red cat and the black cat", "the black cat");
    final VariantGraph graph = merge(w[0]);
    EditGraph eg = graphFactory.newEditGraph(graph).build(graph, w[1].getTokens(), new EqualityTokenComparator());
    final List<EditGraphEdge> shortestPath = Lists.newArrayList(shortestPathIn(eg));
    assertEquals(4, shortestPath.size());
    assertEquals(EditOperation.GAP, shortestPath.get(0).getEditOperation()); // The ideal path should start with a gap
    assertEquals(EditOperation.NO_GAP, shortestPath.get(1).getEditOperation());
    assertEquals(EditOperation.NO_GAP, shortestPath.get(2).getEditOperation());
    assertEquals(EditOperation.NO_GAP, shortestPath.get(3).getEditOperation());
  }

  @Test
  public void testShortestPathEverythingEqual() {
    // All the witness are equal
    // There should only be one valid path through this decision graph
    final IWitness[] w = createWitnesses("The red cat and the black cat", "The red cat and the black cat");
    final VariantGraph graph = merge(w[0]);
    EditGraph eg = graphFactory.newEditGraph(graph).build(graph, w[1].getTokens(), new EqualityTokenComparator());
    assertEquals(1, Iterables.size(eg.shortestPaths()));
  }

  protected static void assertShortestPathVertices(EditGraph dGraph, String... vertices) {
    final Iterator<EditGraphEdge> shortestPath = shortestPathIn(dGraph).iterator();
    shortestPath.next(); // skip start vertex

    int vc = 0;
    for (String vertex : vertices) {
      assertTrue("Shortest path to short", shortestPath.hasNext());
      assertEquals(vertex + "[" + (vc++) + "]", vertex, ((SimpleToken) shortestPath.next().from().getWitness()).getNormalized());
    }
  }

  protected static Iterable<EditGraphEdge> shortestPathIn(EditGraph eg) {
    final Iterable<EditGraphEdge> shortestPath = eg.shortestPath(0);
    assertTrue("Shortest path exists", !Iterables.isEmpty(shortestPath));
    return shortestPath;
  }

  protected static void assertNumberOfGaps(int expected, EditGraph eg) {
    int numberOfGaps = 0;
    for (EditGraphEdge e : shortestPathIn(eg)) {
      if (e.getEditOperation() == EditOperation.GAP) {
        numberOfGaps++;
      }
    }
    assertEquals(expected, numberOfGaps);
  }

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
//  DGVertex start = graph.getStartVertex();
//  List<List<DGEdge>> initialpaths = Lists.newArrayList();
//  initialpaths.add(new ArrayList<DGEdge>());
//  List<List<DGEdge>> paths = traverseVertex(initialpaths, graph, vertexToMinWeight, minGaps, start);
//  System.out.println(paths);

  //  DGEdge[] bla = new DGEdge[] { new DGEdge(start, start, minGaps), new DGEdge(start, start, minGaps) };
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
  
//
//
//
//private List<List<DGEdge>> traverseVertex(List<List<DGEdge>> paths, DecisionGraph graph,
//    Map<DGVertex, Integer> vertexToMinWeight, int minGaps, DGVertex source) {
//  Set<DGEdge> outgoingEdges = graph.outgoingEdgesOf(source);
//  // hier moeten we kijken hoeveel outgoingEdges source heeft
//  // 0 == we zijn klaar; return de paden gewoon zoals ze zijn
//  // 1 == vul het huidige path gewoon aan en we zijn klaar
//  // >1 == maak extra paden aan in de list!
//  // hier het aantal outgoing edges checken werkt niet,
//  // want er kunnen er een aantal onzichtbaar zijn..
//  
//  for (DGEdge edge : outgoingEdges) {
//    DGVertex targetVertex = edge.getTargetVertex();
//    if (vertexToMinWeight.get(targetVertex) <= minGaps) {
//      // we willen dit path bewandelen
//      for (List<DGEdge> path : paths) {
//        path.add(edge);
//      }
//      traverseVertex(paths, graph, vertexToMinWeight, minGaps, targetVertex);
//    }
//  }
//  return paths;
//}

}
