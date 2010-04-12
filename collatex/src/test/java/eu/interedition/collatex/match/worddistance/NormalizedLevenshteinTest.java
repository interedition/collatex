package eu.interedition.collatex.match.worddistance;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sd_editions.collatex.Block.Util;

public class NormalizedLevenshteinTest {

  private static final float THRESHOLD = 0.5f;

  @Test
  public void testThisThoseDistance() {
    NormalizedLevenshtein normalizedLevenshtein = new NormalizedLevenshtein();
    float thisThoseDistance = normalizedLevenshtein.distance("this", "those");
    Util.p("this" + " ~ " + "those" + ":" + thisThoseDistance);
    assertTrue("this ~ those > threshold: " + thisThoseDistance, thisThoseDistance < THRESHOLD);
  }

  @Test
  public void testSomethingElseDistance() {
    NormalizedLevenshtein normalizedLevenshtein = new NormalizedLevenshtein();
    float somethingElseDistance = normalizedLevenshtein.distance("something", "else");
    Util.p("something" + " ~ " + "else" + ":" + somethingElseDistance);
    assertTrue("something ~ else < threshold: " + somethingElseDistance, somethingElseDistance > THRESHOLD);
  }

}
