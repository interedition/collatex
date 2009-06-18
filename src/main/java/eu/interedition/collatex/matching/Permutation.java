package eu.interedition.collatex.matching;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import eu.interedition.collatex.collation.Match;
import eu.interedition.collatex.collation.NonMatch;
import eu.interedition.collatex.collation.sequences.MatchSequence;
import eu.interedition.collatex.input.Witness;

public class Permutation {
  private final Match possibleMatch;
  private final Set<Match> matches; // TODO: remove here!
  private final Collation collation;

  public Permutation(Set<Match> _fixedMatches, Match _possibleMatch) {
    this.possibleMatch = _possibleMatch;
    this.matches = Sets.newLinkedHashSet();
    this.matches.addAll(_fixedMatches);
    this.matches.add(_possibleMatch);
    this.collation = new Collation(matches);
  }

  public List<MatchSequence> getMatchSequences() {
    return getCollation().getMatchSequences();
  }

  private Collation getCollation() {
    return collation;
  }

  public List<NonMatch> getNonMatches(Witness a, Witness b) {
    return getCollation().getNonMatches(a, b);
  }

  // NOTE: rename? this is more like a last added
  // match
  // maybe the caller could just use the Permutation
  // class as a whole or the getMatches method?
  public Match getPossibleMatch() {
    return possibleMatch;
  }

  // Note: only used in tests!
  public Set<Match> getMatches() {
    return matches;
  }
}
