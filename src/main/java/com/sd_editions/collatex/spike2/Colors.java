package com.sd_editions.collatex.spike2;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sd_editions.collatex.spike2.collate.Addition;
import com.sd_editions.collatex.spike2.collate.Removal;
import com.sd_editions.collatex.spike2.collate.Replacement;
import com.sd_editions.collatex.spike2.collate.Transposition;

public class Colors {

  public final String[] witnessStrings;
  private final Index index;
  private final List<WitnessIndex> witnessIndexes;

  public Colors(String... _witnessStrings) {
    this.witnessStrings = _witnessStrings;
    index = new Index(witnessStrings);
    this.witnessIndexes = Lists.newArrayList();
    for (String witness : witnessStrings) {
      WitnessIndex witnessIndex = new WitnessIndex(witness, index);
      witnessIndexes.add(witnessIndex);
    }
  }

  public Colors(List<String> witnesses) {
    this(witnesses.toArray(new String[witnesses.size()]));
  }

  public int numberOfUniqueWords() {
    return index.numberOfEntries();
  }

  public WitnessIndex getWitnessIndex(int i) {
    return witnessIndexes.get(i - 1);
  }

  public List<Modifications> compareWitness(int i, int j) {
    List<Modifications> modificationsList = Lists.newArrayList();
    WitnessIndex witnessIndex = getWitnessIndex(i);
    WitnessIndex witnessIndex2 = getWitnessIndex(j);
    //    Matches matches = new Matches(witnessIndex, witnessIndex2);
    Matches matches = new Matches(new Witness(witnessStrings[i - 1]), new Witness(witnessStrings[j - 1]));
    List<Set<Match>> permutationList = matches.permutations();
    for (Set<Match> permutation : permutationList) {
      //Note: this only leads to one permutation of the possible matches..
      List<MatchSequence> matchSequencesForBase = TranspositionDetection.calculateMatchSequencesForgetNonMatches(permutation);
      List<MatchSequence> matchSequencesForWitness = TranspositionDetection.sortSequencesForWitness(matchSequencesForBase);
      List<Tuple2<MatchSequence>> matchSequenceTuples = TranspositionDetection.calculateSequenceTuples(matchSequencesForBase, matchSequencesForWitness);
      List<Tuple2<MatchSequence>> possibleTranspositionTuples = TranspositionDetection.filterAwayRealMatches(matchSequenceTuples);
      List<Transposition> transpositions = TranspositionDetection.calculateTranspositions(possibleTranspositionTuples);

      List<Modification> modifications = Lists.newArrayList();
      modifications.addAll(Matches.getLevenshteinMatches(permutation));
      modifications.addAll(MatchSequences.getModificationsInBetweenMatchSequences(witnessIndex, witnessIndex2, matchSequencesForBase, matchSequencesForWitness));
      modifications.addAll(MatchSequences.getModificationsInMatchSequences(witnessIndex, witnessIndex2, matchSequencesForBase));
      modificationsList.add(new Modifications(modifications, transpositions));
    }
    Comparator<Modifications> comparator = new Comparator<Modifications>() {

      public int compare(Modifications o1, Modifications o2) {
        return o1.size() - o2.size();
      }
    };
    Collections.sort(modificationsList, comparator);
    return modificationsList;
  }

  public List<Addition> getAdditions(List<MisMatch> mismatches) {
    List<MisMatch> mismatches_filter = Lists.newArrayList(Iterables.filter(mismatches, new Predicate<MisMatch>() {
      public boolean apply(MisMatch arg0) {
        return arg0.isAddition();
      }
    }));
    List<Addition> additions = Lists.newArrayList();
    for (MisMatch misMatch : mismatches_filter) {
      additions.add(misMatch.createAddition());
    }
    return additions;
  }

  public List<Removal> getOmissions(List<MisMatch> mismatches) {
    List<MisMatch> mismatches_filter = Lists.newArrayList(Iterables.filter(mismatches, new Predicate<MisMatch>() {
      public boolean apply(MisMatch arg0) {
        return arg0.isOmission();
      }
    }));
    List<Removal> omissions = Lists.newArrayList();
    for (MisMatch misMatch : mismatches_filter) {
      omissions.add(misMatch.createOmission());
    }
    return omissions;
  }

  public List<Replacement> getReplacements(List<MisMatch> mismatches) {
    List<MisMatch> mismatches_filter = Lists.newArrayList(Iterables.filter(mismatches, new Predicate<MisMatch>() {
      public boolean apply(MisMatch arg0) {
        return arg0.isReplacement();
      }
    }));
    List<Replacement> replacements = Lists.newArrayList();
    for (MisMatch misMatch : mismatches_filter) {
      replacements.add(misMatch.createReplacement());
    }
    return replacements;
  }

  public Matches getMatches(int i, int j) {
    return new Matches(getWitnessIndex(i), getWitnessIndex(j));
  }

  public int numberOfWitnesses() {
    return witnessIndexes.size();
  }

  public List<MatchSequence> getMatchSequences(int i, int j) {
    //    WitnessIndex base = getWitnessIndex(i);
    //    WitnessIndex witness = getWitnessIndex(j);
    Set<Match> matches = getMatches(i, j).matches();
    return TranspositionDetection.calculateMatchSequencesForgetNonMatches(matches);
  }
}
