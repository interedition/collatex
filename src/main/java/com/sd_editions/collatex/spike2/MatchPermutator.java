package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class MatchPermutator {

  private final Set<Match> possibleMatches;
  private final List<Match[]> groupedMatches;
  private final List<Set<Match>> permutations;

  public MatchPermutator(Set<Match> allPossibleMatches) {
    possibleMatches = allPossibleMatches;
    groupedMatches = group(possibleMatches);
    permutations = Lists.newArrayList();
    permutate(groupedMatches, 0, permutations);
  }

  @SuppressWarnings("boxing")
  protected List<Match[]> group(Set<Match> matches) {
    List<Match[]> groups = Lists.newArrayList();
    List<Integer> t0 = Lists.newArrayList();
    List<Integer> t1 = Lists.newArrayList();
    for (Match match : matches) {
      Iterable<Match> group0 = Lists.newArrayList();
      Iterable<Match> group1 = Lists.newArrayList();
      final int basePosition = match.getBaseWord().position;
      final int witnessPosition = match.getWitnessWord().position;
      if (!t0.contains(basePosition)) {
        Predicate<Match> basePositionPredicate = new Predicate<Match>() {
          public boolean apply(Match _match) {
            return (_match.getBaseWord().position == basePosition);
          }
        };
        group0 = Iterables.filter(matches, basePositionPredicate);
        t0.add(basePosition);
      }
      if (!t1.contains(witnessPosition)) {
        Predicate<Match> witnessPositionPredicate = new Predicate<Match>() {
          public boolean apply(Match _match) {
            return (_match.getWitnessWord().position == witnessPosition);
          }
        };
        group1 = Iterables.filter(matches, witnessPositionPredicate);
        t1.add(witnessPosition);
      }
      Set<Match> set0 = Sets.newLinkedHashSet(group0);
      Set<Match> set1 = Sets.newLinkedHashSet(group1);
      Match[] matchGroup0 = Iterables.newArray(group0, Match.class);
      Match[] matchGroup1 = Iterables.newArray(group1, Match.class);
      if (set0.containsAll(set1) && matchGroup0.length > 0) {
        groups.add(matchGroup0);
      } else if (set1.containsAll(set0) && matchGroup1.length > 0) {
        groups.add(matchGroup1);
      } else {
        if (matchGroup0.length > 0) groups.add(matchGroup0);
        if (matchGroup1.length > 0) groups.add(matchGroup1);
      }
    }
    return groups;
  }

  private void permutate(List<Match[]> _groupedMatches, int startGroup, List<Set<Match>> _permutations) {
    //    if (valid_permutation?(matchgroups))
    if (validPermutation(_groupedMatches)) {
      //      permutations << degroup(matchgroups)
      _permutations.add(degroup(_groupedMatches));
      //    else
    } else {
      //      i = start_group
      int i = startGroup;
      //      while (!matchgroups[i].nil? && matchgroups[i].size==1 && i<matchgroups.size-1)
      while (i < _groupedMatches.size() - 1 && _groupedMatches.get(i) != null && _groupedMatches.get(i).length == 1) {
        //        i+=1
        i++;
        //      end
      }
      //      if (i<matchgroups.size)
      if (i < _groupedMatches.size()) {
        //        matchgroups[i].each do |match|
        for (Match match : _groupedMatches.get(i)) {
          //          new_matchgroups = fix_cell(matchgroups, i, match)
          List<Match[]> newMatchGroups = fixCell(_groupedMatches, i, match);
          //          permutate(new_matchgroups, i+1, permutations)
          permutate(newMatchGroups, i + 1, _permutations);
          //        end
        }
        //      end
      }
      //    end
    }
  }

  //  def fix_cell(_matchgroups, index, match)
  List<Match[]> fixCell(List<Match[]> _matchgroups, int index, Match match) {
    //    new_groups = []
    List<Match[]> newGroups = Lists.newArrayList();

    //    _matchgroups.each_with_index do |mg,i|
    int i = 0;
    for (Match[] mg : _matchgroups) {
      //      if (i == index)
      if (i == index) {
        //        new_groups << [match]
        newGroups.add(new Match[] { match });
        //      else
      } else {
        //        new_groups << mg.select{ |m| m.word1 != match.word1 && m.word2 != match.word2 }
        List<Match> matchgroup = Lists.newArrayList();
        for (Match m : mg) {
          if (m.getBaseWord().position != match.getBaseWord().position && m.getWitnessWord().position != match.getWitnessWord().position) {
            matchgroup.add(m);
          }
        }
        newGroups.add(matchgroup.toArray(new Match[] {}));
        //      end
      }
      i++;
      //    end
    }
    //    return new_groups.delete_if{ |g| g.empty? }
    return Lists.newArrayList(Iterables.filter(newGroups, new Predicate<Match[]>() {
      public boolean apply(Match[] matchArray) {
        return matchArray.length > 0;
      }
    }));
    //  end
  }

  //  def degroup(matchgroups)
  Set<Match> degroup(List<Match[]> matchgroups) {
    //    matchgroups.collect{ |mg| mg[0] }
    Set<Match> degrouped = Sets.newLinkedHashSet();
    for (Match[] matches : matchgroups) {
      degrouped.add(matches[0]);
    }
    return degrouped;
    //  end
  }

  //  def valid_permutation?(_matchgroups)
  @SuppressWarnings("boxing")
  private boolean validPermutation(List<Match[]> _matchgroups) {
    //    single_matchgroups = _matchgroups.all?{ |mg| mg.size==1 }
    boolean onlyOneMatchPerGroup = Iterables.all(_matchgroups, new Predicate<Match[]>() {
      public boolean apply(Match[] mg) {
        return mg.length == 1;
      }
    });
    //    all_positions1 = _matchgroups.collect{ |mg| mg.collect{|m| m.word1.position}}
    //    all_positions2 = _matchgroups.collect{ |mg| mg.collect{|m| m.word2.position}}
    List<Integer> allBasePositions = Lists.newArrayList();
    List<Integer> allWitnessPositions = Lists.newArrayList();
    for (Match[] mg : _matchgroups) {
      for (Match m : mg) {
        allBasePositions.add(m.getBaseWord().position);
        allWitnessPositions.add(m.getWitnessWord().position);
      }
    }

    Set<Integer> uniqueBasePositions = Sets.newHashSet(allBasePositions);
    Set<Integer> uniqueWitnessPositions = Sets.newHashSet(allWitnessPositions);
    //    unique_word1 = all_positions1.uniq.size == all_positions1.size
    boolean baseWordIsUnique = uniqueBasePositions.size() == allBasePositions.size();
    //    unique_word2 = all_positions2.uniq.size == all_positions2.size
    boolean witnessWordIsUnique = uniqueWitnessPositions.size() == allWitnessPositions.size();
    //    return single_matchgroups && unique_word1 && unique_word2
    return onlyOneMatchPerGroup && baseWordIsUnique && witnessWordIsUnique;
    //  end
  }

  public List<Set<Match>> permutations() {
    return permutations;
  }

}
