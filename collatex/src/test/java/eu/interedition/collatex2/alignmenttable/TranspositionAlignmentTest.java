package eu.interedition.collatex2.alignmenttable;

import org.junit.Assert;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;

public class TranspositionAlignmentTest {
  @Test
  public void transposeInOnePair() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("A", "y");
    IWitness b = engine.createWitness("B", "x y z");
    IWitness c = engine.createWitness("C", "z y");
    Assert.assertEquals("A:  | |y| \n" + "B: x| |y|z\n" + "C:  |z|y| \n", engine.align(a, b, c).toString());
  }

  @Test
  public void transposeInTwoPairs() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("A", "y x");
    IWitness b = engine.createWitness("B", "x y z");
    IWitness c = engine.createWitness("C", "z y");
    Assert.assertEquals("A:  |y|x| \nB: x|y|z\nC: z|y| \n", engine.align(a, b, c).toString());
  }
}
