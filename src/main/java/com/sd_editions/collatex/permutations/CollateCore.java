package com.sd_editions.collatex.permutations;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sd_editions.collatex.match.worddistance.Levenshtein;
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
    Matches matches = new Matches(base, witness, new Levenshtein());
    List<Set<Match>> permutationList = matches.permutations();
    for (Set<Match> permutation : permutationList) {
      List<MatchSequence> matchSequencesForBase = SequenceDetection.calculateMatchSequences(permutation);
      List<MatchSequence> matchSequencesForWitness = SequenceDetection.sortSequencesForWitness(matchSequencesForBase);
      List<MisMatch> unmatches = determineUnmatches(base, witness, matchSequencesForBase, matchSequencesForWitness);

      List<Transposition> transpositions = determineTranspositions(matchSequencesForBase, matchSequencesForWitness);
      List<Modification> modifications = determineModifications(permutation, unmatches);
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

  private List<Modification> determineModifications(Set<Match> permutation, List<MisMatch> determineUnmatches) {
    List<Modification> modifications = Lists.newArrayList();
    modifications.addAll(Matches.getWordDistanceMatches(permutation));
    modifications.addAll(MatchSequences.analyseVariants(determineUnmatches));
    return modifications;
  }

  private List<MisMatch> determineUnmatches(Witness base, Witness witness, List<MatchSequence> matchSequencesForBase, List<MatchSequence> matchSequencesForWitness) {
    List<MisMatch> variants2 = Lists.newArrayList();
    variants2.addAll(MatchSequences.getVariantsInBetweenMatchSequences(base, witness, matchSequencesForBase, matchSequencesForWitness));
    variants2.addAll(MatchSequences.getVariantsInMatchSequences(base, witness, matchSequencesForBase));
    return variants2;
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
    return new Matches(getWitness(i), getWitness(j), new Levenshtein());
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
    Matches xmatches = new Matches(base, witness, new Levenshtein());
    List<Set<Match>> permutationList = xmatches.permutations();
    Set<Match> matches = permutationList.get(0);
    return SequenceDetection.calculateMatchSequences(matches);
  }

  public MultiMatchMap getMultiMatchMap() {
    // initialize with the first 2 witnesses
    Witness base = getWitness(1);
    Witness witness = getWitness(2);
    MultiMatchMap multiMatchesPerNormalizedWord = new MultiMatchMap();
    for (Word baseword : base.getWords()) {
      String normalized = baseword.normalized;
      if (multiMatchesPerNormalizedWord.containsKey(normalized)) {
        multiMatchesPerNormalizedWord.get(normalized).addMatchingWord(baseword);
      }
      for (Word witnessword : witness.getWords()) {
        if (normalized.equals(witnessword.normalized)) {
          MultiMatch mm;
          if (multiMatchesPerNormalizedWord.containsKey(normalized)) {
            mm = multiMatchesPerNormalizedWord.get(normalized);
            mm.addMatchingWord(witnessword);
          } else {
            mm = new MultiMatch(baseword, witnessword);
          }
          multiMatchesPerNormalizedWord.put(normalized, mm);
        }
      }
    }
    // go over the rest of the witnesses, comparing the normalizedwords from the multimatches
    for (int i = 3; i <= witnesses.size(); i++) {
      witness = getWitness(i);
      for (String normalized : multiMatchesPerNormalizedWord.keySet()) {
        boolean normalizedHasMatchInThisWitness = false;
        for (Word witnessword : witness.getWords()) {
          if (normalized.equals(witnessword.normalized)) {
            MultiMatch mm = multiMatchesPerNormalizedWord.get(normalized);
            mm.addMatchingWord(witnessword);
            multiMatchesPerNormalizedWord.put(normalized, mm);
            normalizedHasMatchInThisWitness = true;
          }
        }
        if (!normalizedHasMatchInThisWitness) {
          multiMatchesPerNormalizedWord.remove(normalized);
        }
      }
    }
    return multiMatchesPerNormalizedWord;
  }
}
