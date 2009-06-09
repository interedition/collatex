package eu.interedition.collatex.matching;

import java.util.Map;
import java.util.Set;

import com.sd_editions.collatex.permutations.MatchGroup;

import eu.interedition.collatex.collation.Match;

public class Result {

  private final Set<Match> exactMatches;
  private final Set<MatchGroup> possibleMatches; // TODO: remove!
  private final Map<Integer, MatchGroup> matchGroupsForBase;

  public Result(Set<Match> _exactMatches, Set<MatchGroup> _possibleMatches, Map<Integer, MatchGroup> _matchGroupsForBase) {
    this.exactMatches = _exactMatches;
    this.possibleMatches = _possibleMatches;
    this.matchGroupsForBase = _matchGroupsForBase;
  }

  public Set<Match> getExactMatches() {
    return exactMatches;
  }

  public Set<MatchGroup> getPossibleMatches() {
    return possibleMatches;
  }

  // Note: maybe it should be Word instead of int?
  public MatchGroup getMatchGroupForBaseWord(int i) {
    return matchGroupsForBase.get(new Integer(i));
  }

  public MatchGroup getMatchGroupForWitnessWord(int i) {
    // TODO Auto-generated method stub
    return null;
  }
}
