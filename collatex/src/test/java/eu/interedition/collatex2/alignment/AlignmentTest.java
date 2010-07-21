package eu.interedition.collatex2.alignment;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.PairwiseAlignmentHelper;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.ITransposition;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTest {
  private static final Logger LOG = LoggerFactory.getLogger(AlignmentTest.class);
  private CollateXEngine factory;

  @Before
  public void setup() {
    factory = new CollateXEngine();
  }


  @Test
  public void testTransposition1Matches() {
    final IWitness a = factory.createWitness("A", "The black dog chases a red cat.");
    final IWitness b = factory.createWitness("B", "A red cat chases the black dog.");
    final IAlignment align = PairwiseAlignmentHelper.align(factory, a, b);
    final List<IMatch> matches = align.getMatches();
    assertEquals(3, matches.size());
    assertEquals("the black dog", matches.get(0).getNormalized());
    assertEquals("chases", matches.get(1).getNormalized());
    assertEquals("a red cat", matches.get(2).getNormalized());
  }

  @Test
  public void testTransposition1Gaps() {
    final IWitness a = factory.createWitness("A", "The black dog chases a red cat.");
    final IWitness b = factory.createWitness("B", "A red cat chases the black dog.");
    final IAlignment align = PairwiseAlignmentHelper.align(factory, a, b);
    final List<IGap> gaps = align.getGaps();
    assertTrue(gaps.toString(), gaps.isEmpty());
  }

  @Test
  public void testTransposition2Matches() {
    final IWitness a = factory.createWitness("A", "d a b");
    final IWitness b = factory.createWitness("B", "a c b d");
    final IAlignment align = PairwiseAlignmentHelper.align(factory, a, b);
    final List<IMatch> matches = align.getMatches();
    assertEquals(3, matches.size());
    assertEquals("d", matches.get(0).getNormalized());
    assertEquals("a", matches.get(1).getNormalized());
    assertEquals("b", matches.get(2).getNormalized());
  }

  @Test
  public void testTransposition2Gaps() {
    final IWitness a = factory.createWitness("A", "d a b");
    final IWitness b = factory.createWitness("B", "a c b d");
    final IAlignment align = PairwiseAlignmentHelper.align(factory, a, b);
    final List<IGap> gaps = align.getGaps();
    assertEquals(1, gaps.size());
    final IGap gap = gaps.get(0);
    assertTrue(gap.isAddition());
    assertEquals("c", gap.getPhrase().getNormalized());
  }

  @Test
  public void testTransposition1() {
    final IWitness a = factory.createWitness("A", "d a b");
    final IWitness b = factory.createWitness("B", "a b d");
    final IAlignment align = PairwiseAlignmentHelper.align(factory, a, b);
    final List<ITransposition> transpositions = align.getTranspositions();
    assertEquals(2, transpositions.size());
    assertEquals("d", transpositions.get(0).getMatchA().getNormalized());
    assertEquals("a b", transpositions.get(0).getMatchB().getNormalized());
    assertEquals("d", transpositions.get(1).getMatchB().getNormalized());
    assertEquals("a b", transpositions.get(1).getMatchA().getNormalized());
  }

  @Test
  public void testTransposition2() {
    final IWitness a = factory.createWitness("A", "d a b");
    final IWitness b = factory.createWitness("B", "a c b d");
    final IAlignment align = PairwiseAlignmentHelper.align(factory, a, b);
    final List<ITransposition> transpositions = align.getTranspositions();
    assertEquals(3, transpositions.size());
    assertEquals("d", transpositions.get(0).getMatchA().getNormalized());
    assertEquals("a", transpositions.get(0).getMatchB().getNormalized());
    assertEquals("a", transpositions.get(1).getMatchA().getNormalized());
    assertEquals("b", transpositions.get(1).getMatchB().getNormalized());
    assertEquals("b", transpositions.get(2).getMatchA().getNormalized());
    assertEquals("d", transpositions.get(2).getMatchB().getNormalized());
  }

  @Test
  public void testTransposition3() {
    final IWitness a = factory.createWitness("1", "a b x c d e");
    final IWitness b = factory.createWitness("2", "c e y a d b");
    final IAlignment align = PairwiseAlignmentHelper.align(factory, a, b);
    final List<ITransposition> transpositions = align.getTranspositions();
    LOG.debug("transpositions=[" + Joiner.on(", ").join(Iterables.transform(transpositions, new Function<ITransposition, String>() {
      @Override
      public String apply(final ITransposition from) {
        return from.getMatchA().getNormalized() + "=>" + from.getMatchB().getNormalized();
      }
    })) + "]");
    assertEquals(4, transpositions.size());
    assertEquals("a", transpositions.get(0).getMatchA().getNormalized());
    assertEquals("c", transpositions.get(0).getMatchB().getNormalized());
    assertEquals("b", transpositions.get(1).getMatchA().getNormalized());
    assertEquals("e", transpositions.get(1).getMatchB().getNormalized());
    assertEquals("c", transpositions.get(2).getMatchA().getNormalized());
    assertEquals("a", transpositions.get(2).getMatchB().getNormalized());
    assertEquals("e", transpositions.get(3).getMatchA().getNormalized());
    assertEquals("b", transpositions.get(3).getMatchB().getNormalized());
  }
}
