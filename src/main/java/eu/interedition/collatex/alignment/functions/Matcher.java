package eu.interedition.collatex.alignment.functions;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.alignment.UnfixedAlignment;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.match.worddistance.NormalizedLevenshtein;
import eu.interedition.collatex.match.worddistance.WordDistance;

// TODO: extract matching functionality from this class
// TODO: rename class to Aligner or something like that
public class Matcher {

  public static Alignment<Word> align(Witness a, Witness b) {
    return align(a.getFirstSegment(), b.getFirstSegment());
  }

  public static Alignment<Word> align(Segment a, Segment b) {
    UnfixedAlignment<Word> unfixedAlignment = createFirstUnfixedAlignment(a, b);

    while (unfixedAlignment.hasUnfixedWords()) {
      unfixedAlignment = Matcher.permutate(a, b, unfixedAlignment);
    }
    Alignment<Word> alignment = Alignment.create(unfixedAlignment.getFixedMatches(), a, b);
    return alignment;
  }

  public static UnfixedAlignment<Word> createFirstUnfixedAlignment(Segment a, Segment b) {
    Set<Match<Word>> allMatches = findMatches(a, b, new NormalizedLevenshtein());

    // Note: this code is not the simplest thing that 
    // could possibly work!
    Set<Match<Word>> exactMatches = Sets.newLinkedHashSet();
    for (Match<Word> match : allMatches) {
      Iterable<Match<Word>> alternatives = Matcher.findAlternatives(allMatches, match);
      if (!alternatives.iterator().hasNext()) {
        exactMatches.add(match);
      }
    }

    Set<Match<Word>> unfixedMatches = Sets.newLinkedHashSet(allMatches);
    unfixedMatches.removeAll(exactMatches);

    UnfixedAlignment unfixedAlignment = new UnfixedAlignment(exactMatches, unfixedMatches);
    return unfixedAlignment;
  }

  private static Set<Match<Word>> findMatches(Segment base, Segment witness, WordDistance distanceMeasure) {
    Set<Match<Word>> matchSet = Sets.newLinkedHashSet();
    for (Word baseWord : base.getWords()) {
      for (Word witnessWord : witness.getWords()) {
        if (baseWord.normalized.equals(witnessWord.normalized)) {
          matchSet.add(new Match<Word>(baseWord, witnessWord));
        } else {
          float editDistance = distanceMeasure.distance(baseWord.normalized, witnessWord.normalized);
          if (editDistance < 0.5) matchSet.add(new Match<Word>(baseWord, witnessWord, editDistance));
        }
      }
    }
    return matchSet;
  }

  public static UnfixedAlignment<Word> permutate(Segment a, Segment b, final UnfixedAlignment alignment) {
    Collection<Match<Word>> unfixedMatches = getMatchesToPermutateWith(alignment);
    List<UnfixedAlignment<Word>> alignments = getAlignmentsForUnfixedMatches(alignment, unfixedMatches);
    UnfixedAlignment<Word> bestAlignment = selectBestPossibleAlignment(a, b, alignments);
    return bestAlignment;
  }

  private static Collection<Match<Word>> getMatchesToPermutateWith(final UnfixedAlignment<Word> alignment) {
    Word nextBase = selectNextUnfixedWordToAlign(alignment);
    Collection<Match<Word>> unfixedMatchesFrom = alignment.getMatchesThatLinkFrom(nextBase);
    Word nextWitness = unfixedMatchesFrom.iterator().next().getWitnessWord();
    Collection<Match<Word>> unfixedMatchesTo = alignment.getMatchesThatLinkTo(nextWitness);
    Collection<Match<Word>> unfixedMatches;
    if (unfixedMatchesFrom.size() > unfixedMatchesTo.size()) {
      unfixedMatches = unfixedMatchesFrom;
      //      System.out.println("next word that is going to be matched: (from a) " + nextBase + " at position: " + nextBase.position);
    } else {
      unfixedMatches = unfixedMatchesTo;
      //      System.out.println("next word that is going to be matched: (from b) " + nextWitness + " at position: " + nextWitness.position);
    }
    return unfixedMatches;
  }

  private static Word selectNextUnfixedWordToAlign(final UnfixedAlignment<Word> alignment) {
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
  private static List<UnfixedAlignment<Word>> getAlignmentsForUnfixedMatches(UnfixedAlignment<Word> previousAlignment, Collection<Match<Word>> unfixedMatches) {
    List<UnfixedAlignment<Word>> permutationsForMatchGroup = Lists.newArrayList();
    for (Match<Word> possibleMatch : unfixedMatches) {
      UnfixedAlignment<Word> alignment = previousAlignment.fixMatch(possibleMatch);
      alignment = fixTheOnlyOtherPossibleMatch(unfixedMatches, possibleMatch, alignment);
      permutationsForMatchGroup.add(alignment);
    }
    return permutationsForMatchGroup;
  }

  private static UnfixedAlignment<Word> fixTheOnlyOtherPossibleMatch(Collection<Match<Word>> unfixedMatches, Match<Word> possibleMatch, final UnfixedAlignment<Word> alignment) {
    UnfixedAlignment<Word> result = alignment;
    if (unfixedMatches.size() == 2) {
      Set<Match<Word>> temp = Sets.newLinkedHashSet(unfixedMatches);
      temp.remove(possibleMatch);
      Match<Word> matchToSearch = temp.iterator().next();
      Set<Match<Word>> unfixedMatchesInNewAlignment = alignment.getUnfixedMatches();
      Match<Word> matchToFix = null;
      for (Match<Word> matchToCheck : unfixedMatchesInNewAlignment) {
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
  private static UnfixedAlignment<Word> selectBestPossibleAlignment(Segment a, Segment b, List<UnfixedAlignment<Word>> alignments) {
    UnfixedAlignment bestAlignment = null;
    Alignment bestCollation = null;

    // TODO: add test for lowest number of matchsequences (transpositions)
    // NOTE: this can be done in a nicer way with the min function!
    for (UnfixedAlignment alignment : alignments) {
      Alignment collation = Alignment.create(alignment.getFixedMatches(), a, b);
      List<Gap> nonMatches = collation.getGaps();
      List<MatchSequence> matchSequences = collation.getMatchSequences();
      //      System.out.println(alignment.getFixedMatches().toString() + ":" + matchSequences.size() + ":" + nonMatches.size());
      if (bestAlignment == null || bestCollation == null || matchSequences.size() < bestCollation.getMatchSequences().size() || nonMatches.size() < bestCollation.getGaps().size()) {
        bestAlignment = alignment;
        bestCollation = Alignment.create(bestAlignment.getFixedMatches(), a, b);
      }
    }
    if (bestAlignment == null) throw new RuntimeException("Unexpected error!");
    return bestAlignment;
  }

  private static Iterable<Match<Word>> findAlternatives(Iterable<Match<Word>> pmatches, final Match<Word> pmatch) {
    Predicate<Match<Word>> unfixedAlternativeToGivenPMatch = new Predicate<Match<Word>>() {
      public boolean apply(Match<Word> pm) {
        return pm != pmatch && (pm.getBaseWord().equals(pmatch.getBaseWord()) || pm.getWitnessWord().equals(pmatch.getWitnessWord()));
      }
    };
    return Iterables.filter(pmatches, unfixedAlternativeToGivenPMatch);
  }

}
