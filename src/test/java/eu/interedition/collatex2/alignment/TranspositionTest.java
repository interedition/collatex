package eu.interedition.collatex2.alignment;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.IWitness;

public class TranspositionTest {
  private Factory factory;

  @Before
  public void setup() {
    factory = new Factory();
  }

  @Test
  public void testTransposition1Matches() {
    final IWitness a = factory.createWitness("A", "The black dog chases a red cat.");
    final IWitness b = factory.createWitness("B", "A red cat chases the black dog.");
    final IAlignment align = factory.createAlignment(a, b);
    final List<IMatch> matches = align.getMatches();
    Assert.assertEquals(3, matches.size());
    Assert.assertEquals("the black dog", matches.get(0).getNormalized());
    Assert.assertEquals("chases", matches.get(1).getNormalized());
    Assert.assertEquals("a red cat", matches.get(2).getNormalized());
  }

  @Test
  public void testTransposition1Gaps() {
    final IWitness a = factory.createWitness("A", "The black dog chases a red cat.");
    final IWitness b = factory.createWitness("B", "A red cat chases the black dog.");
    final IAlignment align = factory.createAlignment(a, b);
    final List<IGap> gaps = align.getGaps();
    Assert.assertTrue(gaps.toString(), gaps.isEmpty());
  }

  @Ignore
  @Test
  public void testTrans2() {
    final IWitness a = factory.createWitness("A", "d a b");
    final IWitness b = factory.createWitness("B", "a c b d");
    final IAlignment align = factory.createAlignment(b, a);
    final List<IMatch> matches = align.getMatches();
    System.out.println(matches.toString());
    Assert.assertEquals(3, matches.size());
    final List<IGap> gaps = align.getGaps();
    // TODO: change to string of Gap
    Assert.assertTrue(gaps.toString(), gaps.isEmpty());

  }
}
