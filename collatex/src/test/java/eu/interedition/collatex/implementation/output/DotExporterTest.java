package eu.interedition.collatex.implementation.output;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.implementation.graph.EditGraph;
import eu.interedition.collatex.implementation.matching.EditDistanceTokenComparator;
import eu.interedition.collatex.interfaces.IWitness;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DotExporterTest extends AbstractTest {
  private static final String THE_RED_CAT_AND_THE_BLACK_CAT = "The red cat and the black cat";

  @Test
  public void test() throws IOException {
    final IWitness[] w = createWitnesses(THE_RED_CAT_AND_THE_BLACK_CAT, "With the green cat and the red cat");
    final EditGraph dGraph = graphFactory.newEditGraph().build(w[0], w[1], new EditDistanceTokenComparator());
    
    final String dot = DotExporter.toDot(dGraph);
    assertNotNull(dot);
    assertTrue(dot.contains("0"));
    DotExporter.generateSVG("out.svg", dot, "EditGraph: " + THE_RED_CAT_AND_THE_BLACK_CAT);
  }

}
