package eu.interedition.collatex.matching;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.collation.Match;
import eu.interedition.collatex.collation.NonMatch;
import eu.interedition.collatex.collation.sequences.MatchSequence;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class Matcher {

  public PossibleMatches match(Witness a, Witness b) {
    Set<Match> allMatches = findMatches(a, b);
    // group matches by common base word or common witness word
    Set<Match> exactMatches = Sets.newLinkedHashSet();
    for (Match match : allMatches) {
      Iterable<Match> alternatives = PossibleMatches.findAlternativesBase(allMatches, match);
      if (!alternatives.iterator().hasNext()) {
        exactMatches.add(match);
      }
    }

    Set<Match> unfixedMatches = Sets.newLinkedHashSet(allMatches);
    unfixedMatches.removeAll(exactMatches);
    PossibleMatches posMatches = new PossibleMatches(exactMatches, unfixedMatches);
    return posMatches;
  }

  // TODO: re-enable near matches!
  private Set<Match> findMatches(Witness base, Witness witness) {
    Set<Match> matchSet = Sets.newLinkedHashSet();
    for (Word baseWord : base.getWords()) {
      for (Word witnessWord : witness.getWords()) {
        if (baseWord.normalized.equals(witnessWord.normalized)) {
          matchSet.add(new Match(baseWord, witnessWord));
          //        } else {
          //          float editDistance = distanceMeasure.distance(baseWord.normalized, witnessWord.normalized);
          //          if (editDistance < 0.5) matchSet.add(new Match(baseWord, witnessWord, editDistance));
        }
      }
    }
    return matchSet;
  }

  // TODO: test separately
  // TODO: there is a problem here when there are no permutations!
  public Permutation getBestPermutation(Witness a, Witness b) {
    PossibleMatches matches = match(a, b);
    Permutation bestPermutation = null;
    while (matches.hasUnfixedWords()) {
      bestPermutation = permutationLoop(a, b, matches);
      matches = matches.fixMatch(bestPermutation.getPossibleMatch());
    }
    if (bestPermutation == null) {
      throw new RuntimeException("There are no permutations!");
    }
    return bestPermutation;
  }

  // TODO: rename!
  private Permutation permutationLoop(Witness a, Witness b, final PossibleMatches matches) {
    Set<Match> fixedMatches = matches.getFixedMatches();
    Set<Word> unfixedWords = matches.getUnfixedWords();
    Word nextBase = unfixedWords.iterator().next();
    Collection<Match> unfixedMatchesFrom = matches.getMatchesThatLinkFrom(nextBase);
    Word nextWitness = unfixedMatchesFrom.iterator().next().getWitnessWord();
    Collection<Match> unfixedMatchesTo = matches.getMatchesThatLinkTo(nextWitness);
    Collection<Match> unfixedMatches;
    if (unfixedMatchesFrom.size() > unfixedMatchesTo.size()) {
      unfixedMatches = unfixedMatchesFrom;
      System.out.println("next word that is going to be matched: " + nextBase + " at position: " + nextBase.position);
    } else {
      unfixedMatches = unfixedMatchesTo;
      System.out.println("next word that is going to be matched: " + nextWitness + " at position: " + nextWitness.position);
    }
    List<Permutation> permutations = getPermutationsForUnfixedMatches(fixedMatches, unfixedMatches);
    Permutation bestPermutation = selectBestPossiblePermutation(a, b, permutations);
    return bestPermutation;
  }

  private List<Permutation> getPermutationsForUnfixedMatches(Set<Match> fixedMatches, Collection<Match> unfixedMatches) {
    List<Permutation> permutationsForMatchGroup = Lists.newArrayList();
    for (Match possibleMatch : unfixedMatches) {
      Permutation permutation = new Permutation(fixedMatches, possibleMatch);
      permutationsForMatchGroup.add(permutation);
    }
    return permutationsForMatchGroup;
  }

  private Permutation selectBestPossiblePermutation(Witness a, Witness b, List<Permutation> permutations) {
    Permutation bestPermutation = null;

    // TODO: add test for lowest number of matchsequences (transpositions)
    // NOTE: this can be done in a nicer way with the min function!
    for (Permutation permutation : permutations) {
      List<NonMatch> nonMatches = permutation.getNonMatches(a, b);
      List<MatchSequence> matchSequences = permutation.getMatchSequences();
      if (bestPermutation == null || matchSequences.size() < bestPermutation.getMatchSequences().size() || nonMatches.size() < bestPermutation.getNonMatches(a, b).size()) {
        bestPermutation = permutation;
      }
    }
    if (bestPermutation == null) throw new RuntimeException("Unexpected error!");
    //System.out.println(bestPermutation.getPossibleMatch());
    return bestPermutation;
  }

}
