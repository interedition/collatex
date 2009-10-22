package com.sd_editions.collatex.permutations;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.alignment.Match;

@Deprecated
public class MatchPermutator {

  private final Set<Match> possibleMatches;
  private final Set<Set<Match>> permutations;

  public MatchPermutator(Set<Match> allPossibleMatches) {
    possibleMatches = allPossibleMatches;
    //    Util.p("possibleMatches", possibleMatches);
    List<PMatch> pmatches = Lists.newArrayList();
    for (Match match : possibleMatches) {
      pmatches.add(new PMatch(match));
    }
    permutations = Sets.newHashSet();
    permutate(pmatches);
    //    Util.p("permutations", permutations);
  }

  private void permutate(Iterable<PMatch> pmatches) {
    //    for (PMatch pmatch : pmatches) {
    //      if (!pmatch.isFixed() && Lists.newArrayList(findAlternatives(pmatches, pmatch)).size() == 1) pmatch.fix();
    //    }
    Predicate<PMatch> unFixedMatch = new Predicate<PMatch>() {
      public boolean apply(PMatch pmatch) {
        return !pmatch.isFixed();
      }
    };
    PMatch pmatch;
    try {
      pmatch = Iterables.find(pmatches, unFixedMatch);
    } catch (NoSuchElementException e) {
      pmatch = null;
    }
    if (pmatch == null) {
      // no more unfixed matches in the list? we've got a permutation!
      Set<Match> permutation = Sets.newLinkedHashSet();
      for (PMatch pm : pmatches) {
        permutation.add(pm.match);
      }
      //      Util.p("permutation", permutation);
      //      Util.p("");
      permutations.add(permutation);
    } else {
      Iterable<PMatch> alternatives = findAlternatives(pmatches, pmatch);
      //      Util.p("alternatives for " + pmatch, alternatives);
      for (PMatch alternative : alternatives) {
        //        Util.p("alternative", alternative);
        Iterable<PMatch> newPMatches = fixPMatch(pmatches, alternative);
        //        Util.p("permutate with", newPMatches);
        //        Util.p("");
        permutate(newPMatches);
      }
    }
  }

  Iterable<PMatch> fixPMatch(Iterable<PMatch> pmatches, final PMatch alternative) {
    Predicate<PMatch> fixedAndNonConflictingPMatches = new Predicate<PMatch>() {
      public boolean apply(PMatch pm) {
        return pm.isFixed() || (!pm.getBaseWord().equals(alternative.getBaseWord()) && !pm.getWitnessWord().equals(alternative.getWitnessWord()));
      }
    };
    List<PMatch> newPMatches = Lists.newArrayList();
    for (PMatch pmatch : pmatches) {
      if (pmatch.equals(alternative)) {
        PMatch copy = pmatch.copy();
        copy.fix();
        newPMatches.add(copy);
      } else if (fixedAndNonConflictingPMatches.apply(pmatch)) {
        newPMatches.add(pmatch.copy());
      }
    }
    return newPMatches;
  }

  Iterable<PMatch> findAlternatives(Iterable<PMatch> pmatches, final PMatch pmatch) {
    Predicate<PMatch> unfixedAlternativeToGivenPMatch = new Predicate<PMatch>() {
      public boolean apply(PMatch pm) {
        return !pm.isFixed() && (pm.getBaseWord().equals(pmatch.getBaseWord()) || pm.getWitnessWord().equals(pmatch.getWitnessWord()));
      }
    };
    return Iterables.filter(pmatches, unfixedAlternativeToGivenPMatch);
  }

  //  List<MatchGroup> fixCell(List<MatchGroup> _matchgroups, int index, Match match) {
  //    Set<MatchGroup> newGroups = Sets.newLinkedHashSet();
  //    int i = 0;
  //    for (MatchGroup mg : _matchgroups) {
  //      if (i == index) {
  //        newGroups.add(new MatchGroup(match));
  //      } else {
  //        MatchGroup matchgroup = new MatchGroup();
  //        for (Match m : mg) {
  //          if (m.getBaseWord().position != match.getBaseWord().position && m.getWitnessWord().position != match.getWitnessWord().position) {
  //            matchgroup.add(m);
  //          }
  //        }
  //        newGroups.add(matchgroup);
  //      }
  //      i++;
  //    }
  //    return Lists.newArrayList(Iterables.filter(newGroups, new Predicate<MatchGroup>() {
  //      public boolean apply(MatchGroup matchGroup) {
  //        return matchGroup.size() > 0;
  //      }
  //    }));
  //  }

  //  Set<Match> degroup(List<MatchGroup> matchgroups) {
  //    List<Match> degrouped = Lists.newArrayList();
  //    for (MatchGroup matches : matchgroups) {
  //      degrouped.add(matches.get(0));
  //    }
  //    return Sets.newLinkedHashSet(Lists.sortedCopy(degrouped));
  //  }

  //  @SuppressWarnings("boxing")
  //  private boolean validPermutation(List<MatchGroup> _matchgroups) {
  //    boolean onlyOneMatchPerGroup = Iterables.all(_matchgroups, new Predicate<MatchGroup>() {
  //      public boolean apply(MatchGroup mg) {
  //        return mg.size() == 1;
  //      }
  //    });
  //    List<Integer> allBasePositions = Lists.newArrayList();
  //    List<Integer> allSegmentPositions = Lists.newArrayList();
  //    for (MatchGroup mg : _matchgroups) {
  //      for (Match m : mg) {
  //        allBasePositions.add(m.getBaseWord().position);
  //        allSegmentPositions.add(m.getWitnessWord().position);
  //      }
  //    }
  //
  //    Set<Integer> uniqueBasePositions = Sets.newHashSet(allBasePositions);
  //    Set<Integer> uniqueSegmentPositions = Sets.newHashSet(allSegmentPositions);
  //    boolean baseWordIsUnique = uniqueBasePositions.size() == allBasePositions.size();
  //    boolean witnessWordIsUnique = uniqueSegmentPositions.size() == allSegmentPositions.size();
  //    boolean result = onlyOneMatchPerGroup && baseWordIsUnique && witnessWordIsUnique;
  //    //    System.out.println("incoming: " + _matchgroups);
  //    //    System.out.println("result: " + result);
  //    return result;
  //  }

  public List<Set<Match>> permutations() {
    return Lists.newArrayList(permutations);
  }

}
