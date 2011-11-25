package eu.interedition.collatex2.implementation.output.dot;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;

import eu.interedition.collatex.implementation.CollateXEngine;
import eu.interedition.collatex.implementation.graph.edit.EditGraph;
import eu.interedition.collatex.implementation.graph.edit.EditGraphCreator;
import eu.interedition.collatex.implementation.matching.EditDistanceTokenComparator;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IWitness;

import org.junit.Test;

public class DotExporterTest {
  private static final String THE_RED_CAT_AND_THE_BLACK_CAT = "The red cat and the black cat";

  @Test
  public void test() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("a", THE_RED_CAT_AND_THE_BLACK_CAT);
    IWitness b = engine.createWitness("b", "With the green cat and the red cat");
    EditGraphCreator creator = new EditGraphCreator();
    Comparator<INormalizedToken> comparator = new EditDistanceTokenComparator();
    EditGraph dGraph = creator.buildEditGraph(a, b, comparator);
    String dot = DotExporter.toDot(dGraph);
    assertNotNull(dot);
    assertTrue(dot.contains("0"));
    DotExporter.generateSVG("out.svg", dot, "EditGraph: " + THE_RED_CAT_AND_THE_BLACK_CAT);
  }

}
