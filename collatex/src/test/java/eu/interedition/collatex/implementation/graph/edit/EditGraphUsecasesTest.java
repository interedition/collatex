package eu.interedition.collatex.implementation.graph.edit;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import eu.interedition.collatex.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex.implementation.alignment.VariantGraphWitnessAdapter;
import org.junit.Test;

import eu.interedition.collatex.implementation.CollateXEngine;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IVariantGraph;
import eu.interedition.collatex.interfaces.IWitness;

public class EditGraphUsecasesTest {
//  <example>
//  <witness>The black cat</witness>
//  <witness>The black and white cat</witness>
//  <witness>The black and green cat</witness>
//  <witness>The black very special cat</witness>
//  <witness>The black not very special cat</witness>
//</example>

  @Test
  public void testUsecase1() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("A", "The black cat");
    IWitness b = engine.createWitness("B", "The black and white cat");
    IVariantGraph graph = engine.graph(a);
    EditGraphLinker linker = new EditGraphLinker();
    Map<INormalizedToken, INormalizedToken> link = linker.link(VariantGraphWitnessAdapter.create(graph), b, new EqualityTokenComparator());
    assertEquals(3, link.size());
    //TODO: add asserts!
    //System.out.println(link);
  }
}
