package eu.interedition.collatex.implementation.alignment;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.implementation.Tuple;
import eu.interedition.collatex.implementation.graph.VariantGraph;
import eu.interedition.collatex.implementation.graph.VariantGraphVertex;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.implementation.input.WhitespaceAndPunctuationTokenizer;
import eu.interedition.collatex.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex.implementation.matching.Matches;
import eu.interedition.collatex.interfaces.Token;
import eu.interedition.collatex.interfaces.IWitness;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BeckettTest extends AbstractTest {

  @Test
  public void dirkVincent() {
    final IWitness[] w = createWitnesses(//
            "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",//
            "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    final Multimap<Token, Token> matches = Matches.between(w[0], w[1], new EqualityTokenComparator()).getAll();

    assertEquals("Its", Iterables.get(matches.get(w[1].getTokens().get(0)), 0).getContent());
    assertEquals(2, matches.get(w[1].getTokens().get(3)).size()); // 2 matches for 'light'
  }

  @Test
  public void dirkVincent5() {
    final IWitness[] w = createWitnesses("Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    final VariantGraph graph = merge(w);

    vertexWith(graph, "its", w[0]);
    vertexWith(graph, "soft", w[0]);
    vertexWith(graph, "light", w[0]);
    vertexWith(graph, "neither", w[0]);
    vertexWith(graph, "daylight", w[0]);
  }

  @Test
  public void dirkVincent6() {
    final IWitness[] w = createWitnesses(
            "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",//
            "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    final VariantGraph graph = merge(w);

    final VariantGraphVertex itsVertex = vertexWith(graph, "its", w[0]);
    final VariantGraphVertex softVertex = vertexWith(graph, "soft", w[0]);
    final VariantGraphVertex changelessVertex = vertexWith(graph, "changeless", w[1]);
    final VariantGraphVertex lightVertex = vertexWith(graph, "light", w[0]);

    assertHasWitnesses(edgeBetween(graph.getStart(), itsVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(itsVertex, softVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(softVertex, lightVertex), w[0]);
    assertHasWitnesses(edgeBetween(softVertex, changelessVertex), w[1]);
    assertHasWitnesses(edgeBetween(changelessVertex, lightVertex), w[1]);
  }

  @Test
  public void testDirkVincent7() {
    final VariantGraph graph = merge(//
            "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",
            "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    final StringBuilder graphTokens = new StringBuilder();
    for (Token token : VariantGraphWitnessAdapter.create(graph).getTokens()) {
      graphTokens.append(" ").append(((SimpleToken) token).getNormalized());
    }

    assertEquals("# its soft changeless light neither daylight nor moonlight nor starlight nor unlike any light he could remember from the days & and nights when day followed hard on night & and vice versa #", graphTokens.toString().trim());
  }

  @Test
  public void dirkVincent8() {
    final IWitness[] w = createWitnesses(//
            "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",//
            "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.",//
            "Its faint unchanging light unlike any light he could remember from the days & nights when day followed on night & night on day.");
    final Matches matches = Matches.between(VariantGraphWitnessAdapter.create(merge(w[0], w[1])), w[2], new EqualityTokenComparator());

    final Set<Token> unmatchedTokens = matches.getUnmatched();
    final Set<Token> unsureTokens = matches.getAmbiguous();
    final List<Token> w2Tokens = w[2].getTokens();

    assertTrue(unmatchedTokens.contains(w2Tokens.get(1)));
    assertTrue(unmatchedTokens.contains(w2Tokens.get(2)));
    assertTrue(unsureTokens.contains(w2Tokens.get(3)));
    assertTrue(unsureTokens.contains(w2Tokens.get(6)));
    assertTrue(unsureTokens.contains(w2Tokens.get(13))); // &
    assertTrue(unsureTokens.contains(w2Tokens.get(16))); // day
  }

  @Test
  public void dirkVincent10() {
    final IWitness[] w = createWitnesses(
            "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",//
            "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.",//
            "Its faint unchanging light unlike any light he could remember from the days & nights when day followed on night & night on day.");
    final VariantGraph graph = merge(w);

    vertexWith(graph, "its", w[0]);
    vertexWith(graph, "soft", w[0]);
    vertexWith(graph, "changeless", w[1]);
    vertexWith(graph, "faint", w[2]);
    vertexWith(graph, "unchanging", w[2]);
    vertexWith(graph, "light", w[0]);
    vertexWith(graph, "neither", w[0]);
    vertexWith(graph, "daylight", w[0]);
    vertexWith(graph, "nor", w[0]);
    vertexWith(graph, "moonlight", w[0]);
    vertexWith(graph, "starlight", w[0]);

    // FIXME: test this!
    /*
    assertEquals("its", iterator.get.getNormalized());
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
    */
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

    final VariantGraph graph = merge(w[0], w[1]);
    assertGraphContains(graph, "the", "same", "clock", "as", "when", "for", "example", "magee", "once", "died", ".");

    merge(graph, w[2]);
    assertGraphContains(graph, "the", "same", "clock", "as", "when", "for", "example", "magee", "mckee", "once", "died", ".");

    merge(graph, w[3]);
    assertGraphContains(graph, "the", "same", "clock", "as", "when", "for", "example", "magee", "mckee", "among", "others", "darly", "once", "died", "&", "left", "him", ".");

    // transpositions should be handled correctly for this test to succeed
    final VariantGraphBuilder builder = merge(graph, w[4]);
    final List<Tuple<List<Token>>> phraseMatches = builder.getPhraseMatches();
    final List<Tuple<List<Token>>> transpositions = builder.getTranspositions();
    assertEquals("The same as when", SimpleToken.toString(phraseMatches.get(0).right));
    assertEquals("Darly", SimpleToken.toString(phraseMatches.get(1).right));
    assertEquals("among others", SimpleToken.toString(phraseMatches.get(2).right));
    assertEquals("once died left him .", SimpleToken.toString(phraseMatches.get(3).right));
    assertEquals("darly", SimpleToken.toString(transpositions.get(0).right));
    assertEquals("among others", SimpleToken.toString(transpositions.get(1).right));
  }



  private static void assertGraphContains(VariantGraph graph, String... expected) {
    SortedSet<String> contents = Sets.newTreeSet();
    for (IWitness witness : graph.witnesses()) {
      extractPhrases(contents, graph, witness);
    }
    Assert.assertTrue(contents.containsAll(Arrays.asList(expected)));
  }
}
