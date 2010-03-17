package eu.interedition.collatex2.implementation.alignment;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.BaseElement;
import eu.interedition.collatex2.implementation.modifications.Transposition;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.ITransposition;

public class TranspositionDetection {

  public static <T extends BaseElement> List<ITransposition> getTranspositions(final Alignment alignment) {
    final List<IMatch> matchesA = alignment.getMatches();
    final List<IMatch> matchesB = alignment.getMatchesSortedForB();
    final List<ITransposition> transpositions = Lists.newArrayList();
    for (int i = 0; i < matchesA.size(); i++) {
      final IMatch matchA = matchesA.get(i);
      final IMatch matchB = matchesB.get(i);
      if (!matchA.equals(matchB)) {
        transpositions.add(new Transposition(matchA, matchB));
      }
    }
    return transpositions;
  }

}
