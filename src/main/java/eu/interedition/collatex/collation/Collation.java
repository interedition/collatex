package eu.interedition.collatex.collation;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex.collation.alignment.Match;
import eu.interedition.collatex.collation.gaps.Gap;
import eu.interedition.collatex.collation.gaps.GapDetection;
import eu.interedition.collatex.collation.sequences.MatchSequence;
import eu.interedition.collatex.collation.sequences.SequenceDetection;
import eu.interedition.collatex.input.Witness;

public class Collation {

  private final List<MatchSequence> sequencesBase;
  private final List<MatchSequence> sequencesWitness;
  private final Set<Match> matches;
  private final List<Gap> nonMatches;

  // Note: this constructor should take only an Alignment object as parameter!
  public Collation(Set<Match> _matches, Witness a, Witness b) {
    this.matches = _matches;
    this.sequencesBase = SequenceDetection.calculateMatchSequences(matches);
    this.sequencesWitness = SequenceDetection.sortSequencesForWitness(sequencesBase);
    List<Gap> nonMatches1 = GapDetection.getVariantsInBetweenMatchSequences(a, b, sequencesBase, sequencesWitness);
    List<Gap> nonMatches2 = GapDetection.getVariantsInMatchSequences(a, b, sequencesBase);
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

  public List<Gap> getNonMatches() {
    return nonMatches;
  }

  public List<MatchSequence> getMatchSequencesForBase() {
    return getMatchSequences();
  }

  public List<MatchSequence> getMatchSequencesForWitness() {
    return sequencesWitness;
  }

  public List<Gap> getAdditions() {
    List<Gap> additions = Lists.newArrayList();
    for (Gap nonMatch : nonMatches) {
      if (nonMatch.isAddition()) {
        additions.add(nonMatch);
      }
    }
    return additions;
  }

  public List<Gap> getReplacements() {
    List<Gap> replacements = Lists.newArrayList();
    for (Gap nonMatch : getNonMatches()) {
      if (nonMatch.isReplacement()) {
        replacements.add(nonMatch);
      }
    }
    return replacements;
  }

}
