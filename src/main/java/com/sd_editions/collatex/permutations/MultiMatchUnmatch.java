package com.sd_editions.collatex.permutations;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class MultiMatchUnmatch {

  private final List<MisMatch> unmatches;
  private final Set<MultiMatch> matches;

  public MultiMatchUnmatch(MatchUnmatch... _matchUnmatches) {
    this.matches = determineAllCommonMatches(_matchUnmatches);
    this.unmatches = determineUnmatches();
  }

  private Set<MultiMatch> determineAllCommonMatches(MatchUnmatch... matchUnmatches) {
    // These unmatches should all have the same base
    Set<MultiMatch> commonMatches = Sets.newHashSet();
    // initialize with the first matchset
    Function<Match, MultiMatch> match2multimatch = new Function<Match, MultiMatch>() {
      @Override
      public MultiMatch apply(Match match) {
        return new MultiMatch(match.getBaseWord(), match.getWitnessWord());
      }
    };
    commonMatches.addAll((Collection<MultiMatch>) Iterables.transform(matchUnmatches[0].getPermutation(), match2multimatch));

    // now, for the rest of the unmatches, check the basewords from the matchset against the basewords from the commonmatches
    // add witnessword to the multimatch if the baseword is found, delete the multimatch if it's not found.
    Function<MultiMatch, Word> extractBaseWord = new Function<MultiMatch, Word>() {
      @Override
      public Word apply(MultiMatch multimatch) {
        return multimatch.getWords().get(0);
      }
    };
    for (int i = 1; i < matchUnmatches.length; i++) {
      for (MultiMatch multiMatch : commonMatches) {
        final Word baseWord = multiMatch.getWords().get(0);

        Predicate<Match> sameBaseword = new Predicate<Match>() {
          @Override
          public boolean apply(Match match) {
            return match.getBaseWord().equals(baseWord);
          }
        };
        for (Match match : Iterables.filter(matchUnmatches[i].getPermutation(), sameBaseword)) {
          multiMatch.addMatchingWord(match.getWitnessWord());
        }
      }

      // now we can remove the multimatches from commomMatches that have only 1+i words,
      // since these are multimatches that don't have a match in matchUnmatch[i]
      final int expectedNumberOfWordsInMultiMatch = 2 + i;
      Predicate<MultiMatch> multiMatchHasTooFewWords = new Predicate<MultiMatch>() {
        @Override
        public boolean apply(MultiMatch multiMatch) {
          return multiMatch.getWords().size() < expectedNumberOfWordsInMultiMatch;
        }
      };
      commonMatches.removeAll((Collection<MultiMatch>) Iterables.filter(commonMatches, multiMatchHasTooFewWords));
    }

    return commonMatches;
  }

  private List<MisMatch> determineUnmatches() {
    List<MisMatch> misMatchList = Lists.newArrayList();
    for (MultiMatch multiMatch : this.matches) {

    }
    return misMatchList;
  }

  public Set<MultiMatch> getMatches() {
    return matches;
  }

  public List<MisMatch> getUnmatches() {
    return unmatches;
  }
}
