package eu.interedition.collatex2.experimental;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ListMultimap;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraph2;
import eu.interedition.collatex2.implementation.input.tokenization.WhitespaceAndPunctuationTokenizer;
import eu.interedition.collatex2.implementation.matching.IMatchResult;
import eu.interedition.collatex2.implementation.matching.MatchResultAnalyzer;
import eu.interedition.collatex2.implementation.matching.TokenMatcher;
import eu.interedition.collatex2.implementation.vg_alignment.BaseAfgeleider;
import eu.interedition.collatex2.implementation.vg_alignment.SuperbaseCreator;
import eu.interedition.collatex2.implementation.vg_alignment.VariantGraphAligner;
import eu.interedition.collatex2.implementation.vg_analysis.IAnalysis;
import eu.interedition.collatex2.implementation.vg_analysis.ISequence;
import eu.interedition.collatex2.implementation.vg_analysis.ITransposition2;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class DeTestDirkVincent {

  private static CollateXEngine factory = new CollateXEngine();

  // helper method
  private void checkGraph(IVariantGraph graph, String... expected) {
    Iterator<IVariantGraphVertex> iterator = graph.iterator();
    assertEquals(graph.getStartVertex(), iterator.next());
    for (String exp : expected) {
      assertTrue(iterator.hasNext());
      IVariantGraphVertex vertex = iterator.next();
      assertEquals(exp, vertex.getNormalized());
    }
    assertEquals(graph.getEndVertex(), iterator.next());
    assertTrue(!iterator.hasNext());
  }

  @Test
  public void testDirkVincent() {
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    TokenMatcher matcher = new TokenMatcher();
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
  public void testVincentDirk3() {
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    TokenMatcher matcher = new TokenMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b);
    BaseAfgeleider afgeleider = new BaseAfgeleider();
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
  public void testDirkVincent5() {
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IVariantGraph graph = new VariantGraph2();
    VariantGraphAligner aligner = new VariantGraphAligner(graph);
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
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    IVariantGraph graph = new VariantGraph2();
    VariantGraphAligner aligner = new VariantGraphAligner(graph);
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
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    IVariantGraph graph = new VariantGraph2();
    VariantGraphAligner aligner = new VariantGraphAligner(graph);
    aligner.addWitness(a);
    aligner.addWitness(b);
    SuperbaseCreator creator = new SuperbaseCreator();
    IWitness superbase = creator.create(graph);
    Iterator<INormalizedToken> tokenIterator = superbase.tokenIterator();
    assertEquals("#", tokenIterator.next().getNormalized()); // start vertex
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
  
  @Test
  public void testDirkVincent8() {
    // lots of setup
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    IVariantGraph graph = new VariantGraph2();
    VariantGraphAligner aligner = new VariantGraphAligner(graph);
    aligner.addWitness(a);
    aligner.addWitness(b);
    SuperbaseCreator creator = new SuperbaseCreator();
    IWitness superbase = creator.create(graph);
    IWitness c = factory.createWitness("11", "Its faint unchanging light unlike any light he could remember from the days & nights when day followed on night & night on day.");
    MatchResultAnalyzer analyzer = new MatchResultAnalyzer();
    IMatchResult result = analyzer.analyze(superbase, c);
    Set<INormalizedToken> unmatchedTokens = result.getUnmatchedTokens();
    assertTrue(unmatchedTokens.contains(c.getTokens().get(1)));
    assertTrue(unmatchedTokens.contains(c.getTokens().get(2)));
    Set<INormalizedToken> unsureTokens = result.getUnsureTokens();
    assertTrue(unsureTokens.contains(c.getTokens().get(3)));
    assertTrue(unsureTokens.contains(c.getTokens().get(6)));
    assertTrue(unsureTokens.contains(c.getTokens().get(13))); // &
    assertTrue(unsureTokens.contains(c.getTokens().get(16))); // day
  }
  
  
  //Test alignment with 3 witnesses; superbase should be in aligner to make this test work 
  @Test
  public void testDirkVincent10() {
    // lots of setup
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    IWitness c = factory.createWitness("11", "Its faint unchanging light unlike any light he could remember from the days & nights when day followed on night & night on day.");
    IVariantGraph graph = new VariantGraph2();
    VariantGraphAligner aligner = new VariantGraphAligner(graph);
    aligner.add(a, b, c);
    Iterator<IVariantGraphVertex> iterator = graph.iterator();
    assertEquals("#", iterator.next().getNormalized()); // start vertex
    assertEquals("its", iterator.next().getNormalized());
    assertEquals("soft", iterator.next().getNormalized());
    assertEquals("changeless", iterator.next().getNormalized());
    assertEquals("faint", iterator.next().getNormalized());
    assertEquals("unchanging", iterator.next().getNormalized());
    assertEquals("light", iterator.next().getNormalized());
    assertEquals("neither", iterator.next().getNormalized());
    assertEquals("daylight", iterator.next().getNormalized()); 
    assertEquals("nor", iterator.next().getNormalized()); 
    assertEquals("moonlight", iterator.next().getNormalized());
    assertEquals("nor", iterator.next().getNormalized()); 
    assertEquals("starlight", iterator.next().getNormalized());
    assertEquals("nor", iterator.next().getNormalized()); 
    assertEquals("unlike", iterator.next().getNormalized());
    assertEquals("any", iterator.next().getNormalized());
    assertEquals("light", iterator.next().getNormalized()); 
    assertEquals("he", iterator.next().getNormalized());
    assertEquals("could", iterator.next().getNormalized());
  }
  

  //TODO: enable test!
  @Test
  public void testStartTokenWitnessIndexing() {
    IWitness a = factory.createWitness("a", "So on to no purpose till finally at a stand again to his ears just audible oh how and here some word he could not catch it would be to end somewhere he had never been.");
    IWitness b = factory.createWitness("b", "The next he knew he was stuck still again & to his ears just audible Oh how and here a word he could not catch it were to end where never been.");
//  assert this some how! (information is contained in the linker!
    //    MyNewWitnessIndexer indexer = new MyNewWitnessIndexer();
//    IWitnessIndex index = indexer.index(b, matches, analyze);
////    for (ITokenSequence seq : index.getTokenSequences()) {
////      System.out.println(seq.toString());
////    }
//    Iterator<ITokenSequence> iterator = index.getTokenSequences().iterator();
//    assertEquals("TokenSequence: #: 0, he: 3, ", iterator.next().toString());
  }
    
    

  // punctuation should be treated as separate tokens for this test to succeed
  // transpositions should be handled correctly for this test to succeed
  @Test
  public void testSentence42Transposition() {
    factory.setTokenizer(new WhitespaceAndPunctuationTokenizer());
    IWitness a = factory.createWitness("06-1", "The same clock as when for example Magee once died.");
    IWitness b = factory.createWitness("06-2", "The same as when for example Magee once died.");
    IVariantGraph graph = new VariantGraph2();
    VariantGraphAligner aligner = new VariantGraphAligner(graph);
    aligner.add(a, b);
    checkGraph(graph, "the", "same", "clock", "as", "when", "for", "example", "magee", "once", "died", ".");
    IWitness c = factory.createWitness("08-01","The same as when for example McKee once died .");
    aligner.addWitness(c);
    checkGraph(graph, "the", "same", "clock", "as", "when", "for", "example", "magee", "mckee", "once", "died", ".");
    IWitness d = factory.createWitness("08-02", "The same as when among others Darly once died & left him.");
    aligner.addWitness(d);
    checkGraph(graph, "the", "same", "clock", "as", "when", "for", "example", "magee", "mckee", "among", "others", "darly", "once", "died", "&", "left", "him", ".");
    IWitness e = factory.createWitness("xxx", "The same as when Darly among others once died and left him.");
    aligner.addWitness(e);
    IAnalysis analysis = aligner.getAnalysis();
    List<ISequence> sequences = analysis.getSequences();
    assertEquals("The same as when", sequences.get(0).getWitnessPhrase().getContent());
    assertEquals("Darly", sequences.get(1).getWitnessPhrase().getContent());
    assertEquals("among others", sequences.get(2).getWitnessPhrase().getContent());
    assertEquals("once died left him .", sequences.get(3).getWitnessPhrase().getContent());
    List<ITransposition2> transpositions = analysis.getTranspositions();
    assertEquals("darly", transpositions.get(0).getSequenceB().getNormalized());
    assertEquals("among others", transpositions.get(1).getSequenceB().getNormalized());
  }

  
  // aligner.align(e);
  // aligner.addWitness(e);

}
