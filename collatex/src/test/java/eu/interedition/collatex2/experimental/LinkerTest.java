package eu.interedition.collatex2.experimental;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraph2;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class LinkerTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new MyNewCollateXEngine();
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

}
