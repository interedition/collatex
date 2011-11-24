package eu.interedition.collatex2.experimental;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import eu.interedition.collatex2.AbstractTest;
import eu.interedition.collatex2.implementation.Tuple;
import eu.interedition.collatex2.implementation.alignment.VariantGraphBuilder;
import eu.interedition.collatex2.implementation.alignment.VariantGraphWitnessAdapter;
import eu.interedition.collatex2.implementation.input.NormalizedToken;
import eu.interedition.collatex2.implementation.input.Token;
import eu.interedition.collatex2.implementation.input.tokenization.WhitespaceAndPunctuationTokenizer;
import eu.interedition.collatex2.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex2.implementation.matching.Matches;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BeckettTest extends AbstractTest {

  @Test
  public void dirkVincent() {
    final IWitness[] w = createWitnesses(//
            "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",//
            "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    final Multimap<INormalizedToken, INormalizedToken> matches = Matches.between(w[0], w[1], new EqualityTokenComparator()).getAll();

    assertEquals("Its", Iterables.get(matches.get(w[1].getTokens().get(0)), 0).getContent());
    assertEquals(2, matches.get(w[1].getTokens().get(3)).size()); // 2 matches for 'light'
  }

  @Test
  public void dirkVincent5() {
    final IVariantGraph graph = merge("Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    final Iterator<IVariantGraphVertex> iterator = graph.iterator();

    assertEquals("#", iterator.next().getNormalized()); // start vertex
    assertEquals("its", iterator.next().getNormalized());
    assertEquals("soft", iterator.next().getNormalized());
    assertEquals("light", iterator.next().getNormalized());
    assertEquals("neither", iterator.next().getNormalized());
    assertEquals("daylight", iterator.next().getNormalized());
  }

  @Test
  public void dirkVincent6() {
    final IVariantGraph graph = merge(//
            "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",//
            "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    final Iterator<IVariantGraphVertex> iterator = graph.iterator();

    assertEquals("#", iterator.next().getNormalized()); // start vertex
    assertEquals("its", iterator.next().getNormalized());
    assertEquals("soft", iterator.next().getNormalized());
    assertEquals("changeless", iterator.next().getNormalized()); // addition
    assertEquals("light", iterator.next().getNormalized());
  }

  @Test
  public void testDirkVincent7() {
    final IVariantGraph graph = merge(//
            "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",
            "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    final Iterator<INormalizedToken> tokenIterator = VariantGraphWitnessAdapter.create(graph).tokenIterator();

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
  public void dirkVincent8() {
    final IWitness[] w = createWitnesses(//
            "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",//
            "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.",//
            "Its faint unchanging light unlike any light he could remember from the days & nights when day followed on night & night on day.");
    final Matches matches = Matches.between(VariantGraphWitnessAdapter.create(merge(w[0], w[1])), w[2], new EqualityTokenComparator());

    final Set<INormalizedToken> unmatchedTokens = matches.getUnmatched();
    final Set<INormalizedToken> unsureTokens = matches.getAmbiguous();
    final List<INormalizedToken> w2Tokens = w[2].getTokens();

    assertTrue(unmatchedTokens.contains(w2Tokens.get(1)));
    assertTrue(unmatchedTokens.contains(w2Tokens.get(2)));
    assertTrue(unsureTokens.contains(w2Tokens.get(3)));
    assertTrue(unsureTokens.contains(w2Tokens.get(6)));
    assertTrue(unsureTokens.contains(w2Tokens.get(13))); // &
    assertTrue(unsureTokens.contains(w2Tokens.get(16))); // day
  }

  @Test
  public void dirkVincent10() {
    final Iterator<IVariantGraphVertex> iterator = merge(//
            "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",//
            "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.",//
            "Its faint unchanging light unlike any light he could remember from the days & nights when day followed on night & night on day.").iterator();

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

  @Test
  public void sentence42Transposition() {
    // punctuation should be treated as separate tokens for this test to succeed
    final IWitness[] w = createWitnesses(witnessBuilder, new WhitespaceAndPunctuationTokenizer(),//
            "The same clock as when for example Magee once died.",//
            "The same as when for example Magee once died.",//
            "The same as when for example McKee once died .",//
            "The same as when among others Darly once died & left him.",//
            "The same as when Darly among others once died and left him.");

    final IVariantGraph graph = merge(w[0], w[1]);
    assertGraphContains(graph, "the", "same", "clock", "as", "when", "for", "example", "magee", "once", "died", ".");

    merge(graph, w[2]);
    assertGraphContains(graph, "the", "same", "clock", "as", "when", "for", "example", "magee", "mckee", "once", "died", ".");

    merge(graph, w[3]);
    assertGraphContains(graph, "the", "same", "clock", "as", "when", "for", "example", "magee", "mckee", "among", "others", "darly", "once", "died", "&", "left", "him", ".");

    // transpositions should be handled correctly for this test to succeed
    final VariantGraphBuilder builder = merge(graph, w[4]);
    final List<Tuple<List<INormalizedToken>>> phraseMatches = builder.getPhraseMatches();
    final List<Tuple<Tuple<List<INormalizedToken>>>> transpositions = builder.getTranspositions();
    assertEquals("The same as when", Token.toString(phraseMatches.get(0).right));
    assertEquals("Darly", Token.toString(phraseMatches.get(1).right));
    assertEquals("among others", Token.toString(phraseMatches.get(2).right));
    assertEquals("once died left him .", Token.toString(phraseMatches.get(3).right));
    assertEquals("darly", NormalizedToken.toString(transpositions.get(0).right.right));
    assertEquals("among others", NormalizedToken.toString(transpositions.get(1).right.right));
  }



  private static void assertGraphContains(IVariantGraph graph, String... expected) {
    final Iterator<IVariantGraphVertex> iterator = graph.iterator();
    assertEquals(graph.getStartVertex(), iterator.next());
    for (String exp : expected) {
      assertTrue(iterator.hasNext());
      IVariantGraphVertex vertex = iterator.next();
      assertEquals(exp, vertex.getNormalized());
    }
    assertEquals(graph.getEndVertex(), iterator.next());
    assertTrue(!iterator.hasNext());
  }
}
