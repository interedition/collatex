package eu.interedition.collatex.collation.alignment;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import eu.interedition.collatex.input.Word;

public class Alignment {
  private final Set<Match> fixedMatches;
  private final Set<Match> unfixedMatches;

  private final Multimap<Word, Match> baseToWitness;
  private final Multimap<Word, Match> witnessToBase;

  public Alignment(Set<Match> _fixedMatches, Set<Match> _unfixedMatches) {
    this.fixedMatches = _fixedMatches;
    this.unfixedMatches = _unfixedMatches;
    this.baseToWitness = groupMatchesForBase(unfixedMatches);
    this.witnessToBase = groupMatchesForWitness(unfixedMatches);
    //    System.out.println(fixedMatches.toString());
    //    System.out.println(unfixedMatches.toString());
  }

  public Set<Match> getFixedMatches() {
    return fixedMatches;
  }

  public Set<Word> getUnfixedWords() {
    return baseToWitness.keySet();
  }

  public Collection<Match> getMatchesThatLinkFrom(Word word) {
    return baseToWitness.get(word);
  }

  public Collection<Match> getMatchesThatLinkTo(Word word) {
    return witnessToBase.get(word);
  }

  public Alignment fixMatch(Match match) {
    Set<Match> newFixedMatches = Sets.newLinkedHashSet();
    newFixedMatches.addAll(fixedMatches);
    newFixedMatches.add(match);
    Set<Match> newUnfixedMatches = filterAwayNoLongerPossibleMatches(unfixedMatches, match);
    Alignment matches = new Alignment(newFixedMatches, newUnfixedMatches);
    return matches;
  }

  // group matches by common base word or common witness word
  private Multimap<Word, Match> groupMatchesForBase(Set<Match> _matches) {
    Multimap<Word, Match> matchGroupsForBase = Multimaps.newLinkedListMultimap();
    for (Match match : _matches) {
      matchGroupsForBase.put(match.getBaseWord(), match);
    }
    return matchGroupsForBase;
  }

  private Multimap<Word, Match> groupMatchesForWitness(Set<Match> _matches) {
    Multimap<Word, Match> groupMatchesForWitness = Multimaps.newLinkedListMultimap();
    for (Match match : _matches) {
      groupMatchesForWitness.put(match.getWitnessWord(), match);
    }
    return groupMatchesForWitness;
  }

  private Set<Match> filterAwayNoLongerPossibleMatches(Set<Match> unfixedMatches2, Match possibleMatch) {
    Set<Match> results = Sets.newLinkedHashSet();
    for (Match match : unfixedMatches2) {
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

  public List<Match> getUnfixedNearMatches() {
    List<Match> nearMatches = Lists.newArrayList();
    for (Match match : unfixedMatches) {
      if (match.wordDistance > 0) {
        nearMatches.add(match);
      }
    }
    return nearMatches;
  }

  public Set<Match> getUnfixedMatches() {
    return unfixedMatches;
  }

}
