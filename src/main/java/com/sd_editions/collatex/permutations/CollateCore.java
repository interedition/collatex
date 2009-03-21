package com.sd_editions.collatex.permutations;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.collate.Addition;
import com.sd_editions.collatex.permutations.collate.Omission;
import com.sd_editions.collatex.permutations.collate.Replacement;
import com.sd_editions.collatex.permutations.collate.Transposition;

public class CollateCore {

  public final List<Witness> witnesses;

  public CollateCore(String... _witnessStrings) {
    this.witnesses = Lists.newArrayList();
    for (String witnessString : _witnessStrings) {
      this.witnesses.add(new Witness(witnessString));
    }
  }

  public CollateCore(List<String> _witnessStrings) {
    this(_witnessStrings.toArray(new String[_witnessStrings.size()]));
  }

  public List<Modifications> compareWitness(int i, int j) {
    List<Modifications> modificationsList = Lists.newArrayList();
    Witness base = getWitness(i);
    Witness witness = getWitness(j);
    Matches matches = new Matches(base, witness);
    List<Set<Match>> permutationList = matches.permutations();
    for (Set<Match> permutation : permutationList) {
      List<MatchSequence> matchSequencesForBase = TranspositionDetection.calculateMatchSequencesForgetNonMatches(permutation);
      List<MatchSequence> matchSequencesForWitness = TranspositionDetection.sortSequencesForWitness(matchSequencesForBase);
      List<Transposition> transpositions = determineTranspositions(matchSequencesForBase, matchSequencesForWitness);
      List<Modification> modifications = determineModifications(base, witness, permutation, matchSequencesForBase, matchSequencesForWitness);
      modificationsList.add(new Modifications(modifications, transpositions, permutation));
    }
    sortPermutationsByRelevance(modificationsList);
    return modificationsList;
  }

  private void sortPermutationsByRelevance(List<Modifications> modificationsList) {
    Comparator<Modifications> comparator = new Comparator<Modifications>() {
      public int compare(Modifications o1, Modifications o2) {
        return o1.size() - o2.size();
      }
    };
    Collections.sort(modificationsList, comparator);
  }

  private List<Modification> determineModifications(Witness base, Witness witness, Set<Match> permutation, List<MatchSequence> matchSequencesForBase, List<MatchSequence> matchSequencesForWitness) {
    List<Modification> modifications = Lists.newArrayList();
    modifications.addAll(Matches.getLevenshteinMatches(permutation));
    modifications.addAll(MatchSequences.getModificationsInBetweenMatchSequences(base, witness, matchSequencesForBase, matchSequencesForWitness));
    modifications.addAll(MatchSequences.getModificationsInMatchSequences(base, witness, matchSequencesForBase));
    return modifications;
  }

  private List<Transposition> determineTranspositions(List<MatchSequence> matchSequencesForBase, List<MatchSequence> matchSequencesForWitness) {
    List<Tuple2<MatchSequence>> matchSequenceTuples = TranspositionDetection.calculateSequenceTuples(matchSequencesForBase, matchSequencesForWitness);
    List<Tuple2<MatchSequence>> possibleTranspositionTuples = TranspositionDetection.filterAwayRealMatches(matchSequenceTuples);
    List<Transposition> transpositions = TranspositionDetection.calculateTranspositions(possibleTranspositionTuples);
    return transpositions;
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

  public List<Omission> getOmissions(List<MisMatch> mismatches) {
    List<MisMatch> mismatches_filter = Lists.newArrayList(Iterables.filter(mismatches, new Predicate<MisMatch>() {
      public boolean apply(MisMatch arg0) {
        return arg0.isOmission();
      }
    }));
    List<Omission> omissions = Lists.newArrayList();
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
    return new Matches(getWitness(i), getWitness(j));
  }

  public Witness getWitness(int i) {
    return witnesses.get(i - 1);
  }

  public int numberOfWitnesses() {
    return witnesses.size();
  }

  public List<MatchSequence> getMatchSequences(int i, int j) {
    Witness base = getWitness(i);
    Witness witness = getWitness(j);
    Matches xmatches = new Matches(base, witness);
    List<Set<Match>> permutationList = xmatches.permutations();
    Set<Match> matches = permutationList.get(0);
    return TranspositionDetection.calculateMatchSequencesForgetNonMatches(matches);
  }
}
