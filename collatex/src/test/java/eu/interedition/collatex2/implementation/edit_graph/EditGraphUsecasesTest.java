package eu.interedition.collatex2.implementation.edit_graph;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import eu.interedition.collatex2.implementation.vg_alignment.Superbase;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.edit_graph.EditGraphLinker;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

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
    Map<INormalizedToken, INormalizedToken> link = linker.link(new Superbase(graph), b);
    assertEquals(3, link.size());
    //TODO: add asserts!
    //System.out.println(link);
  }
}
