package eu.interedition.collatex2.implementation.output.dot;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.edit_graph.EditGraph;
import eu.interedition.collatex2.implementation.edit_graph.EditGraphCreator;
import eu.interedition.collatex2.interfaces.IWitness;

public class DotExporterTest {
  private static final String THE_RED_CAT_AND_THE_BLACK_CAT = "The red cat and the black cat";

  @Test
  public void test() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("a", THE_RED_CAT_AND_THE_BLACK_CAT);
    IWitness b = engine.createWitness("b", "The black cat and the red cat");
    EditGraphCreator creator = new EditGraphCreator();
    EditGraph dGraph = creator.buildEditGraph(a, b);
    String dot = DotExporter.toDot(dGraph);
    assertNotNull(dot);
    assertTrue(dot.contains("NO_GAP"));
    DotExporter.generateSVG("out.svg", dot, "EditGraph: " + THE_RED_CAT_AND_THE_BLACK_CAT);
  }

}
