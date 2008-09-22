package com.sd_editions.collatex.spike2;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class MatchPermutator {

  private final Set<Match> possibleMatches;
  private final List<MatchGroup> groupedMatches;
  private final List<Set<Match>> permutations;

  public MatchPermutator(Set<Match> allPossibleMatches) {
    possibleMatches = allPossibleMatches;
    groupedMatches = group(possibleMatches);
    permutations = Lists.newArrayList();
    permutate(groupedMatches, 0, permutations);
  }

  @SuppressWarnings("boxing")
  protected List<MatchGroup> group(Set<Match> matches) {
    List<Match> matchList = Lists.sortedCopy(Lists.newArrayList(matches));

    List<MatchGroup> groups = Lists.newArrayList();
    List<Integer> t0 = Lists.newArrayList();
    List<Integer> t1 = Lists.newArrayList();
    for (Match match : matchList) {
      //      Util.p(match);
      MatchGroup group0 = new MatchGroup();
      MatchGroup group1 = new MatchGroup();
      final int basePosition = match.getBaseWord().position;
      final int witnessPosition = match.getWitnessWord().position;
      if (!t0.contains(basePosition)) {
        Predicate<Match> basePositionPredicate = new Predicate<Match>() {
          public boolean apply(Match _match) {
            return (_match.getBaseWord().position == basePosition);
          }
        };
        group0 = new MatchGroup(Iterables.filter(matches, basePositionPredicate));
        //        Util.p(group0);
        t0.add(basePosition);
      }
      if (!t1.contains(witnessPosition)) {
        Predicate<Match> witnessPositionPredicate = new Predicate<Match>() {
          public boolean apply(Match _match) {
            return (_match.getWitnessWord().position == witnessPosition);
          }
        };
        group1 = new MatchGroup(Iterables.filter(matches, witnessPositionPredicate));
        //        Util.p(group1);
        t1.add(witnessPosition);
      }
      Set<Match> set0 = group0.asSet();
      Set<Match> set1 = group1.asSet();
      MatchGroup matchGroup0 = group0;
      final Comparator<Match> comparator = new Comparator<Match>() {
        public int compare(Match m1, Match m2) {
          return m1.getBaseWord().position - m2.getBaseWord().position;
        }
      };
      matchGroup0.sort(comparator);
      MatchGroup matchGroup1 = group1;
      matchGroup1.sort(comparator);
      if (set0.containsAll(set1) && matchGroup0.size() > 0) {
        groups.add(matchGroup0);
      } else if (set1.containsAll(set0) && matchGroup1.size() > 0) {
        groups.add(matchGroup1);
      } else {
        if (matchGroup0.size() > 0) groups.add(matchGroup0);
        if (matchGroup1.size() > 0) groups.add(matchGroup1);
      }
    }
    //    System.out.println(groups);
    return groups;
  }

  private void permutate(List<MatchGroup> _groupedMatches, int startGroup, List<Set<Match>> _permutations) {
    //    if (valid_permutation?(matchgroups))
    if (validPermutation(_groupedMatches)) {
      //      permutations << degroup(matchgroups)
      _permutations.add(degroup(_groupedMatches));
      //    else
    } else {
      //      i = start_group
      int i = startGroup;
      //      while (!matchgroups[i].nil? && matchgroups[i].size==1 && i<matchgroups.size-1)
      while (i < _groupedMatches.size() - 1 && _groupedMatches.get(i) != null && _groupedMatches.get(i).size() == 1) {
        //        i+=1
        i++;
        //      end
      }
      //      if (i<matchgroups.size)
      if (i < _groupedMatches.size()) {
        //        matchgroups[i].each do |match|
        for (Match match : _groupedMatches.get(i)) {
          //          new_matchgroups = fix_cell(matchgroups, i, match)
          List<MatchGroup> newMatchGroups = fixCell(_groupedMatches, i, match);
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
  List<MatchGroup> fixCell(List<MatchGroup> _matchgroups, int index, Match match) {
    //    new_groups = []
    Set<MatchGroup> newGroups = Sets.newLinkedHashSet();
    //    _matchgroups.each_with_index do |mg,i|
    int i = 0;
    for (MatchGroup mg : _matchgroups) {
      //      if (i == index)
      if (i == index) {
        //        new_groups << [match]
        newGroups.add(new MatchGroup(match));
        //      else
      } else {
        //        new_groups << mg.select{ |m| m.word1 != match.word1 && m.word2 != match.word2 }
        MatchGroup matchgroup = new MatchGroup();
        for (Match m : mg) {
          if (m.getBaseWord().position != match.getBaseWord().position && m.getWitnessWord().position != match.getWitnessWord().position) {
            matchgroup.add(m);
          }
        }
        newGroups.add(matchgroup);
        //      end
      }
      i++;
      //    end
    }
    //    return new_groups.delete_if{ |g| g.empty? }
    return Lists.newArrayList(Iterables.filter(newGroups, new Predicate<MatchGroup>() {
      public boolean apply(MatchGroup matchGroup) {
        return matchGroup.size() > 0;
      }
    }));
    //  end
  }

  //  def degroup(matchgroups)
  Set<Match> degroup(List<MatchGroup> matchgroups) {
    //    matchgroups.collect{ |mg| mg[0] }
    Set<Match> degrouped = Sets.newLinkedHashSet();
    for (MatchGroup matches : matchgroups) {
      degrouped.add(matches.get(0));
    }
    return degrouped;
    //  end
  }

  //  def valid_permutation?(_matchgroups)
  @SuppressWarnings("boxing")
  private boolean validPermutation(List<MatchGroup> _matchgroups) {
    //    single_matchgroups = _matchgroups.all?{ |mg| mg.size==1 }
    boolean onlyOneMatchPerGroup = Iterables.all(_matchgroups, new Predicate<MatchGroup>() {
      public boolean apply(MatchGroup mg) {
        return mg.size() == 1;
      }
    });
    //    all_positions1 = _matchgroups.collect{ |mg| mg.collect{|m| m.word1.position}}
    //    all_positions2 = _matchgroups.collect{ |mg| mg.collect{|m| m.word2.position}}
    List<Integer> allBasePositions = Lists.newArrayList();
    List<Integer> allWitnessPositions = Lists.newArrayList();
    for (MatchGroup mg : _matchgroups) {
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
    boolean result = onlyOneMatchPerGroup && baseWordIsUnique && witnessWordIsUnique;
    //    System.out.println("incoming: " + _matchgroups);
    //    System.out.println("result: " + result);
    return result;
    //  end
  }

  public List<Set<Match>> permutations() {
    return permutations;
  }

}
