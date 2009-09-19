package eu.interedition.collatex.visualization;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.Matches;
import com.sd_editions.collatex.permutations.TranspositionDetection;
import com.sd_editions.collatex.permutations.Tuple2;
import com.sd_editions.collatex.permutations.collate.Transposition;

import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.collation.Collation;

public class Visualization {

  public static Modifications getModifications(Collation collation) {
    List<Transposition> transpositions = Visualization.determineTranspositions(collation.getMatchSequencesOrderedForWitnessA(), collation.getMatchSequencesOrderedForWitnessB());
    List<Modification> modificationList = Visualization.determineModifications(collation.getMatches(), collation.getGaps());
    Modifications modifications = new Modifications(modificationList, transpositions, collation.getMatches());
    return modifications;
  }

  public static List<Modification> determineModifications(Set<Match> permutation, List<Gap> determineNonMatches) {
    List<Modification> modifications = Lists.newArrayList();
    modifications.addAll(Matches.getWordDistanceMatches(permutation));
    modifications.addAll(Visualization.analyseVariants(determineNonMatches));
    return modifications;
  }

  public static List<Transposition> determineTranspositions(List<MatchSequence> matchSequencesForBase, List<MatchSequence> matchSequencesForWitness) {
    List<Tuple2<MatchSequence>> matchSequenceTuples = TranspositionDetection.calculateSequenceTuples(matchSequencesForBase, matchSequencesForWitness);
    List<Tuple2<MatchSequence>> possibleTranspositionTuples = TranspositionDetection.filterAwayRealMatches(matchSequenceTuples);
    List<Transposition> transpositions = TranspositionDetection.createTranspositions(possibleTranspositionTuples);
    return transpositions;
  }

  @Deprecated
  public static List<Modification> analyseVariants(List<Gap> variants) {
    List<Modification> results = Lists.newArrayList();
    for (Gap nonMatch : variants) {
      Modification modification = nonMatch.analyse();
      results.add(modification);
    }
    return results;
  }

}
