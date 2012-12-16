package eu.interedition.collatex.dekker;

import static eu.interedition.collatex.dekker.Match.PHRASE_MATCH_TO_TOKENS;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import eu.interedition.collatex.neo4j.Neo4jVariantGraph;
import eu.interedition.collatex.neo4j.Neo4jVariantGraphVertex;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.matching.Matches;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.simple.WhitespaceAndPunctuationTokenizer;

public class BeckettTest extends AbstractTest {

  @Test
  public void dirkVincent() {
    final SimpleWitness[] w = createWitnesses(//
        "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",//
        "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    final Neo4jVariantGraph graph = collate(w[0]);
    final ListMultimap<Token, Neo4jVariantGraphVertex> matches = Matches.between(graph.vertices(), w[1], new EqualityTokenComparator()).getAll();

    assertVertexHasContent(matches.get(w[1].getTokens().get(0)).get(0), "its", w[0]);
    assertEquals(2, matches.get(w[1].getTokens().get(3)).size()); // 2 matches for 'light'
  }

  @Test
  public void dirkVincentWithMatchMatrixLinker() {
    setCollationAlgorithm(CollationAlgorithmFactory.dekkerMatchMatrix(new EqualityTokenComparator(), 1));
    final SimpleWitness[] w = createWitnesses(//
        "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",//
        "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    final Neo4jVariantGraph graph = collate(w[0]);
    final ListMultimap<Token, Neo4jVariantGraphVertex> matches = Matches.between(graph.vertices(), w[1], new EqualityTokenComparator()).getAll();

    assertVertexHasContent(matches.get(w[1].getTokens().get(0)).get(0), "its", w[0]);
    assertEquals(2, matches.get(w[1].getTokens().get(3)).size()); // 2 matches for 'light'
  }

  @Test
  public void dirkVincent5() {
    final SimpleWitness[] w = createWitnesses("Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    final Neo4jVariantGraph graph = collate(w);

    vertexWith(graph, "its", w[0]);
    vertexWith(graph, "soft", w[0]);
    vertexWith(graph, "light", w[0]);
    vertexWith(graph, "neither", w[0]);
    vertexWith(graph, "daylight", w[0]);
  }

  @Test
  public void dirkVincent6() {
    final SimpleWitness[] w = createWitnesses("Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",//
        "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    final Neo4jVariantGraph graph = collate(w);

    final Neo4jVariantGraphVertex itsVertex = vertexWith(graph, "its", w[0]);
    final Neo4jVariantGraphVertex softVertex = vertexWith(graph, "soft", w[0]);
    final Neo4jVariantGraphVertex changelessVertex = vertexWith(graph, "changeless", w[1]);
    final Neo4jVariantGraphVertex lightVertex = vertexWith(graph, "light", w[0]);

    assertHasWitnesses(edgeBetween(graph.getStart(), itsVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(itsVertex, softVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(softVertex, lightVertex), w[0]);
    assertHasWitnesses(edgeBetween(softVertex, changelessVertex), w[1]);
    assertHasWitnesses(edgeBetween(changelessVertex, lightVertex), w[1]);
  }

  @Test
  public void testDirkVincent7() {
    final SimpleWitness[] w = createWitnesses(//
        "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    Neo4jVariantGraph graph = collate(w);
    assertPhraseMatches("its soft","light", "any light he could remember from the days", "nights when day followed", "night", "vice versa");
    assertTrue(Iterables.isEmpty(((DekkerAlgorithm) collationAlgorithm).getTranspositions()));
  }

  @Test
  public void dirkVincent8() {
    final SimpleWitness[] w = createWitnesses(//
        "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",//
        "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.",//
        "Its faint unchanging light unlike any light he could remember from the days & nights when day followed on night & night on day.");
    final Neo4jVariantGraph graph = collate(w[0], w[1]);
    final Matches matches = Matches.between(graph.vertices(), w[2].getTokens(), new EqualityTokenComparator());

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
    final SimpleWitness[] w = createWitnesses("Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",//
        "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.",//
        "Its faint unchanging light unlike any light he could remember from the days & nights when day followed on night & night on day.");
    final Neo4jVariantGraph graph = collate(w);

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
    final SimpleWitness[] w = createWitnesses(new WhitespaceAndPunctuationTokenizer(),//
        "The same clock as when for example Magee once died.",//
        "The same as when for example Magee once died.",//
        "The same as when for example McKee once died .",//
        "The same as when among others Darly once died & left him.",//
        "The same as when Darly among others once died and left him.");

    final Neo4jVariantGraph graph = collate(w[0], w[1]);
    assertGraphContains(graph, "the", "same", "clock", "as", "when", "for", "example", "magee", "once", "died", ".");

    collate(graph, w[2]);
    assertGraphContains(graph, "the", "same", "clock", "as", "when", "for", "example", "magee", "mckee", "once", "died", ".");

    collate(graph, w[3]);
    assertGraphContains(graph, "the", "same", "clock", "as", "when", "for", "example", "magee", "mckee", "among", "others", "darly", "once", "died", "&", "left", "him", ".");

    // transpositions should be handled correctly for this test to succeed
    collate(graph, w[4]);
    final List<List<Match>> phraseMatches = ((DekkerAlgorithm) collationAlgorithm).getPhraseMatches();
    final List<List<Match>> transpositions = ((DekkerAlgorithm) collationAlgorithm).getTranspositions();
    assertEquals("the same", SimpleToken.toString(PHRASE_MATCH_TO_TOKENS.apply(phraseMatches.get(0))));
    assertEquals("as when", SimpleToken.toString(PHRASE_MATCH_TO_TOKENS.apply(phraseMatches.get(1))));
    assertEquals("darly", SimpleToken.toString(PHRASE_MATCH_TO_TOKENS.apply(phraseMatches.get(2))));
    assertEquals("among others", SimpleToken.toString(PHRASE_MATCH_TO_TOKENS.apply(phraseMatches.get(3))));
    assertEquals("once died", SimpleToken.toString(PHRASE_MATCH_TO_TOKENS.apply(phraseMatches.get(4))));
    assertEquals("left him", SimpleToken.toString(PHRASE_MATCH_TO_TOKENS.apply(phraseMatches.get(5))));
    assertEquals(1, transpositions.size());
    assertEquals("darly", SimpleToken.toString(PHRASE_MATCH_TO_TOKENS.apply(transpositions.get(0))));
  }

  private static void assertGraphContains(Neo4jVariantGraph graph, String... expected) {
    SortedSet<String> contents = Sets.newTreeSet();
    for (Witness witness : graph.witnesses()) {
      extractPhrases(contents, graph, witness);
    }
    Assert.assertTrue(contents.containsAll(Arrays.asList(expected)));
  }
}
