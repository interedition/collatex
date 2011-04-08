package eu.interedition.collatex2.experimental;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ListMultimap;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraph2;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class DeTestDirkVincent {

  @Test
  public void testDirkVincent() {
    CollateXEngine factory = new CollateXEngine();
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    MyNewMatcher matcher = new MyNewMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b);
    INormalizedToken its = b.getTokens().get(0);
    INormalizedToken light = b.getTokens().get(3);
    List<INormalizedToken> matchedTokens;
    matchedTokens = matches.get(its);
    assertEquals("Its", matchedTokens.get(0).getContent());
    matchedTokens = matches.get(light);
    assertEquals(2, matchedTokens.size());
  }
  
  @Test
  public void testDirkVincent2() {
    CollateXEngine factory = new CollateXEngine();
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    MyNewMatcher matcher = new MyNewMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b);
    //TODO: nu moet ik een witness index maken van de base
    //met de tokens erin die een unieke sequence voorstellen voor dubbelvoorkomend woord
    IWitnessIndex index = new MyNewWitnessIndex(b, matches);
    List<ITokenSequence> sequences = index.getTokenSequences();
    INormalizedToken soft = b.getTokens().get(1);
    INormalizedToken light = b.getTokens().get(3);
    ITokenSequence expectedSequence = new TokenSequence(soft, light);
    ITokenSequence firstSequence = sequences.get(0);
    assertEquals(expectedSequence, firstSequence);
    INormalizedToken any = b.getTokens().get(5);
    INormalizedToken light2 = b.getTokens().get(6);
    ITokenSequence secondSequence = sequences.get(1);
    expectedSequence = new TokenSequence(any, light2);
    assertEquals(expectedSequence, secondSequence);
  }

  @Test
  public void testVincentDirk3() {
    CollateXEngine factory = new CollateXEngine();
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    MyNewMatcher matcher = new MyNewMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b);
    WitnessAfgeleide afgeleider = new WitnessAfgeleide();
    List<INormalizedToken> afgeleideWitness = afgeleider.calculateAfgeleide(a, matches);
    Iterator<INormalizedToken> tokenIterator = afgeleideWitness.iterator();
    assertEquals("Its", tokenIterator.next().getContent());
    assertEquals("soft", tokenIterator.next().getContent());
    assertEquals("light", tokenIterator.next().getContent());
    assertEquals("any", tokenIterator.next().getContent());
    assertEquals("light", tokenIterator.next().getContent());
    assertEquals("he", tokenIterator.next().getContent());
    assertEquals("could", tokenIterator.next().getContent());
  }
  
  
  @Test
  public void testDirkVincent4() {
    CollateXEngine factory = new CollateXEngine();
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    MyNewLinker linker = new MyNewLinker();
    Map<INormalizedToken, INormalizedToken> tokens = linker.link(a, b);
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
  public void testDirkVincent5() {
    CollateXEngine factory = new CollateXEngine();
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
//    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    IVariantGraph graph = new VariantGraph2();
    MyNewAligner aligner = new MyNewAligner(graph);
    aligner.addWitness(a);
    Iterator<IVariantGraphVertex> iterator = graph.iterator();
    assertEquals("#", iterator.next().getNormalized()); // start vertex
    assertEquals("its", iterator.next().getNormalized());
    assertEquals("soft", iterator.next().getNormalized());
    assertEquals("light", iterator.next().getNormalized());
    assertEquals("neither", iterator.next().getNormalized());
    assertEquals("daylight", iterator.next().getNormalized());
  }
  
  @Test
  public void testDirkVincent6() {
    CollateXEngine factory = new CollateXEngine();
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    IVariantGraph graph = new VariantGraph2();
    MyNewAligner aligner = new MyNewAligner(graph);
    aligner.addWitness(a);
    aligner.addWitness(b);
    Iterator<IVariantGraphVertex> iterator = graph.iterator();
    assertEquals("#", iterator.next().getNormalized()); // start vertex
    assertEquals("its", iterator.next().getNormalized());
    assertEquals("soft", iterator.next().getNormalized());
    assertEquals("changeless", iterator.next().getNormalized()); // addition
    assertEquals("light", iterator.next().getNormalized());
  }

  @Test
  public void testDirkVincent7() {
    CollateXEngine factory = new CollateXEngine();
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    IVariantGraph graph = new VariantGraph2();
    MyNewAligner aligner = new MyNewAligner(graph);
    aligner.addWitness(a);
    aligner.addWitness(b);
    SuperbaseCreator creator = new SuperbaseCreator();
    IWitness superbase = creator.create(graph);
    Iterator<INormalizedToken> tokenIterator = superbase.tokenIterator();
    assertEquals("its", tokenIterator.next().getNormalized());
    assertEquals("soft", tokenIterator.next().getNormalized());
    assertEquals("changeless", tokenIterator.next().getNormalized());
    assertEquals("light", tokenIterator.next().getNormalized());
    assertEquals("neither", tokenIterator.next().getNormalized());
    assertEquals("daylight", tokenIterator.next().getNormalized()); 
    assertEquals("nor", tokenIterator.next().getNormalized()); 
    assertEquals("moonlight", tokenIterator.next().getNormalized());
    assertEquals("nor", tokenIterator.next().getNormalized()); 
    assertEquals("starlight", tokenIterator.next().getNormalized());
    assertEquals("nor", tokenIterator.next().getNormalized()); 
    assertEquals("unlike", tokenIterator.next().getNormalized());
    assertEquals("any", tokenIterator.next().getNormalized()); 
    assertEquals("light", tokenIterator.next().getNormalized());
  }

}
