package eu.interedition.collatex.matching;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex.collation.GapDetection;
import eu.interedition.collatex.collation.Match;
import eu.interedition.collatex.collation.NonMatch;
import eu.interedition.collatex.collation.sequences.MatchSequence;
import eu.interedition.collatex.collation.sequences.SequenceDetection;
import eu.interedition.collatex.input.Witness;

public class Collation {

  private final List<MatchSequence> sequencesBase;
  private final List<MatchSequence> sequencesWitness;
  private final Set<Match> matches;
  private final List<NonMatch> nonMatches;

  public Collation(Set<Match> _matches, Witness a, Witness b) {
    this.matches = _matches;
    this.sequencesBase = SequenceDetection.calculateMatchSequences(matches);
    this.sequencesWitness = SequenceDetection.sortSequencesForWitness(sequencesBase);
    List<NonMatch> nonMatches1 = GapDetection.getVariantsInBetweenMatchSequences(a, b, sequencesBase, sequencesWitness);
    List<NonMatch> nonMatches2 = GapDetection.getVariantsInMatchSequences(a, b, sequencesBase);
    nonMatches = Lists.newArrayList();
    nonMatches.addAll(nonMatches1);
    nonMatches.addAll(nonMatches2);
  }

  public Set<Match> getMatches() {
    return matches;
  }

  public List<MatchSequence> getMatchSequences() {
    return sequencesBase;
  }

  public List<NonMatch> getNonMatches() {
    return nonMatches;
  }

  public List<MatchSequence> getMatchSequencesForBase() {
    return getMatchSequences();
  }

  public List<MatchSequence> getMatchSequencesForWitness() {
    return sequencesWitness;
  }

  public List<NonMatch> getAdditions() {
    List<NonMatch> additions = Lists.newArrayList();
    for (NonMatch nonMatch : nonMatches) {
      if (nonMatch.isAddition()) {
        additions.add(nonMatch);
      }
    }
    return additions;
  }

  public List<NonMatch> getReplacements() {
    List<NonMatch> replacements = Lists.newArrayList();
    for (NonMatch nonMatch : getNonMatches()) {
      if (nonMatch.isReplacement()) {
        replacements.add(nonMatch);
      }
    }
    return replacements;
  }

}
