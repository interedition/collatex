/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sd_editions.collatex.permutations;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.input.Word;

public class MultiMatchNonMatch {

  private final List<Gap> nonMatches;
  private final Set<MultiMatch> matches;

  public MultiMatchNonMatch(Alignment... _matchNonMatches) {
    this.matches = determineAllCommonMatches(_matchNonMatches);
    this.nonMatches = determineNonMatches();
  }

  private Set<MultiMatch> determineAllCommonMatches(Alignment<Word>... matchNonMatches) {
    // These nonMatches should all have the same base
    Set<MultiMatch> commonMatches = Sets.newHashSet();
    // initialize with the first matchset
    Function<Match<Word>, MultiMatch> match2multimatch = new Function<Match<Word>, MultiMatch>() {
      @Override
      public MultiMatch apply(Match<Word> match) {
        return new MultiMatch(match.getBaseWord(), match.getWitnessWord());
      }
    };
    commonMatches.addAll((Collection<MultiMatch>) Iterables.transform(matchNonMatches[0].getMatches(), match2multimatch));

    new Function<MultiMatch, Word>() {
      @Override
      public Word apply(MultiMatch multimatch) {
        return multimatch.getWords().get(0);
      }
    };
    for (int i = 1; i < matchNonMatches.length; i++) {
      for (MultiMatch multiMatch : commonMatches) {
        final Word baseWord = multiMatch.getWords().get(0);

        Predicate<Match<Word>> sameBaseword = new Predicate<Match<Word>>() {
          @Override
          public boolean apply(Match match) {
            return match.getBaseWord().equals(baseWord);
          }
        };
        for (Match<Word> match : Iterables.filter(matchNonMatches[i].getMatches(), sameBaseword)) {
          multiMatch.addMatchingWord(match.getWitnessWord());
        }
      }

      // now we can remove the multimatches from commomMatches that have only 1+i words,
      // since these are multimatches that don't have a match in matchNonMatch[i]
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

  private List<Gap> determineNonMatches() {
    List<Gap> nonMatchList = Lists.newArrayList();
    for (MultiMatch multiMatch : this.matches) {

    }
    return nonMatchList;
  }

  public Set<MultiMatch> getMatches() {
    return matches;
  }

  public List<Gap> getNonMatches() {
    return nonMatches;
  }
}
