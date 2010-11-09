package eu.interedition.collatex2.usecases.peter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.output.rankedgraph.IRankedVariantGraphVertex;
import eu.interedition.collatex2.implementation.output.rankedgraph.VariantGraphRanker;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class PeterUsesCasesTest {
  private static CollateXEngine factory;
  
  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }
  
  @Test
  public void testRanking() {
    IWitness a = factory.createWitness("A", "The black cat");
    IWitness b = factory.createWitness("B", "The black and white cat");
    IWitness c = factory.createWitness("C", "The black and green cat");
    IVariantGraph graph = factory.graph(a, b, c);
    VariantGraphRanker ranker = new VariantGraphRanker(graph);
    List<IRankedVariantGraphVertex> vertices = ranker.getRankedVertices();
    assertEquals("the", vertices.get(0).getNormalized());
    assertEquals(1, vertices.get(0).getRank());
    assertEquals("black", vertices.get(1).getNormalized());
    assertEquals(2, vertices.get(1).getRank());
    assertEquals("and", vertices.get(2).getNormalized());
    assertEquals(3, vertices.get(2).getRank());
    assertEquals("white", vertices.get(3).getNormalized());
    assertEquals(4, vertices.get(3).getRank());
    assertEquals("green", vertices.get(4).getNormalized());
    assertEquals(4, vertices.get(4).getRank());
    assertEquals("cat", vertices.get(5).getNormalized());
    assertEquals(5, vertices.get(5).getRank());
  }

}
