package eu.interedition.collatex.interfaces;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class GeneralAlignmentTest {
  private IFactory factory;

  @Before
  public void setup() {
    factory = new StandardFactory();
  }

  @Ignore
  @Test
  public void testAlignment() {
    final IWitness a = WitnessF.create("A", "cat");
    final IWitness b = WitnessF.create("B", "cat");
    final IAlignment alignment = factory.createAlignment(a, b);
    final List<INGram> matches = alignment.getMatchesOrderedByWitnessA();
    Assert.assertEquals(1, matches.size());
    Assert.assertEquals("cat", matches.get(0).getNormalized());
  }

}
