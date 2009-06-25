package eu.interedition.collatex.collation.alignment;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.collation.Collation;
import eu.interedition.collatex.collation.gaps.Gap;
import eu.interedition.collatex.collation.sequences.MatchSequence;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.match.worddistance.NormalizedLevenshtein;
import eu.interedition.collatex.match.worddistance.WordDistance;

public class Matcher {

  // Note: The WordDistance parameter should be parameterized!
  public static Alignment align(Witness a, Witness b) {
    Set<Match> allMatches = findMatches(a, b, new NormalizedLevenshtein());

    // Note: this code is not the simplest thing that 
    // could possibly work!
    Set<Match> exactMatches = Sets.newLinkedHashSet();
    for (Match match : allMatches) {
      Iterable<Match> alternatives = Matcher.findAlternatives(allMatches, match);
      if (!alternatives.iterator().hasNext()) {
        exactMatches.add(match);
      }
    }

    Set<Match> unfixedMatches = Sets.newLinkedHashSet(allMatches);
    unfixedMatches.removeAll(exactMatches);

    Alignment alignment = new Alignment(exactMatches, unfixedMatches);

    while (alignment.hasUnfixedWords()) {
      alignment = Matcher.permutate(a, b, alignment);
    }

    return alignment;
  }

  private static Set<Match> findMatches(Witness base, Witness witness, WordDistance distanceMeasure) {
    Set<Match> matchSet = Sets.newLinkedHashSet();
    for (Word baseWord : base.getWords()) {
      for (Word witnessWord : witness.getWords()) {
        if (baseWord.normalized.equals(witnessWord.normalized)) {
          matchSet.add(new Match(baseWord, witnessWord));
        } else {
          float editDistance = distanceMeasure.distance(baseWord.normalized, witnessWord.normalized);
          if (editDistance < 0.5) matchSet.add(new Match(baseWord, witnessWord, editDistance));
        }
      }
    }
    return matchSet;
  }

  public static Alignment permutate(Witness a, Witness b, final Alignment alignment) {
    Collection<Match> unfixedMatches = getMatchesToPermutateWith(alignment);
    List<Alignment> alignments = getAlignmentsForUnfixedMatches(alignment, unfixedMatches);
    Alignment bestAlignment = selectBestPossibleAlignment(a, b, alignments);
    return bestAlignment;
  }

  private static Collection<Match> getMatchesToPermutateWith(final Alignment alignment) {
    Word nextBase = selectNextUnfixedWordToAlign(alignment);
    Collection<Match> unfixedMatchesFrom = alignment.getMatchesThatLinkFrom(nextBase);
    Word nextWitness = unfixedMatchesFrom.iterator().next().getWitnessWord();
    Collection<Match> unfixedMatchesTo = alignment.getMatchesThatLinkTo(nextWitness);
    Collection<Match> unfixedMatches;
    if (unfixedMatchesFrom.size() > unfixedMatchesTo.size()) {
      unfixedMatches = unfixedMatchesFrom;
      //      System.out.println("next word that is going to be matched: (from a) " + nextBase + " at position: " + nextBase.position);
    } else {
      unfixedMatches = unfixedMatchesTo;
      //      System.out.println("next word that is going to be matched: (from b) " + nextWitness + " at position: " + nextWitness.position);
    }
    return unfixedMatches;
  }

  private static Word selectNextUnfixedWordToAlign(final Alignment alignment) {
    // Check whether there are unfixed near matches.
    // Align them first!
    // Note: this is probably not generic enough!
    if (!alignment.getUnfixedNearMatches().isEmpty()) {
      Word nextNearFromBase = alignment.getUnfixedNearMatches().iterator().next().getBaseWord();
      return nextNearFromBase;
    }

    Set<Word> unfixedWords = alignment.getUnfixedWords();
    Word nextBase = unfixedWords.iterator().next();
    return nextBase;
  }

  // TODO: naming here is not cool!
  private static List<Alignment> getAlignmentsForUnfixedMatches(Alignment previousAlignment, Collection<Match> unfixedMatches) {
    List<Alignment> permutationsForMatchGroup = Lists.newArrayList();
    for (Match possibleMatch : unfixedMatches) {
      Alignment alignment = previousAlignment.fixMatch(possibleMatch);
      alignment = fixTheOnlyOtherPossibleMatch(unfixedMatches, possibleMatch, alignment);
      permutationsForMatchGroup.add(alignment);
    }
    return permutationsForMatchGroup;
  }

  private static Alignment fixTheOnlyOtherPossibleMatch(Collection<Match> unfixedMatches, Match possibleMatch, final Alignment alignment) {
    Alignment result = alignment;
    if (unfixedMatches.size() == 2) {
      Set<Match> temp = Sets.newLinkedHashSet(unfixedMatches);
      temp.remove(possibleMatch);
      Match matchToSearch = temp.iterator().next();
      Set<Match> unfixedMatchesInNewAlignment = alignment.getUnfixedMatches();
      Match matchToFix = null;
      for (Match matchToCheck : unfixedMatchesInNewAlignment) {
        if (matchToFix == null && (matchToCheck.getBaseWord().equals(matchToSearch.getBaseWord()) || matchToCheck.getWitnessWord().equals(matchToSearch.getWitnessWord()))) {
          matchToFix = matchToCheck;
        }
      }
      if (matchToFix != null) {
        //        System.out.println("Also fixed match " + matchToFix);
        result = alignment.fixMatch(matchToFix);
      }
    }
    return result;
  }

  // TODO: move all the collation creation out of the way!
  private static Alignment selectBestPossibleAlignment(Witness a, Witness b, List<Alignment> alignments) {
    Alignment bestAlignment = null;
    Collation bestCollation = null;

    // TODO: add test for lowest number of matchsequences (transpositions)
    // NOTE: this can be done in a nicer way with the min function!
    for (Alignment alignment : alignments) {
      Collation collation = new Collation(alignment.getFixedMatches(), a, b);
      List<Gap> nonMatches = collation.getGaps();
      List<MatchSequence> matchSequences = collation.getMatchSequences();
      //      System.out.println(alignment.getFixedMatches().toString() + ":" + matchSequences.size() + ":" + nonMatches.size());
      if (bestAlignment == null || bestCollation == null || matchSequences.size() < bestCollation.getMatchSequences().size() || nonMatches.size() < bestCollation.getGaps().size()) {
        bestAlignment = alignment;
        bestCollation = new Collation(bestAlignment.getFixedMatches(), a, b);
      }
    }
    if (bestAlignment == null) throw new RuntimeException("Unexpected error!");
    return bestAlignment;
  }

  private static Iterable<Match> findAlternatives(Iterable<Match> pmatches, final Match pmatch) {
    Predicate<Match> unfixedAlternativeToGivenPMatch = new Predicate<Match>() {
      public boolean apply(Match pm) {
        return pm != pmatch && (pm.getBaseWord().equals(pmatch.getBaseWord()) || pm.getWitnessWord().equals(pmatch.getWitnessWord()));
      }
    };
    return Iterables.filter(pmatches, unfixedAlternativeToGivenPMatch);
  }
}
