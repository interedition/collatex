package eu.interedition.collatex.input;

import junit.framework.Assert;

import org.junit.Test;

import eu.interedition.collatex.input.builders.WitnessBuilder;
import eu.interedition.collatex.input.visitors.JSONObjectVisitor;

public class JsonOutputTest {
  @Test
  public void testJsonObjectVisitor() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness w1 = builder.build("id", "a b c d");
    JSONObjectVisitor visitor = new JSONObjectVisitor();
    w1.accept(visitor);
    String expected = "[{\"ID\":\"id\",\"tokens\":[{\"token\":\"a\"},{\"token\":\"b\"},{\"token\":\"c\"},{\"token\":\"d\"}]}]";
    String result = visitor.getJsonArray().toString();
    Assert.assertEquals(expected, result);
  }
}
