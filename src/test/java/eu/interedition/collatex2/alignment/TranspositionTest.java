package eu.interedition.collatex2.alignment;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.implementation.alignment.TranspositionDetection;
import eu.interedition.collatex2.implementation.modifications.Transposition;
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
    assertEquals(3, matches.size());
    assertEquals("the black dog", matches.get(0).getNormalized());
    assertEquals("chases", matches.get(1).getNormalized());
    assertEquals("a red cat", matches.get(2).getNormalized());
  }

  @Test
  public void testTransposition1Gaps() {
    final IWitness a = factory.createWitness("A", "The black dog chases a red cat.");
    final IWitness b = factory.createWitness("B", "A red cat chases the black dog.");
    final IAlignment align = factory.createAlignment(a, b);
    final List<IGap> gaps = align.getGaps();
    assertTrue(gaps.toString(), gaps.isEmpty());
  }

  @Test
  public void testTransposition2() {
    final IWitness a = factory.createWitness("A", "d a b");
    final IWitness b = factory.createWitness("B", "a c b d");
    final IAlignment align = factory.createAlignment(b, a);
    final List<IMatch> matches = align.getMatches();
    System.out.println(matches.toString());
    assertEquals(3, matches.size());
    final List<IGap> gaps = align.getGaps();
    assertEquals(1, gaps.size());
    final TranspositionDetection td = new TranspositionDetection(align);
    final List<Transposition> transpositions = td.getTranspositions();
    assertEquals(1, transpositions.size());
    assertEquals("d", transpositions.get(0).getNormalized());
    // TODO: change to string of Gap
    //    assertTrue(gaps.toString(), gaps.isEmpty());
  }
}
