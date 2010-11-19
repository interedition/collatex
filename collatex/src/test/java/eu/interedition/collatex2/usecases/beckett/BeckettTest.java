package eu.interedition.collatex2.usecases.beckett;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.vg_alignment.IAlignment2;
import eu.interedition.collatex2.implementation.vg_analysis.IAnalysis;
import eu.interedition.collatex2.implementation.vg_analysis.ISequence;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;
public class BeckettTest {
  private static CollateXEngine factory;
  
  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }
  
  @Test
  public void testNodeIsNear() {
    IWitness witness = factory.createWitness("A", "a b c d e f g h i j k l");
    List<INormalizedToken> tokens = witness.getTokens();
    Iterator<INormalizedToken> iterator = tokens.iterator();
    INormalizedToken a = iterator.next();
    INormalizedToken b = iterator.next();
    INormalizedToken c = iterator.next();
    INormalizedToken d = iterator.next();
    assertTrue(witness.isNear(a, b));
    assertFalse(witness.isNear(a, c));
    assertFalse(witness.isNear(b, d));
    assertTrue(witness.isNear(c, d));
  }
  
  @Test
  public void testGraphGetTokens() {
    IWitness witnessA = factory.createWitness("A", "a b c d");
    IVariantGraph graph = factory.graph(witnessA);
    List<INormalizedToken> tokensA = graph.getTokens(witnessA);
    assertEquals("a", tokensA.get(0).getNormalized());
    //TODO: etc
  }
  
  @Test
  public void testGraphAlignment() {
    IWitness witnessA = factory.createWitness("A", "a b c d");
    IWitness witnessB = factory.createWitness("B", "a b d");
    IVariantGraph graph = factory.graph(witnessA);
    IAlignment2 alignment = factory.align(graph, witnessB);
    List<ITokenMatch> tokenMatches = alignment.getTokenMatches();
    ITokenMatch match1 = tokenMatches.get(0);
    ITokenMatch match2 = tokenMatches.get(1);
    ITokenMatch match3 = tokenMatches.get(2);
    List<INormalizedToken> tokensA = graph.getTokens(witnessA);
    assertEquals(tokensA.get(0), match1.getBaseToken());
    assertEquals(tokensA.get(1), match2.getBaseToken());
    assertEquals(tokensA.get(3), match3.getBaseToken());
    assertEquals(3, tokenMatches.size());
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
}
