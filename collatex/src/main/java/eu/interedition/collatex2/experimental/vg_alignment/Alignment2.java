package eu.interedition.collatex2.experimental.vg_alignment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.vg_analysis.IMatch2;
import eu.interedition.collatex2.interfaces.ITokenMatch;

public class Alignment2 implements IAlignment2 {

  private final List<IMatch2> matches;
  private final List<ITokenMatch> tokenMatches;

  public Alignment2(List<ITokenMatch> tokenMatches, List<IMatch2> matches) {
    this.tokenMatches = tokenMatches;
    this.matches = matches;
  }

  @Override
  public List<IMatch2> getMatches() {
    return matches;
  }
  
  @Override
  public List<ITokenMatch> getTokenMatches() {
    return tokenMatches;
  }
  
  @Override
  public List<ITransposition2> getTranspositions() {
    final List<IMatch2> matchesA = matches;
    final List<IMatch2> matchesB = getMatchesSortedForWitness();
    final List<ITransposition2> transpositions = Lists.newArrayList();
    for (int i = 0; i < matchesA.size(); i++) {
      final IMatch2 matchA = matchesA.get(i);
      final IMatch2 matchB = matchesB.get(i);
      if (!matchA.equals(matchB)) {
        // TODO: I have got no idea why have to mirror the matches here!
        transpositions.add(new Transposition2(matchB, matchA));
      }
    }
    return transpositions;
  }

  final Comparator<IMatch2> SORT_MATCHES_ON_POSITION_WITNESS = new Comparator<IMatch2>() {
    @Override
    public int compare(final IMatch2 o1, final IMatch2 o2) {
      return o1.getPhraseB().getBeginPosition() - o2.getPhraseB().getBeginPosition();
    }
  };

  public List<IMatch2> getMatchesSortedForWitness() {
    final List<IMatch2> matchesForWitness = Lists.newArrayList(matches);
    Collections.sort(matchesForWitness, SORT_MATCHES_ON_POSITION_WITNESS);
    return matchesForWitness;
  }

}
