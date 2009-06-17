package eu.interedition.collatex.matching;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.sd_editions.collatex.permutations.MatchGroup;

import eu.interedition.collatex.collation.Match;
import eu.interedition.collatex.input.Word;

// TODO: remove the word possible from the name
public class PossibleMatches {
  private final Set<Match> fixedMatches;
  private final Set<Match> unfixedMatches;

  private final Multimap<Word, Match> baseToWitness;
  private Multimap<Word, Match> witnessToBase;

  public PossibleMatches(Set<Match> _fixedMatches, Set<Match> _unfixedMatches) {
    this.fixedMatches = _fixedMatches;
    this.unfixedMatches = _unfixedMatches;
    this.baseToWitness = matchGroupsForBase(unfixedMatches);
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

  public PossibleMatches fixMatch(Match match) {
    Set<Match> newFixedMatches = Sets.newLinkedHashSet();
    newFixedMatches.addAll(fixedMatches);
    newFixedMatches.add(match);
    Set<Match> newUnfixedMatches = filterAwayNoLongerPossibleMatches(unfixedMatches, match);
    PossibleMatches matches = new PossibleMatches(newFixedMatches, newUnfixedMatches);
    return matches;
  }

  public static Iterable<Match> findAlternativesBase(Iterable<Match> pmatches, final Match pmatch) {
    Predicate<Match> unfixedAlternativeToGivenPMatch = new Predicate<Match>() {
      public boolean apply(Match pm) {
        return pm != pmatch && (pm.getBaseWord().equals(pmatch.getBaseWord()) /* || pm.getWitnessWord().equals(pmatch.getWitnessWord())*/);
      }
    };
    return Iterables.filter(pmatches, unfixedAlternativeToGivenPMatch);
  }

  Iterable<Match> findAlternativesWitness(Iterable<Match> pmatches, final Match pmatch) {
    Predicate<Match> unfixedAlternativeToGivenPMatch = new Predicate<Match>() {
      public boolean apply(Match pm) {
        return pm != pmatch && (pm.getWitnessWord().equals(pmatch.getWitnessWord()));
      }
    };
    return Iterables.filter(pmatches, unfixedAlternativeToGivenPMatch);
  }

  private Multimap<Word, Match> matchGroupsForBase(Set<Match> allMatches) {
    Multimap<Word, Match> matchGroupsForBase = Multimaps.newLinkedListMultimap();
    for (Match match : allMatches) {
      matchGroupsForBase.put(match.getBaseWord(), match);
    }
    return matchGroupsForBase;
  }

  private Map<Integer, MatchGroup> matchGroupsForWitness(Set<Match> allMatches) {
    // Note: this is very much like a key multiple value map!
    Map<Integer, MatchGroup> matchGroupsForWitness = Maps.newLinkedHashMap();
    for (Match match : allMatches) {
      Iterable<Match> alternatives = findAlternativesWitness(allMatches, match);
      if (alternatives.iterator().hasNext()) {
        // start MatchGroup van de iterator
        MatchGroup group = new MatchGroup();
        group.add(match);
        group.addAll(alternatives);
        matchGroupsForWitness.put(new Integer(match.getWitnessWord().position), group);
      }
    }
    return matchGroupsForWitness;
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

}
