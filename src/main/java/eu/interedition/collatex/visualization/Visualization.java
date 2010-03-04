package eu.interedition.collatex.visualization;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.Matches;
import com.sd_editions.collatex.permutations.TranspositionDetection;
import com.sd_editions.collatex.permutations.Tuple2;
import com.sd_editions.collatex.permutations.collate.Transposition;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.experimental.ngrams.alignment.Modification;
import eu.interedition.collatex.input.BaseElement;

public class Visualization {

  public static Modifications getModifications(final Alignment collation) {
    final List<Transposition> transpositions = Visualization.determineTranspositions(collation.getMatchSequencesOrderedForWitnessA(), collation.getMatchSequencesOrderedForWitnessB());
    final List<Modification> modificationList = Visualization.determineModifications(collation.getMatches(), collation.getGaps());
    final Modifications modifications = new Modifications(modificationList, transpositions, collation.getMatches());
    return modifications;
  }

  public static <T extends BaseElement> List<Modification> determineModifications(final Set<Match<T>> permutation, final List<Gap<T>> determineNonMatches) {
    final List<Modification> modifications = Lists.newArrayList();
    modifications.addAll(Matches.getWordDistanceMatches(permutation));
    modifications.addAll(Visualization.analyseVariants(determineNonMatches));
    return modifications;
  }

  // TODO: move this? seems duplicate of Alignment.getTranspositions?
  public static <T extends BaseElement> List<Transposition> determineTranspositions(final List<MatchSequence<T>> matchSequencesForBase, final List<MatchSequence<T>> matchSequencesForWitness) {
    final List<Tuple2<MatchSequence<T>>> matchSequenceTuples = TranspositionDetection.calculateSequenceTuples(matchSequencesForBase, matchSequencesForWitness);
    final List<Tuple2<MatchSequence<T>>> possibleTranspositionTuples = TranspositionDetection.filterAwayRealMatches(matchSequenceTuples);
    final List<Transposition> transpositions = TranspositionDetection.createTranspositions(possibleTranspositionTuples);
    return transpositions;
  }

  @Deprecated
  public static <T extends BaseElement> List<Modification> analyseVariants(final List<Gap<T>> variants) {
    final List<Modification> results = Lists.newArrayList();
    for (final Gap<T> nonMatch : variants) {
      final Modification modification = nonMatch.analyse();
      results.add(modification);
    }
    return results;
  }

}
