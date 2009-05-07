package com.sd_editions.collatex.permutations;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class MultiMatchPermutator {

  private final LinkedHashMap<String, MultiMatch> possibleMultiMatches;
  private final Set<Set<MultiMatch>> permutations;

  public MultiMatchPermutator(LinkedHashMap<String, MultiMatch> allPossibleMultiMatches) {
    possibleMultiMatches = allPossibleMultiMatches;
    List<PMMatch> pmmatches = Lists.newArrayList();
    for (MultiMatch multimatch : possibleMultiMatches.values()) {
      pmmatches.add(new PMMatch(multimatch));
    }
    permutations = Sets.newHashSet();
    permutate(pmmatches);
  }

  private void permutate(Iterable<PMMatch> pmmatches) {
    Predicate<PMMatch> unFixedMatch = new Predicate<PMMatch>() {
      public boolean apply(PMMatch pmatch) {
        return !pmatch.isFixed();
      }
    };
    PMMatch pmmatch;
    try {
      pmmatch = Iterables.find(pmmatches, unFixedMatch);
    } catch (NoSuchElementException e) {
      pmmatch = null;
    }
    if (pmmatch == null) {
      // no more unfixed matches in the list? we've got a permutation!
      Set<MultiMatch> permutation = Sets.newLinkedHashSet();
      for (PMMatch pm : pmmatches) {
        permutation.add(pm.match);
      }
      //      Util.p("permutation", permutation);
      //      Util.p("");
      permutations.add(permutation);
    } else {
      Iterable<PMMatch> alternatives = findAlternatives(pmmatches, pmmatch);
      //      Util.p("alternatives for " + pmatch, alternatives);
      for (PMMatch alternative : alternatives) {
        //        Util.p("alternative", alternative);
        Iterable<PMMatch> newPMMatches = fixPMMatch(pmmatches, alternative);
        //        Util.p("permutate with", newPMatches);
        //        Util.p("");
        permutate(newPMMatches);
      }
    }
  }

  Iterable<PMMatch> fixPMMatch(Iterable<PMMatch> pMMatches, final PMMatch alternative) {
    Predicate<PMMatch> fixedAndNonConflictingPMMatches = new Predicate<PMMatch>() {
      public boolean apply(PMMatch pm) {
        boolean b = true;
        List<Word> pmWords = pm.getWords();
        List<Word> alternativeWords = alternative.getWords();
        for (int i = 0; i < pm.getWords().size(); i++) {
          b = b && !pmWords.get(i).equals(alternativeWords.get(i));
        }
        return pm.isFixed() || b;
      }
    };
    List<PMMatch> newPMMatches = Lists.newArrayList();
    for (PMMatch pMMatch : pMMatches) {
      if (pMMatch.equals(alternative)) {
        PMMatch copy = pMMatch.copy();
        copy.fix();
        newPMMatches.add(copy);
      } else if (fixedAndNonConflictingPMMatches.apply(pMMatch)) {
        newPMMatches.add(pMMatch.copy());
      }
    }
    return newPMMatches;
  }

  Iterable<PMMatch> findAlternatives(Iterable<PMMatch> pMMatches, final PMMatch pMMatch) {
    Predicate<PMMatch> unfixedAlternativeToGivenPMMatch = new Predicate<PMMatch>() {
      public boolean apply(PMMatch pm) {
        List<Word> pmWords = pm.getWords();
        List<Word> alternativeWords = pMMatch.getWords();
        boolean b = false;
        for (int i = 0; i < pm.getWords().size(); i++) {
          b = b || pmWords.get(i).equals(alternativeWords.get(i));
        }
        return !pm.isFixed() && b;
      }
    };
    return Iterables.filter(pMMatches, unfixedAlternativeToGivenPMMatch);
  }

  public List<Set<MultiMatch>> permutations() {
    return Lists.newArrayList(permutations);
  }

}
