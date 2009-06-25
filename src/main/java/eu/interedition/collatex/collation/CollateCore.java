package eu.interedition.collatex.collation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.Matches;
import com.sd_editions.collatex.permutations.collate.Addition;
import com.sd_editions.collatex.permutations.collate.Omission;
import com.sd_editions.collatex.permutations.collate.Replacement;

import eu.interedition.collatex.collation.alignment.Match;
import eu.interedition.collatex.collation.alignment.Matcher;
import eu.interedition.collatex.collation.gaps.Gap;
import eu.interedition.collatex.collation.gaps.GapDetection;
import eu.interedition.collatex.collation.sequences.MatchSequence;
import eu.interedition.collatex.collation.sequences.SequenceDetection;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.match.worddistance.NormalizedLevenshtein;
import eu.interedition.collatex.visualization.Modifications;
import eu.interedition.collatex.visualization.Visualization;

public class CollateCore {

  public final List<Witness> witnesses;

  public CollateCore(Witness... _witnesses) {
    this(Lists.newArrayList(_witnesses));
  }

  public CollateCore(List<Witness> _witnesses) {
    this.witnesses = _witnesses;
  }

  @Deprecated
  public Modifications compareWitness(int i, int j) {
    Witness base = getWitness(i);
    Witness witness = getWitness(j);
    Collation collation = compareWitnesses(base, witness);
    Modifications modifications = Visualization.getModifications(collation);
    return modifications;
    //    List<Modifications> modificationsList = Lists.newArrayList();
    //    List<Collation> matchNonMatchList = doCompareWitnesses(base, witness);
    //
    //    for (Collation matchNonMatch : matchNonMatchList) {
    //      modificationsList.add(Visualization.getModifications(matchNonMatch));
    //    }
    //    sortPermutationsByRelevance(modificationsList);
    //    return modificationsList;
  }

  public Collation doCompareWitnesses(Witness base, Witness witness) {
    Matcher matcher = new Matcher();
    Collation collation = matcher.collate(base, witness);
    return collation;
    //    Matches matches = new Matches(base, witness, new NormalizedLevenshtein());
    //    List<Set<Match>> permutationList = matches.permutations();
    //
    //    List<MatchNonMatch> matchNonMatchList = Lists.newArrayList();
    //    for (Set<Match> permutation : permutationList) {
    //      List<MatchSequence> matchSequencesByBase = SequenceDetection.calculateMatchSequences(permutation);
    //      List<MatchSequence> matchSequencesByWitness = SequenceDetection.sortSequencesForWitness(matchSequencesByBase);
    //      List<NonMatch> nonMatches = determineNonMatches(base, witness, matchSequencesByBase, SequenceDetection.sortSequencesForWitness(matchSequencesByBase));
    //      matchNonMatchList.add(new MatchNonMatch(permutation, matchSequencesByBase, matchSequencesByWitness, nonMatches));
    //    }
    //    sortPermutationsByVariation(matchNonMatchList);
    // return matchNonMatchList;
  }

  public Collation compareWitnesses(Witness w1, Witness w2) {
    return doCompareWitnesses(w1, w2);
    //    List<MatchNonMatch> matchNonMatchList = doCompareWitnesses(w1, w2);
    //    sortPermutationsByVariation(matchNonMatchList);
    //    return matchNonMatchList.get(0);
  }

  public List<List<MatchNonMatch>> getAllMatchNonMatchPermutations() {
    throw new UnsupportedOperationException();
    //    List<List<MatchNonMatch>> matchNonMatchPermutationsForAllWitnessPairs = Lists.newArrayList();
    //    final int numberOfWitnesses = numberOfWitnesses();
    //    for (int w1 = 0; w1 < numberOfWitnesses - 1; w1++) {
    //      for (int w2 = w1 + 1; w2 < numberOfWitnesses; w2++) {
    //        matchNonMatchPermutationsForAllWitnessPairs.add(doCompareWitnesses(witnesses.get(w1), witnesses.get(w2)));
    //      }
    //    }
    //    return matchNonMatchPermutationsForAllWitnessPairs;
  }

  /*
   * Temporary heuristics for the best collation without relying on the analysis stage.
   * Looking for a new home ...
   */
  public void sortPermutationsByNonMatches(List<MatchNonMatch> matchNonMatchList) {
    Comparator<MatchNonMatch> comparator = new Comparator<MatchNonMatch>() {
      public int compare(MatchNonMatch o1, MatchNonMatch o2) {
        return o1.getNonMatches().size() - o2.getNonMatches().size();
      }
    };
    Collections.sort(matchNonMatchList, comparator);
  }

  public void sortPermutationsByVariation(List<MatchNonMatch> matchNonMatchList) {
    Comparator<MatchNonMatch> comparator = new Comparator<MatchNonMatch>() {
      public int compare(MatchNonMatch o1, MatchNonMatch o2) {
        return Double.compare(o1.getVariationMeasure(), o2.getVariationMeasure());
      }
    };
    Collections.sort(matchNonMatchList, comparator);
  }

  private void sortPermutationsByRelevance(List<Modifications> modificationsList) {
    Comparator<Modifications> comparator = new Comparator<Modifications>() {
      public int compare(Modifications o1, Modifications o2) {
        return o1.size() - o2.size();
      }
    };
    Collections.sort(modificationsList, comparator);
  }

  private List<Gap> determineNonMatches(Witness base, Witness witness, List<MatchSequence> matchSequencesForBase, List<MatchSequence> matchSequencesForWitness) {
    List<Gap> variants2 = Lists.newArrayList();
    variants2.addAll(GapDetection.getVariantsInBetweenMatchSequences(base, witness, matchSequencesForBase, matchSequencesForWitness));
    variants2.addAll(GapDetection.getVariantsInMatchSequences(base, witness, matchSequencesForBase));
    return variants2;
  }

  public List<Addition> getAdditions(List<Gap> nonMatches) {
    List<Gap> nonMatches_filter = Lists.newArrayList(Iterables.filter(nonMatches, new Predicate<Gap>() {
      public boolean apply(Gap arg0) {
        return arg0.isAddition();
      }
    }));
    List<Addition> additions = Lists.newArrayList();
    for (Gap nonMatch : nonMatches_filter) {
      additions.add(nonMatch.createAddition());
    }
    return additions;
  }

  public List<Omission> getOmissions(List<Gap> nonMatches) {
    List<Gap> nonMatches_filter = Lists.newArrayList(Iterables.filter(nonMatches, new Predicate<Gap>() {
      public boolean apply(Gap arg0) {
        return arg0.isOmission();
      }
    }));
    List<Omission> omissions = Lists.newArrayList();
    for (Gap nonMatch : nonMatches_filter) {
      omissions.add(nonMatch.createOmission());
    }
    return omissions;
  }

  public List<Replacement> getReplacements(List<Gap> nonMatches) {
    List<Gap> nonMatches_filter = Lists.newArrayList(Iterables.filter(nonMatches, new Predicate<Gap>() {
      public boolean apply(Gap arg0) {
        return arg0.isReplacement();
      }
    }));
    List<Replacement> replacements = Lists.newArrayList();
    for (Gap nonMatch : nonMatches_filter) {
      replacements.add(nonMatch.createReplacement());
    }
    return replacements;
  }

  public Matches getMatches(int i, int j) {
    return new Matches(getWitness(i), getWitness(j), new NormalizedLevenshtein());
  }

  public Witness getWitness(int i) {
    return witnesses.get(i - 1);
  }

  public int numberOfWitnesses() {
    return witnesses.size();
  }

  @Deprecated
  public List<MatchSequence> getMatchSequences(int i, int j) {
    Witness base = getWitness(i);
    Witness witness = getWitness(j);
    Matches xmatches = new Matches(base, witness, new NormalizedLevenshtein());
    List<Set<Match>> permutationList = xmatches.permutations();
    Set<Match> matches = permutationList.get(0);
    return SequenceDetection.calculateMatchSequences(matches);
  }

}
