package eu.interedition.collatex2.experimental;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex2.implementation.vg_alignment.Superbase;
import eu.interedition.collatex2.implementation.vg_alignment.TokenLinker;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LinkerTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new CollateXEngine();
  }

  private Map<INormalizedToken, INormalizedToken> linkTokens(final IVariantGraph graph, final IWitness witness) {
    return new TokenLinker().link(new Superbase(graph), witness, new EqualityTokenComparator());
  }

  @Test
  public void testDirkVincent4() {
    IWitness a = engine.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = engine.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    IVariantGraph graph = engine.graph(a);
    Map<INormalizedToken, INormalizedToken> tokens = linkTokens(graph, b);
    IWitness sb = new Superbase(graph);
    INormalizedToken itsSB = sb.getTokens().get(1);
    INormalizedToken itsB = b.getTokens().get(0);
    assertEquals(itsSB, tokens.get(itsB));
    INormalizedToken lightSB = sb.getTokens().get(3);
    INormalizedToken lightB = b.getTokens().get(3);
    assertEquals(lightSB, tokens.get(lightB));
    INormalizedToken light2SB = sb.getTokens().get(12);
    INormalizedToken light2B = b.getTokens().get(6);
    assertEquals(light2SB, tokens.get(light2B));
  }
  
  @Test
  public void testDirkVincent9() {
    // lots of setup
    IWitness a = engine.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = engine.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    IVariantGraph graph = engine.graph(a, b);
    IWitness c = engine.createWitness("11", "Its faint unchanging light unlike any light he could remember from the days & nights when day followed on night & night on day.");
    Map<INormalizedToken, INormalizedToken> link = linkTokens(graph, c);
    //TODO: isn't this code the same as the other convienance method?
    List<INormalizedToken> unlinkedTokens = Lists.newArrayList();
    for (INormalizedToken witnessToken : c.getTokens()) {
      if (link.get(witnessToken) ==null) {
        unlinkedTokens.add(witnessToken);
      }
    }
    assertTrue(unlinkedTokens.contains(c.getTokens().get(1)));
    assertTrue(unlinkedTokens.contains(c.getTokens().get(2)));
    assertTrue(unlinkedTokens.contains(c.getTokens().get(21)));
    assertTrue(unlinkedTokens.contains(c.getTokens().get(22)));
    assertTrue(unlinkedTokens.contains(c.getTokens().get(23)));
    assertEquals(5, unlinkedTokens.size());
  }

  @Test
  public void testLinkingWithStartToken() {
    IWitness a = engine.createWitness("a", "So on to no purpose till finally at a stand again to his ears just audible oh how and here some word he could not catch it would be to end somewhere he had never been.");
    IWitness b = engine.createWitness("b", "The next he knew he was stuck still again & to his ears just audible Oh how and here a word he could not catch it were to end where never been.");
    IVariantGraph graph = engine.graph(a);
    Map<INormalizedToken, INormalizedToken> link = linkTokens(graph, b);
    assertTrue(!link.containsKey(b.getTokens().get(0)));
    assertTrue(!link.containsKey(b.getTokens().get(1)));
    assertTrue(!link.containsKey(b.getTokens().get(2)));
    assertTrue(!link.containsKey(b.getTokens().get(3)));
    assertTrue(!link.containsKey(b.getTokens().get(4)));
    assertTrue(!link.containsKey(b.getTokens().get(5)));
    assertTrue(!link.containsKey(b.getTokens().get(6)));
    assertTrue(!link.containsKey(b.getTokens().get(7)));
    assertTrue(link.containsKey(b.getTokens().get(8))); // again 
    assertTrue(!link.containsKey(b.getTokens().get(9))); 
    assertTrue(link.containsKey(b.getTokens().get(10))); // to
    assertTrue(link.containsKey(b.getTokens().get(11))); // his
  }

  @Test
  public void testLinkingRepetitionCausedByTransposition() {
    IWitness a = engine.createWitness("a","the cat is very happy");
    IWitness b = engine.createWitness("b", "very happy is the cat");
    IVariantGraph graph = engine.graph(a, b);
    IWitness c = engine.createWitness("C", "very delitied and happy is the cat");
    Map<INormalizedToken, INormalizedToken> tokens = linkTokens(graph, c);
    IWitness superbase = new Superbase(graph);
    INormalizedToken verySB = superbase.getTokens().get(3);
    INormalizedToken veryC = c.getTokens().get(0);
    INormalizedToken happySB = superbase.getTokens().get(4);
    INormalizedToken happyC = c.getTokens().get(3);
    INormalizedToken theSB = superbase.getTokens().get(8);
    INormalizedToken theC = c.getTokens().get(5);
    INormalizedToken catSB = superbase.getTokens().get(9);
    INormalizedToken catC = c.getTokens().get(6);
    assertEquals(verySB, tokens.get(veryC));
    assertEquals(happySB, tokens.get(happyC));
    assertEquals(theSB, tokens.get(theC));
    assertEquals(catSB, tokens.get(catC));
  }

  @Test
  public void testTwoEqualPossibilities1() {
    // test a -> a a
    final IWitness witnessA = engine.createWitness("A", "a");
    final IWitness witnessB = engine.createWitness("B", "a a");
    final IVariantGraph graph = engine.graph(witnessA);
    Map<INormalizedToken, INormalizedToken> tokens = linkTokens(graph, witnessB);
    assertEquals(1, tokens.size());
    INormalizedToken expectedBaseToken = graph.getTokens(witnessA).get(0);
    INormalizedToken witnessToken = witnessB.getTokens().get(0);
    assertEquals(expectedBaseToken, tokens.get(witnessToken));
  }

  @Test
  public void testEverythingIsUnique() {
    final IWitness witnessA = engine.createWitness("A", "everything is unique should be no problem");
    final IWitness witnessB = engine.createWitness("B", "everything is unique");
    final IVariantGraph graph = engine.graph(witnessA);
    Set<Map.Entry<INormalizedToken, INormalizedToken>> matches = linkTokens(graph, witnessB).entrySet();
    assertEquals(3, matches.size());
    assertLink("everything", "everything", Iterables.get(matches, 0));
    assertLink("is", "is", Iterables.get(matches, 1));
    assertLink("unique", "unique", Iterables.get(matches, 2));
  }


  private void assertLink(String left, String right, Map.Entry<INormalizedToken, INormalizedToken> match) {
    assertEquals(left, match.getKey().getNormalized());
    assertEquals(right, match.getValue().getNormalized());
  }
  @Test
  public void testEverythingIsUniqueTwoWitnesses() {
    final IWitness witnessA = engine.createWitness("A", "everything is unique should be no problem");
    final IWitness witnessB = engine.createWitness("B", "this one very different");
    final IWitness witnessC = engine.createWitness("C", "everything is different");
    IVariantGraph graph = engine.graph(witnessA, witnessB);
    Set<Map.Entry<INormalizedToken,INormalizedToken>> matches = linkTokens(graph, witnessC).entrySet();
    assertEquals(3, matches.size());
    assertEquals("everything", Iterables.get(matches, 0).getValue().getNormalized());
    assertEquals("is", Iterables.get(matches, 1).getValue().getNormalized());
    assertEquals("different", Iterables.get(matches, 2).getValue().getNormalized());
  }

  @Test
  public void testOverlappingMatches() {
    final IWitness witnessA = engine.createWitness("A", "everything is unique should be no problem");
    final IWitness witnessB = engine.createWitness("B", "this one is different");
    final IWitness witnessC = engine.createWitness("C", "everything is different");
    IVariantGraph graph = engine.graph(witnessA, witnessB);
    Set<Map.Entry<INormalizedToken,INormalizedToken>> matches = linkTokens(graph, witnessC).entrySet();
    assertEquals(3, matches.size());
    assertEquals("everything", Iterables.get(matches, 0).getValue().getNormalized());
    assertEquals("is", Iterables.get(matches, 1).getValue().getNormalized());
    assertEquals("different", Iterables.get(matches, 2).getValue().getNormalized());
  }
  
  @Test
  public void testGetMatchesUsingWitnessIndex() {
    final IWitness witnessA = engine.createWitness("A", "The big black cat and the big black rat");
    final IWitness witnessB = engine.createWitness("B", "The big black");
    final IVariantGraph graph = engine.graph(witnessA);
    Set<Map.Entry<INormalizedToken,INormalizedToken>> matches = linkTokens(graph, witnessB).entrySet();
    assertEquals(3, matches.size());
    assertLink("the", "the", Iterables.get(matches, 0));
    assertLink("big", "big", Iterables.get(matches, 1));
    assertLink("black", "black", Iterables.get(matches, 2));
  }

  //Note: internally this gives # the big black and the big black cat as matches
  @Test
  public void testGetMatchesUsingWitnessIndexWithOverlapping() {
    final IWitness witnessA = engine.createWitness("A", "the big black cat and the big black rat");
    final IWitness witnessB = engine.createWitness("B", "the big black cat");
    final IVariantGraph graph = engine.graph(witnessA);
    Set<Map.Entry<INormalizedToken,INormalizedToken>> matches = linkTokens(graph, witnessB).entrySet();
    assertEquals(4, matches.size());
    assertLink("cat", "cat", Iterables.get(matches, 0));
    assertLink("the", "the", Iterables.get(matches, 1));
    assertLink("big", "big", Iterables.get(matches, 2));
    assertLink("black", "black", Iterables.get(matches, 3));
  }


  @Test
  public void testOverlappingMatches2() {
    final IWitness witnessA = engine.createWitness("A", "the black cat and the black mat");
    final IWitness witnessB = engine.createWitness("B", "the black dog and the black mat");
    final IVariantGraph graph = engine.graph(witnessA);
    Set<Map.Entry<INormalizedToken,INormalizedToken>> matches = linkTokens(graph, witnessB).entrySet();
    assertEquals(6, matches.size());
    assertLink("and", "and", Iterables.get(matches, 0));
    assertLink("mat", "mat", Iterables.get(matches, 1));
    assertLink("the", "the", Iterables.get(matches, 2));
    assertLink("black", "black", Iterables.get(matches, 3));
    assertLink("the", "the", Iterables.get(matches, 4));
    assertLink("black", "black", Iterables.get(matches, 5));
  }

  @Test
  public void testMatchesWithIndex() {
    final IWitness witnessA = engine.createWitness("A", "The black cat");
    final IWitness witnessB = engine.createWitness("B", "The black and white cat");
    final IVariantGraph graph = engine.graph(witnessA);
    Set<Map.Entry<INormalizedToken,INormalizedToken>> matches = linkTokens(graph, witnessB).entrySet();
    assertEquals(3, matches.size());
    assertLink("the", "the", Iterables.get(matches, 0));
    assertLink("black", "black", Iterables.get(matches, 1));
    assertLink("cat", "cat", Iterables.get(matches, 2));
  }

  @Test
  public void testTwoEqualPossibilities2() {
    // test a a -> a
    final IWitness witnessA = engine.createWitness("A", "a a");
    final IWitness witnessB = engine.createWitness("B", "a");
    final IVariantGraph graph = engine.graph(witnessA);

    final Set<Map.Entry<INormalizedToken,INormalizedToken>> matches = linkTokens(graph, witnessB).entrySet();
    assertEquals(1, matches.size());

    final Map.Entry<INormalizedToken, INormalizedToken> match = Iterables.get(matches, 0);
    assertEquals(graph.getTokens(witnessA).get(0).getNormalized(), match.getKey().getNormalized());
    assertEquals(witnessB.getTokens().get(0).getNormalized(), match.getValue().getNormalized());
  }
}
