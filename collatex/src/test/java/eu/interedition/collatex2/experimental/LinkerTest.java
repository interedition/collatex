package eu.interedition.collatex2.experimental;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraph2;
import eu.interedition.collatex2.implementation.vg_alignment.TokenMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class LinkerTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new MyNewCollateXEngine();
  }

  //convenience method
  private List<ITokenMatch> createTokenMatches2(final IVariantGraph graph, final IWitness witnessB) {
    IWitness superbase = new SuperbaseCreator().create(graph);
    MyNewLinker linker = new MyNewLinker();
    Map<INormalizedToken, INormalizedToken> tokens = linker.link2(superbase, witnessB);
    List<ITokenMatch> matches = Lists.newArrayList();
    for (INormalizedToken witnessToken : tokens.keySet()) {
      INormalizedToken baseToken = tokens.get(witnessToken);
      ITokenMatch match = new TokenMatch(baseToken, witnessToken);
      matches.add(match);
    }
    return matches;
  }

  @Test
  public void testDirkVincent4() {
    IWitness a = engine.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = engine.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    MyNewLinker linker = new MyNewLinker();
    Map<INormalizedToken, INormalizedToken> tokens = linker.link2(a, b);
    INormalizedToken itsA = a.getTokens().get(0);
    INormalizedToken itsB = b.getTokens().get(0);
    assertEquals(itsA, tokens.get(itsB));
    INormalizedToken lightA = a.getTokens().get(2);
    INormalizedToken lightB = b.getTokens().get(3);
    assertEquals(lightA, tokens.get(lightB));
    INormalizedToken light2A = a.getTokens().get(11);
    INormalizedToken light2B = b.getTokens().get(6);
    assertEquals(light2A, tokens.get(light2B));
  }
  
  @Test
  public void testDirkVincent9() {
    // lots of setup
    IWitness a = engine.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = engine.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    IVariantGraph graph = new VariantGraph2();
    MyNewAligner aligner = new MyNewAligner(graph);
    aligner.addWitness(a);
    aligner.addWitness(b);
    SuperbaseCreator creator = new SuperbaseCreator();
    IWitness superbase = creator.create(graph);
    // real test starts here
    // System.out.println("Real TEST STARTS HERE!");
    IWitness c = engine.createWitness("11", "Its faint unchanging light unlike any light he could remember from the days & nights when day followed on night & night on day.");
    //NOTE: this really should be the wrapper by the real aligner
    //TODO: the variantgraph builder stuff should be renamed!
    MyNewLinker linker = new MyNewLinker();
    Map<INormalizedToken, INormalizedToken> link = linker.link2(superbase, c);
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
    IVariantGraph graph = new VariantGraph2();
    MyNewAligner aligner = new MyNewAligner(graph);
    aligner.add(a);
    SuperbaseCreator superbaseCreator = new SuperbaseCreator();
    IWitness superbase = superbaseCreator.create(graph);
    MyNewLinker linker = new MyNewLinker();
    Map<INormalizedToken, INormalizedToken> link = linker.link2(superbase, b);
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
    IWitness superbase = new SuperbaseCreator().create(graph);
    IWitness c = engine.createWitness("C", "very delitied and happy is the cat");
    MyNewLinker linker = new MyNewLinker();
    Map<INormalizedToken, INormalizedToken> tokens = linker.link2(superbase, c);
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
    IWitness superbase = new SuperbaseCreator().create(graph);
    MyNewLinker linker = new MyNewLinker();
    Map<INormalizedToken, INormalizedToken> tokens = linker.link2(superbase, witnessB);
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
    List<ITokenMatch> matches = createTokenMatches2(graph, witnessB);
    assertEquals(3, matches.size());
    assertEquals("everything: 1 -> [everything]", matches.get(0).toString());
    assertEquals("is: 2 -> [is]", matches.get(1).toString());
    assertEquals("unique: 3 -> [unique]", matches.get(2).toString());
  }


  @Test
  public void testEverythingIsUniqueTwoWitnesses() {
    final IWitness witnessA = engine.createWitness("A", "everything is unique should be no problem");
    final IWitness witnessB = engine.createWitness("B", "this one very different");
    final IWitness witnessC = engine.createWitness("C", "everything is different");
    IVariantGraph graph = engine.graph(witnessA, witnessB);
    List<ITokenMatch> matches = createTokenMatches2(graph, witnessC);
    assertEquals(3, matches.size());
    assertEquals("everything", matches.get(0).getNormalized());
    assertEquals("is", matches.get(1).getNormalized());
    assertEquals("different", matches.get(2).getNormalized());
  }

  @Test
  public void testOverlappingMatches() {
    final IWitness witnessA = engine.createWitness("A", "everything is unique should be no problem");
    final IWitness witnessB = engine.createWitness("B", "this one is different");
    final IWitness witnessC = engine.createWitness("C", "everything is different");
    IVariantGraph graph = engine.graph(witnessA, witnessB);
    List<ITokenMatch> matches = createTokenMatches2(graph, witnessC);
    assertEquals(3, matches.size());
    assertEquals("everything", matches.get(0).getNormalized());
    assertEquals("is", matches.get(1).getNormalized());
    assertEquals("different", matches.get(2).getNormalized());
  }
  
  @Test
  public void testGetMatchesUsingWitnessIndex() {
    final IWitness witnessA = engine.createWitness("A", "The big black cat and the big black rat");
    final IWitness witnessB = engine.createWitness("B", "The big black");
    final IVariantGraph graph = engine.graph(witnessA);
    List<ITokenMatch> matches = createTokenMatches2(graph, witnessB);
    assertEquals(3, matches.size());
    assertEquals("the: 1 -> [the]", matches.get(0).toString());
    assertEquals("big: 2 -> [big]", matches.get(1).toString());
    assertEquals("black: 3 -> [black]", matches.get(2).toString());
  }

  //Note: internally this gives # the big black and the big black cat as matches
  @Test
  public void testGetMatchesUsingWitnessIndexWithOverlapping() {
    final IWitness witnessA = engine.createWitness("A", "the big black cat and the big black rat");
    final IWitness witnessB = engine.createWitness("B", "the big black cat");
    final IVariantGraph graph = engine.graph(witnessA);
    List<ITokenMatch> matches = createTokenMatches2(graph, witnessB);
    assertEquals(4, matches.size());
    assertEquals("cat: 4 -> [cat]", matches.get(0).toString());
    assertEquals("the: 1 -> [the]", matches.get(1).toString());
    assertEquals("big: 2 -> [big]", matches.get(2).toString());
    assertEquals("black: 3 -> [black]", matches.get(3).toString());
  }


  @Test
  public void testOverlappingMatches2() {
    final IWitness witnessA = engine.createWitness("A", "the black cat and the black mat");
    final IWitness witnessB = engine.createWitness("B", "the black dog and the black mat");
    final IVariantGraph graph = engine.graph(witnessA);
    List<ITokenMatch> matches = createTokenMatches2(graph, witnessB);
    assertEquals(6, matches.size());
    assertEquals("and: 4 -> [and]", matches.get(0).toString());
    assertEquals("mat: 7 -> [mat]", matches.get(1).toString());
    assertEquals("the: 1 -> [the]", matches.get(2).toString());
    assertEquals("black: 2 -> [black]", matches.get(3).toString());
    assertEquals("the: 5 -> [the]", matches.get(4).toString());
    assertEquals("black: 6 -> [black]", matches.get(5).toString());
  }

  @Test
  public void testMatchesWithIndex() {
    final IWitness witnessA = engine.createWitness("A", "The black cat");
    final IWitness witnessB = engine.createWitness("B", "The black and white cat");
    final IVariantGraph graph = engine.graph(witnessA);
    List<ITokenMatch> matches = createTokenMatches2(graph, witnessB);
    assertEquals(3, matches.size());
    assertEquals("the: 1 -> [the]", matches.get(0).toString());
    assertEquals("black: 2 -> [black]", matches.get(1).toString());
    assertEquals("cat: 5 -> [cat]", matches.get(2).toString());
  }

  @Ignore
  @Test
  public void testTwoEqualPossibilities2() {
    // test a a -> a
    final IWitness witnessA = engine.createWitness("A", "a a");
    final IWitness witnessB = engine.createWitness("B", "a");
    final IVariantGraph graph = engine.graph(witnessA);
    List<ITokenMatch> matches = createTokenMatches2(graph, witnessB);
    assertEquals(1, matches.size());
    ITokenMatch match = matches.get(0);
    assertEquals(graph.getTokens(witnessA).get(0), match.getBaseToken());
    assertEquals(witnessB.getTokens().get(0), match.getWitnessToken());
  }
}
