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

  public Collation collate(Witness a, Witness b) {
    Alignment alignment = align(a, b);
    while (alignment.hasUnfixedWords()) {
      alignment = permutate(a, b, alignment);
    }
    Collation collation = new Collation(alignment.getFixedMatches(), a, b);
    return collation;
  }

  public Alignment align(Witness a, Witness b) {
    Set<Match> allMatches = findMatches(a, b);
    // group matches by common base word or common witness word
    Set<Match> exactMatches = Sets.newLinkedHashSet();
    for (Match match : allMatches) {
      Iterable<Match> alternatives = Alignment.findAlternativesBase(allMatches, match);
      if (!alternatives.iterator().hasNext()) {
        exactMatches.add(match);
      }
    }

    Set<Match> unfixedMatches = Sets.newLinkedHashSet(allMatches);
    unfixedMatches.removeAll(exactMatches);
    Alignment posMatches = new Alignment(exactMatches, unfixedMatches);
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

  private Alignment permutate(Witness a, Witness b, final Alignment alignment) {
    Collection<Match> unfixedMatches = getMatchesToPermutateWith(alignment);
    List<Alignment> alignments = getAlignmentsForUnfixedMatches(alignment, unfixedMatches);
    Alignment bestAlignment = selectBestPossibleAlignment(a, b, alignments);
    return bestAlignment;
  }

  private Collection<Match> getMatchesToPermutateWith(final Alignment alignment) {
    Set<Word> unfixedWords = alignment.getUnfixedWords();
    Word nextBase = unfixedWords.iterator().next();
    Collection<Match> unfixedMatchesFrom = alignment.getMatchesThatLinkFrom(nextBase);
    Word nextWitness = unfixedMatchesFrom.iterator().next().getWitnessWord();
    Collection<Match> unfixedMatchesTo = alignment.getMatchesThatLinkTo(nextWitness);
    Collection<Match> unfixedMatches;
    if (unfixedMatchesFrom.size() > unfixedMatchesTo.size()) {
      unfixedMatches = unfixedMatchesFrom;
      System.out.println("next word that is going to be matched: " + nextBase + " at position: " + nextBase.position);
    } else {
      unfixedMatches = unfixedMatchesTo;
      System.out.println("next word that is going to be matched: " + nextWitness + " at position: " + nextWitness.position);
    }
    return unfixedMatches;
  }

  // TODO: naming here is not cool!
  private List<Alignment> getAlignmentsForUnfixedMatches(Alignment previousAlignment, Collection<Match> unfixedMatches) {
    List<Alignment> permutationsForMatchGroup = Lists.newArrayList();
    for (Match possibleMatch : unfixedMatches) {
      Alignment alignment = previousAlignment.fixMatch(possibleMatch);
      permutationsForMatchGroup.add(alignment);
    }
    return permutationsForMatchGroup;
  }

  // TODO: move all the collation creation out of the way!
  private Alignment selectBestPossibleAlignment(Witness a, Witness b, List<Alignment> alignments) {
    Alignment bestAlignment = null;
    Collation bestCollation = null;

    // TODO: add test for lowest number of matchsequences (transpositions)
    // NOTE: this can be done in a nicer way with the min function!
    for (Alignment alignment : alignments) {
      Collation collation = new Collation(alignment.getFixedMatches(), a, b);
      List<NonMatch> nonMatches = collation.getNonMatches();
      List<MatchSequence> matchSequences = collation.getMatchSequences();
      if (bestAlignment == null || bestCollation == null || matchSequences.size() < bestCollation.getMatchSequences().size() || nonMatches.size() < bestCollation.getNonMatches().size()) {
        bestAlignment = alignment;
        bestCollation = new Collation(bestAlignment.getFixedMatches(), a, b);
      }
    }
    if (bestAlignment == null) throw new RuntimeException("Unexpected error!");
    return bestAlignment;
  }
}
