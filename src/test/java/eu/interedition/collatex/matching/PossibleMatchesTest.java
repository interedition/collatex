package eu.interedition.collatex.matching;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import eu.interedition.collatex.collation.Match;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessBuilder;
import eu.interedition.collatex.input.Word;

public class PossibleMatchesTest {
  // TODO: make test work; remove ignore annotation!
  @Test
  public void testPossibleMatchesAsAMap() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("zijn hond liep aan zijn hand");
    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand");
    Matcher matcher = new Matcher();
    PossibleMatches result = matcher.match(a, b);

    Word zijn = a.getWordOnPosition(1);
    Collection<Match> linked = result.getMatchesThatLinkFrom(zijn);
    Assert.assertEquals("[(1->2), (1->5), (1->8)]", linked.toString());

    // Note: minor; why is the sequence not from lowest to highest?

    // TODO: enable!
    //    Word zijnB = b.getWordOnPosition(2);
    //    Collection<Match> links = result.getMatchesThatLinkTo(zijnB);
    //    Assert.assertEquals("[(5->2), (1->2)]", links.toString());

  }

}
