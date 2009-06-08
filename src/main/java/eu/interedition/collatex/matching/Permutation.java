package eu.interedition.collatex.matching;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.collation.GapDetection;
import eu.interedition.collatex.collation.Match;
import eu.interedition.collatex.collation.NonMatch;
import eu.interedition.collatex.collation.sequences.MatchSequence;
import eu.interedition.collatex.collation.sequences.SequenceDetection;
import eu.interedition.collatex.input.Witness;

public class Permutation {
  private final Set<Match> fixedMatches;
  private final Match possibleMatch;
  private final List<MatchSequence> sequencesBase;
  private final List<MatchSequence> sequencesWitness;

  public Permutation(Set<Match> _fixedMatches, Match _possibleMatch) {
    this.fixedMatches = _fixedMatches;
    this.possibleMatch = _possibleMatch;
    Set<Match> matches = Sets.newLinkedHashSet();
    matches.addAll(_fixedMatches);
    matches.add(_possibleMatch);
    sequencesBase = SequenceDetection.calculateMatchSequences(matches);
    sequencesWitness = SequenceDetection.sortSequencesForWitness(sequencesBase);
  }

  public List<MatchSequence> getMatchSequences() {
    return sequencesBase;
  }

  public List<NonMatch> getNonMatches(Witness a, Witness b) {
    List<NonMatch> nonMatches1 = GapDetection.getVariantsInBetweenMatchSequences(a, b, sequencesBase, sequencesWitness);
    List<NonMatch> nonMatches2 = GapDetection.getVariantsInMatchSequences(a, b, sequencesBase);
    List<NonMatch> result = Lists.newArrayList();
    result.addAll(nonMatches1);
    result.addAll(nonMatches2);
    return result;
  }

  public Match getPossibleMatch() {
    return possibleMatch;
  }
}
