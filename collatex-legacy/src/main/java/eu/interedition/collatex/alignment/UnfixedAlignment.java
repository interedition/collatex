package eu.interedition.collatex.alignment;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import eu.interedition.collatex.input.BaseElement;

//TODO rename to Matches!
public class UnfixedAlignment<T extends BaseElement> {
  private final Set<Match<T>> fixedMatches;
  private final Set<Match<T>> unfixedMatches;

  private final Multimap<T, Match<T>> baseToWitness;
  private final Multimap<T, Match<T>> witnessToBase;
  private final UnfixedAlignment<T> previous;

  public UnfixedAlignment(final Set<Match<T>> _fixedMatches, final Set<Match<T>> _unfixedMatches) {
    this(_fixedMatches, _unfixedMatches, null);

    //    System.out.println(fixedMatches.toString());
    //    System.out.println(unfixedMatches.toString());
  }

  public UnfixedAlignment(final Set<Match<T>> _fixedMatches, final Set<Match<T>> _unfixedMatches, final UnfixedAlignment<T> _previous) {
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

  public Set<T> getUnfixedElementsInWitness() {
    return witnessToBase.keySet();
  }

  public Collection<Match<T>> getMatchesThatLinkFrom(final T word) {
    return baseToWitness.get(word);
  }

  public Collection<Match<T>> getMatchesThatLinkTo(final T word) {
    return witnessToBase.get(word);
  }

  public UnfixedAlignment<T> fixMatch(final Match<T> match) {
    final Set<Match<T>> newFixedMatches = Sets.newLinkedHashSet();
    newFixedMatches.addAll(fixedMatches);
    newFixedMatches.add(match);
    final Set<Match<T>> newUnfixedMatches = filterAwayNoLongerPossibleMatches(unfixedMatches, match);
    final UnfixedAlignment<T> matches = new UnfixedAlignment<T>(newFixedMatches, newUnfixedMatches, this);
    return matches;
  }

  // group matches by common base word or common witness word
  private Multimap<T, Match<T>> groupMatchesForBase(final Set<Match<T>> _matches) {
    final Multimap<T, Match<T>> matchGroupsForBase = LinkedListMultimap.create();
    for (final Match<T> match : _matches) {
      matchGroupsForBase.put(match.getBaseWord(), match);
    }
    return matchGroupsForBase;
  }

  private Multimap<T, Match<T>> groupMatchesForWitness(final Set<Match<T>> _matches) {
    final Multimap<T, Match<T>> groupMatchesForWitness = LinkedListMultimap.create();
    for (final Match<T> match : _matches) {
      groupMatchesForWitness.put(match.getWitnessWord(), match);
    }
    return groupMatchesForWitness;
  }

  private Set<Match<T>> filterAwayNoLongerPossibleMatches(final Set<Match<T>> unfixedMatches2, final Match<T> possibleMatch) {
    final Set<Match<T>> results = Sets.newLinkedHashSet();
    for (final Match<T> match : unfixedMatches2) {
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
    final List<Match<T>> nearMatches = Lists.newArrayList();
    for (final Match<T> match : unfixedMatches) {
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

  public int size() {
    return getFixedMatches().size();
  }

  public Iterator<Match<T>> iterator() {
    return getFixedMatches().iterator();
  }

}
