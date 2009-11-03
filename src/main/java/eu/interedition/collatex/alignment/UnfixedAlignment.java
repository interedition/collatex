package eu.interedition.collatex.alignment;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import eu.interedition.collatex.input.BaseElement;

public class UnfixedAlignment<T extends BaseElement> {
  private final Set<Match<T>> fixedMatches;
  private final Set<Match<T>> unfixedMatches;

  private final Multimap<T, Match<T>> baseToWitness;
  private final Multimap<T, Match<T>> witnessToBase;
  private final UnfixedAlignment<T> previous;

  public UnfixedAlignment(Set<Match<T>> _fixedMatches, Set<Match<T>> _unfixedMatches) {
    this(_fixedMatches, _unfixedMatches, null);

    //    System.out.println(fixedMatches.toString());
    //    System.out.println(unfixedMatches.toString());
  }

  public UnfixedAlignment(Set<Match<T>> _fixedMatches, Set<Match<T>> _unfixedMatches, UnfixedAlignment<T> _previous) {
    this.fixedMatches = _fixedMatches;
    this.unfixedMatches = _unfixedMatches;
    this.baseToWitness = groupMatchesForBase(unfixedMatches);
    this.witnessToBase = groupMatchesForWitness(unfixedMatches);
    this.previous = _previous;
  }

  public Set<Match<T>> getFixedMatches() {
    return fixedMatches;
  }

  public Set<T> getUnfixedWords() {
    return baseToWitness.keySet();
  }

  public Collection<Match<T>> getMatchesThatLinkFrom(T word) {
    return baseToWitness.get(word);
  }

  public Collection<Match<T>> getMatchesThatLinkTo(T word) {
    return witnessToBase.get(word);
  }

  public UnfixedAlignment<T> fixMatch(Match<T> match) {
    Set<Match<T>> newFixedMatches = Sets.newLinkedHashSet();
    newFixedMatches.addAll(fixedMatches);
    newFixedMatches.add(match);
    Set<Match<T>> newUnfixedMatches = filterAwayNoLongerPossibleMatches(unfixedMatches, match);
    UnfixedAlignment<T> matches = new UnfixedAlignment<T>(newFixedMatches, newUnfixedMatches, this);
    return matches;
  }

  // group matches by common base word or common witness word
  private Multimap<T, Match<T>> groupMatchesForBase(Set<Match<T>> _matches) {
    Multimap<T, Match<T>> matchGroupsForBase = Multimaps.newLinkedListMultimap();
    for (Match<T> match : _matches) {
      matchGroupsForBase.put(match.getBaseWord(), match);
    }
    return matchGroupsForBase;
  }

  private Multimap<T, Match<T>> groupMatchesForWitness(Set<Match<T>> _matches) {
    Multimap<T, Match<T>> groupMatchesForWitness = Multimaps.newLinkedListMultimap();
    for (Match<T> match : _matches) {
      groupMatchesForWitness.put(match.getWitnessWord(), match);
    }
    return groupMatchesForWitness;
  }

  private Set<Match<T>> filterAwayNoLongerPossibleMatches(Set<Match<T>> unfixedMatches2, Match<T> possibleMatch) {
    Set<Match<T>> results = Sets.newLinkedHashSet();
    for (Match<T> match : unfixedMatches2) {
      if (match.getBaseWord().equals(possibleMatch.getBaseWord()) || match.getWitnessWord().equals(possibleMatch.getWitnessWord())) {
        // do nothing... this one should be filtered away
      } else {
        results.add(match);
      }
    }
    return results;
  }

  public boolean hasUnfixedWords() {
    return !baseToWitness.keySet().isEmpty();
  }

  public List<Match<T>> getUnfixedNearMatches() {
    List<Match<T>> nearMatches = Lists.newArrayList();
    for (Match<T> match : unfixedMatches) {
      if (match.wordDistance > 0) {
        nearMatches.add(match);
      }
    }
    return nearMatches;
  }

  public Set<Match<T>> getUnfixedMatches() {
    return unfixedMatches;
  }

  public UnfixedAlignment<T> getPrevious() {
    return previous;
  }

}
