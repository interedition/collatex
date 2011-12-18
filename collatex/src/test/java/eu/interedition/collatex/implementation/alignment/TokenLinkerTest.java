package eu.interedition.collatex.implementation.alignment;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.implementation.graph.VariantGraph;
import eu.interedition.collatex.implementation.graph.VariantGraphVertex;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex.interfaces.Token;
import eu.interedition.collatex.interfaces.IWitness;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TokenLinkerTest extends AbstractTest {

  @Test
  public void testDirkVincent4() {
    final IWitness[] w = createWitnesses(//
            "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",//
            "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");

    final IWitness graph = VariantGraphWitnessAdapter.create(merge(w[0]));
    final Map<Token, VariantGraphVertex> links = linkTokens(graph, w[1]);

    assertEquals(graph.getTokens().get(1), links.get(w[1].getTokens().get(0)).tokens().first()); // 'its'
    assertEquals(graph.getTokens().get(3), links.get(w[1].getTokens().get(3)).tokens().first()); // 'light'
    assertEquals(graph.getTokens().get(12), links.get(w[1].getTokens().get(6)).tokens().first()); // 2nd 'light'
  }

  @Test
  public void dirkVincent9() {
    final IWitness[] w = createWitnesses(//
            "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",//
            "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.",//
            "Its faint unchanging light unlike any light he could remember from the days & nights when day followed on night & night on day.");

    final Map<Token, VariantGraphVertex> links = linkTokens(merge(w[0], w[1]), w[2]);

    final List<Token> unlinked = Lists.newArrayList();
    for (Token witnessToken : w[2].getTokens()) {
      if (!links.containsKey(witnessToken)) {
        unlinked.add(witnessToken);
      }
    }
    assertTrue(unlinked.contains(w[2].getTokens().get(1))); // faint
    assertTrue(unlinked.contains(w[2].getTokens().get(2))); // unchanging
    assertTrue(unlinked.contains(w[2].getTokens().get(21))); // night
    assertTrue(unlinked.contains(w[2].getTokens().get(22))); // on
    assertTrue(unlinked.contains(w[2].getTokens().get(23))); // day
    assertEquals(5, unlinked.size());
  }

  @Test
  public void vincentDirk3() {
    final IWitness[] w = createWitnesses(//
            "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",//
            "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");

    final TokenLinker linker = new TokenLinker();
    Map<Token, VariantGraphVertex> links = linkTokens(linker, w[0], w[1]);

    assertTrue(links.containsKey(w[1].getTokens().get(0))); // its
    assertTrue(links.containsKey(w[1].getTokens().get(1))); // soft
    assertTrue(links.containsKey(w[1].getTokens().get(3))); // light
    assertTrue(links.containsKey(w[1].getTokens().get(5))); // any
    assertTrue(links.containsKey(w[1].getTokens().get(6))); // light
    assertTrue(links.containsKey(w[1].getTokens().get(7))); // he
    assertTrue(links.containsKey(w[1].getTokens().get(8))); // could
  }


  @Test
  public void linkingWithStartToken() {
    final IWitness[] w = createWitnesses(//
            "So on to no purpose till finally at a stand again to his ears just audible oh how and here some word he could not catch it would be to end somewhere he had never been.",//
            "The next he knew he was stuck still again & to his ears just audible Oh how and here a word he could not catch it were to end where never been.");

    final Map<Token, VariantGraphVertex> links = linkTokens(merge(w[0]), w[1]);
    assertTrue(!links.containsKey(w[1].getTokens().get(0)));
    assertTrue(!links.containsKey(w[1].getTokens().get(1)));
    assertTrue(!links.containsKey(w[1].getTokens().get(2)));
    assertTrue(!links.containsKey(w[1].getTokens().get(3)));
    assertTrue(!links.containsKey(w[1].getTokens().get(4)));
    assertTrue(!links.containsKey(w[1].getTokens().get(5)));
    assertTrue(!links.containsKey(w[1].getTokens().get(6)));
    assertTrue(!links.containsKey(w[1].getTokens().get(7)));
    assertTrue(links.containsKey(w[1].getTokens().get(8))); // again
    assertTrue(!links.containsKey(w[1].getTokens().get(9)));
    assertTrue(links.containsKey(w[1].getTokens().get(10))); // to
    assertTrue(links.containsKey(w[1].getTokens().get(11))); // his
  }

  @Test
  public void linkingRepetitionCausedByTransposition() {
   IWitness[] w = createWitnesses(//
      "the cat is very happy", //
      "very happy is the cat", // 
      "very delitied and happy is the cat"
           );
    VariantGraph graph = merge(w[0], w[1]);
    VariantGraphBuilder builder = merge(graph, w[2]);
    assertPhraseMatches(builder, "very happy is the cat");
    assertTrue(Iterables.isEmpty(builder.getTranspositions()));
  }

  @Test
  public void twoEqualPossibilities1() {
    final IWitness[] w = createWitnesses("a", "a a");

    final VariantGraph graph = merge(w[0]);
    final Map<Token, VariantGraphVertex> links = linkTokens(graph, w[1]);

    assertEquals(1, links.size());
    assertEquals(vertexWith(graph, "a", w[0]), links.get(w[1].getTokens().get(0)));
  }


  @Test
  public void everythingIsUnique() {
    final IWitness[] w = createWitnesses("everything is unique should be no problem", "everything is unique");

    final Set<Map.Entry<Token,VariantGraphVertex>> matches = linkTokens(merge(w[0]), w[1]).entrySet();

    assertEquals(3, matches.size());
    assertLink("everything", "everything", Iterables.get(matches, 0));
    assertLink("is", "is", Iterables.get(matches, 1));
    assertLink("unique", "unique", Iterables.get(matches, 2));
  }

  @Test
  public void everythingIsUniqueTwoWitnesses() {
    final IWitness[] w = createWitnesses(//
            "everything is unique should be no problem",//
            "this one very different",//
            "everything is different");

    final Set<Map.Entry<Token,VariantGraphVertex>> matches = linkTokens(merge(w[0], w[1]), w[2]).entrySet();

    assertEquals(3, matches.size());
    assertEquals("everything", Iterables.get(matches, 0).getValue().tokens().first().getContent());
    assertEquals("is", Iterables.get(matches, 1).getValue().tokens().first().getContent());
    assertEquals("different", Iterables.get(matches, 2).getValue().tokens().first().getContent());
  }

  @Test
  public void overlappingMatches() {
    final IWitness[] w = createWitnesses(//
            "everything is unique should be no problem",//
            "this one is different",//
            "everything is different");

    final Set<Map.Entry<Token,VariantGraphVertex>> matches = linkTokens(merge(w[0], w[1]), w[2]).entrySet();

    assertEquals(3, matches.size());
    assertEquals("everything", Iterables.get(matches, 0).getValue().tokens().first().getContent());
    assertEquals("is", Iterables.get(matches, 1).getValue().tokens().first().getContent());
    assertEquals("different", Iterables.get(matches, 2).getValue().tokens().first().getContent());
  }

  @Test
  public void getMatchesUsingWitnessIndex() {
    final IWitness[] w = createWitnesses("The big black cat and the big black rat", "The big black");

    final Set<Map.Entry<Token,VariantGraphVertex>> matches = linkTokens(merge(w[0]), w[1]).entrySet();

    assertEquals(3, matches.size());
    assertLink("the", "the", Iterables.get(matches, 0));
    assertLink("big", "big", Iterables.get(matches, 1));
    assertLink("black", "black", Iterables.get(matches, 2));
  }

  @Test
  public void getMatchesUsingWitnessIndexWithOverlapping() {
    final IWitness[] w = createWitnesses("the big black cat and the big black rat", "the big black cat");

    final Set<Map.Entry<Token,VariantGraphVertex>> matches = linkTokens(merge(w[0]), w[1]).entrySet();

    assertEquals(4, matches.size());
    assertLink("cat", "cat", Iterables.get(matches, 0));
    assertLink("the", "the", Iterables.get(matches, 1));
    assertLink("big", "big", Iterables.get(matches, 2));
    assertLink("black", "black", Iterables.get(matches, 3));
  }


  @Test
  public void overlappingMatches2() {
    final IWitness[] w = createWitnesses("the black cat and the black mat", "the black dog and the black mat");

    final Set<Map.Entry<Token,VariantGraphVertex>> matches = linkTokens(merge(w[0]), w[1]).entrySet();

    assertEquals(6, matches.size());
    assertLink("and", "and", Iterables.get(matches, 0));
    assertLink("mat", "mat", Iterables.get(matches, 1));
    assertLink("the", "the", Iterables.get(matches, 2));
    assertLink("black", "black", Iterables.get(matches, 3));
    assertLink("the", "the", Iterables.get(matches, 4));
    assertLink("black", "black", Iterables.get(matches, 5));
  }

  @Test
  public void matchesWithIndex() {
    final IWitness[] w = createWitnesses("The black cat", "The black and white cat");

    final Set<Map.Entry<Token,VariantGraphVertex>> matches = linkTokens(merge(w[0]), w[1]).entrySet();

    assertEquals(3, matches.size());
    assertLink("the", "the", Iterables.get(matches, 0));
    assertLink("black", "black", Iterables.get(matches, 1));
    assertLink("cat", "cat", Iterables.get(matches, 2));
  }

  @Test
  public void twoEqualPossibilities2() {
    final IWitness[] w = createWitnesses("a a", "a");

    final VariantGraph graph = merge(w[0]);
    final Set<Map.Entry<Token,VariantGraphVertex>> matches = linkTokens(graph, w[1]).entrySet();

    assertEquals(1, matches.size());
    final Map.Entry<Token, VariantGraphVertex> match = Iterables.get(matches, 0);
    assertEquals(vertexWith(graph, "a", w[0]), match.getValue());
    assertEquals(((SimpleToken) vertexWith(graph, "a", w[0]).tokens().first()).getNormalized(), ((SimpleToken) match.getKey()).getNormalized());
  }

  @Test
  public void witnessIndexingDirkVincent2() {
    final IWitness[] w = createWitnesses(//
            "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.",//
            "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");

    final TokenLinker linker = new TokenLinker();
    linkTokens(linker, w[0], w[1]);

    final Token soft = w[1].getTokens().get(1);
    final Token light = w[1].getTokens().get(3);
    final Token any = w[1].getTokens().get(5);
    final Token light2 = w[1].getTokens().get(6);

    final List<List<Token>> rightExpandingPhrases = linker.getRightExpandingPhrases();
    assertEquals(Lists.newArrayList(soft, light), rightExpandingPhrases.get(0));
    assertEquals(Lists.newArrayList(any, light2), rightExpandingPhrases.get(1));

    final List<List<Token>> leftExpandingPhrases = linker.getLeftExpandingPhrases();
    assertEquals(Lists.newArrayList(light, any), leftExpandingPhrases.get(0));
  }

  @Test
  public void witnessIndexingRepetitionCausedByTransposition() {
    final IWitness[] w = createWitnesses(//
            "the cat very happy is very happy the cat",//
            "very delitied and happy is the cat");

    final TokenLinker linker = new TokenLinker();
    linkTokens(linker, w[0], w[1]);

    final List<List<Token>> rightExpandingPhrases = linker.getRightExpandingPhrases();
    final List<List<Token>> leftExpandingPhrases = linker.getLeftExpandingPhrases();

    assertEquals("# very", SimpleToken.toString(rightExpandingPhrases.get(0)));
    assertEquals("very happy is", SimpleToken.toString(leftExpandingPhrases.get(0)));
    assertEquals("# very happy", SimpleToken.toString(rightExpandingPhrases.get(1)));
    assertEquals("happy is", SimpleToken.toString(leftExpandingPhrases.get(1)));
    assertEquals("is the", SimpleToken.toString(rightExpandingPhrases.get(2)));
    assertEquals("the cat #", SimpleToken.toString(leftExpandingPhrases.get(2)));
    assertEquals("is the cat", SimpleToken.toString(rightExpandingPhrases.get(3)));
    assertEquals("cat #", SimpleToken.toString(leftExpandingPhrases.get(3)));
  }

  private Map<Token, VariantGraphVertex> linkTokens(TokenLinker linker, IWitness base, IWitness witness) {
    final VariantGraph graph = merge(base);
    return linker.link(graph, witness.getTokens(), new EqualityTokenComparator());
  }

  private Map<Token, VariantGraphVertex> linkTokens(TokenLinker linker, VariantGraph base, IWitness witness) {
    return linker.link(base, witness.getTokens(), new EqualityTokenComparator());
  }

  private Map<Token, VariantGraphVertex> linkTokens(IWitness base, IWitness witness) {
    return linkTokens(new TokenLinker(), base, witness);
  }

  private Map<Token, VariantGraphVertex> linkTokens(VariantGraph base, IWitness witness) {
    return linkTokens(new TokenLinker(), base, witness);
  }

  private static void assertLink(String left, String right, Map.Entry<Token, VariantGraphVertex> match) {
    final SimpleToken token = (SimpleToken) match.getKey();
    assertEquals(left, token.getNormalized());
    for (SimpleToken vertexToken : Iterables.filter(match.getValue().tokens(), SimpleToken.class)) {
      if (!vertexToken.getWitness().equals(token.getWitness())) {
        assertEquals(right, vertexToken.getNormalized());
        return;
      }
    }
    fail();
  }
}
