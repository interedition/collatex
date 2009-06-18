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

  public Collation(Set<Match> _matches) {
    this.matches = _matches;
    this.sequencesBase = SequenceDetection.calculateMatchSequences(matches);
    this.sequencesWitness = SequenceDetection.sortSequencesForWitness(sequencesBase);
  }

  public Set<Match> getMatches() {
    return matches;
  }

  public List<MatchSequence> getMatchSequences() {
    return sequencesBase;
  }

  //TODO: move witness a, witness b up!
  public List<NonMatch> getNonMatches(Witness a, Witness b) {
    List<NonMatch> nonMatches1 = GapDetection.getVariantsInBetweenMatchSequences(a, b, sequencesBase, sequencesWitness);
    List<NonMatch> nonMatches2 = GapDetection.getVariantsInMatchSequences(a, b, sequencesBase);
    List<NonMatch> result = Lists.newArrayList();
    result.addAll(nonMatches1);
    result.addAll(nonMatches2);
    return result;
  }

}
