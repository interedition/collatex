package eu.interedition.collatex.input;

import junit.framework.Assert;

import org.junit.Test;

import eu.interedition.collatex.input.visitors.JSonVisitor;

public class JsonOutputTest {
  @Test
  public void testJason() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness w1 = builder.build("id", "a b c d");
    JSonVisitor visitor = new JSonVisitor();
    w1.accept(visitor);
    String expected = "{ words: [{ content:a}, { content:b}, { content:c}, { content:d}] }";
    String result = visitor.getResult();
    Assert.assertEquals(expected, result);
  }
}
