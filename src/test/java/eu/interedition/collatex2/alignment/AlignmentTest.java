package eu.interedition.collatex2.alignment;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTest {
  private Factory factory;

  @Before
  public void setup() {
    factory = new Factory();
  }

  @Test
  public void testSimple1() {
    final IWitness a = factory.createWitness("A", "a b");
    final IWitness b = factory.createWitness("B", "a c b");
    final IAlignment ali = factory.createAlignment(a, b);
    final List<IMatch> matches = ali.getMatches();
    Assert.assertEquals(2, matches.size());
    Assert.assertEquals("a", matches.get(0).getNormalized());
    Assert.assertEquals("b", matches.get(1).getNormalized());
    final List<IGap> gaps = ali.getGaps();
    Assert.assertEquals(1, gaps.size());
    Assert.assertEquals("\"c\" added", gaps.get(0).toString());
  }
}
