package eu.interedition.collatex.matching;

import java.util.Set;

import com.sd_editions.collatex.permutations.MatchGroup;

import eu.interedition.collatex.collation.Match;

public class Result {

  private final Set<Match> exactMatches;
  private final Set<MatchGroup> possibleMatches;

  public Result(Set<Match> _exactMatches, Set<MatchGroup> _possibleMatches) {
    this.exactMatches = _exactMatches;
    possibleMatches = _possibleMatches;
  }

  public Set<Match> getExactMatches() {
    return exactMatches;
  }

  public Set<MatchGroup> getPossibleMatches() {
    return possibleMatches;
  }

}
