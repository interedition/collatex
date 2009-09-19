package eu.interedition.collatex.matching;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.functions.Matcher;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class PossibleMatchesTest {
  @Test
  public void testPossibleMatchesAsAMap() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("zijn hond liep aan zijn hand");
    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand");
    Alignment result = Matcher.align(a, b);
    Alignment firstAlignment = result.getPrevious().getPrevious().getPrevious().getPrevious();
    Word zijn = a.getWordOnPosition(1);
    Collection<Match> linked = firstAlignment.getMatchesThatLinkFrom(zijn);
    Assert.assertEquals("[(1->2), (1->5), (1->8)]", linked.toString());

    Word zijnB = b.getWordOnPosition(2);
    Collection<Match> links = firstAlignment.getMatchesThatLinkTo(zijnB);
    Assert.assertEquals("[(1->2), (5->2)]", links.toString());
  }
}
