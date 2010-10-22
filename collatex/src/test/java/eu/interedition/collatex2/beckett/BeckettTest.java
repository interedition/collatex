package eu.interedition.collatex2.beckett;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.vg_analysis.IAnalysis;
import eu.interedition.collatex2.implementation.vg_analysis.ISequence;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class BeckettTest {
  private static CollateXEngine factory;
  
  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }
  
//TODO: This test only works with the
//TODO: AlternativeTokenIndexMatcher and the AlternativeWitnessIndex
//TODO: and the yet to be made AlternativeVariantGraphIndex!


@Ignore
@Test
public void testAnalysisBeckettLotsOfRepetition() {
  final IWitness a = factory.createWitness("A", "from the days & nights when day followed on night & night on day.");
  final IWitness b = factory.createWitness("B", "from the days and nights when day followed fast on night and night on day.");
  IVariantGraph graph = factory.graph(a);
  IAnalysis analysis = factory.analyse(graph, b);
  List<ISequence> sequences = analysis.getSequences();
  assertEquals("from the days", sequences.get(0).getNormalized());
  assertEquals("nights when day followed", sequences.get(1).getNormalized());
  assertEquals("on night", sequences.get(2).getNormalized());
  assertEquals("night on day", sequences.get(3).getNormalized());
  assertEquals(4, sequences.size());
}


//TODO: make alignment test?
}
