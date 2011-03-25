package eu.interedition.collatex2.experimental;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;

public class DeTestDirkVincent {

  @Test
  public void testDirkVincent() {
    CollateXEngine factory = new CollateXEngine();
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    MyNewMatcher matcher = new MyNewMatcher();
    List<ITokenMatch> matches = matcher.match(a, b);
    ITokenMatch m = matches.get(0);
    assertEquals("Its", m.getBaseToken().getContent());
    assertEquals("Its", m.getWitnessToken().getContent());
  }
}
