package eu.interedition.collatex.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class Poging2Test {
  private WitnessBuilder builder;

  @Before
  public void setup() {
    builder = new WitnessBuilder();
  }

  @Test
  public void testMatchingWordPositionsPerWitness() {
    Witness a = builder.build("a", "zijn hond liep aan zijn hand");
    Witness b = builder.build("b", "op zijn pad liep zijn hond aan zijn hand");
    Witness c = builder.build("c", "met zijn hond aan zijn hand liep hij op zijn pad");
    Poging2 p2 = new Poging2(a, b, c);

    Map<String, List<Integer>> zijnPositions = p2.matchingWordPositionsPerWitness("zijn");
    // all 3 witnesses have at least 1 'zijn':
    assertEquals(3, zijnPositions.size());

    assertContainsPositions(zijnPositions.get("a"), 1, 5);
    assertContainsPositions(zijnPositions.get("b"), 2, 5, 8);
    assertContainsPositions(zijnPositions.get("c"), 2, 5, 10);
  }

  private void assertContainsPositions(List<Integer> positionsA, int... positions) {
    for (int position : positions) {
      assertTrue("position " + position + " not found", positionsA.contains(Integer.valueOf(position)));
    }
  }

  @Test
  public void testGetOneWordSequences() {
    Witness a = builder.build("a", "zijn hond liep aan zijn hand");
    Witness b = builder.build("b", "op zijn pad liep zijn hond aan zijn hand");
    Witness c = builder.build("c", "met zijn hond aan zijn hand liep hij op zijn pad");
    Poging2 p2 = new Poging2(a, b, c);

    Map<String, Map<String, List<Integer>>> oneWordSequences = p2.getOneWordSequences();
    Map<String, List<Integer>> hondSequences = oneWordSequences.get("hond");
    assertContainsPositions(hondSequences.get("a"), 2);
    // nr. of unique normalized words in all witnesses combined
    assertEquals(9, oneWordSequences.size());

    //    ArrayList<String> newArrayList = Lists.newArrayList(oneWordSequences.keySet());
    //    Collections.sort(newArrayList);
    //    Util.p(newArrayList);
  }
}
