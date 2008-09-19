package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

public class Matches {
  private Witness base;
  private Witness witness;
  private List<Set<Match>> permutations;
  private Set<Match> matches;

  public Matches(Witness _base, Witness _witness) {
    this.base = _base;
    this.witness = _witness;
    this.matches = findMatches();
  }

  //def calculate_matches(w1,w2)
  private Set<Match> findMatches() {
    //    matches=[]
    Set<Match> matchSet = Sets.newHashSet();
    //    w1.words.each do |word1|
    for (Word baseWord : base.getWords()) {
      //      w2.words.each do |word2|
      for (Word witnessWord : witness.getWords()) {
        //       if (word1.normalized.eql? word2.normalized)
        if (baseWord.normalized.equals(witnessWord.normalized)) {
          //         matches << Match.new(word1,word2)
          matchSet.add(new Match(baseWord, witnessWord));
          //       else
          //        } else {
          ////          //         lev_distance = word1.distance_to(word2)
          ////          long levDistance = baseWord.distanceTo(witnessWord);
          ////          //         matches << Match.new(word1,word2,lev_distance) if (lev_distance<0.5)
          ////          Util.p(levDistance);
          ////          if (levDistance < 0.5) matchSet.add(new Match(baseWord, witnessWord, levDistance));
          ////          //       end
        }
        //      end
      }
      //    end
    }
    //    return matches
    return matchSet;
    //  end
  }

  public List<Set<Match>> permutations() {
    if (permutations == null) permutations = new MatchPermutator(matches).permutations();
    //    Util.p(matches());
    //    Util.p(permutations);
    return permutations;
  }

  /// Old stuff, remove?
  private WitnessIndex baseIndex;
  private WitnessIndex witnessIndex;

  public Matches(WitnessIndex _base, WitnessIndex _witness) {
    this.baseIndex = _base;
    this.witnessIndex = _witness;
  }

  // Integers are word codes
  public Set<Match> matches() {
    Set<Integer> matchesAsWordCodes = matchesAsWordCodes();
    Set<Match> matches = Sets.newLinkedHashSet();
    for (Integer matchAsWordCode : matchesAsWordCodes) {
      matches.add(convertWordCodeToMatch(baseIndex, witnessIndex, matchAsWordCode));
    }
    return matches;
  }

  private Set<Integer> matchesAsWordCodes() {
    Set<Integer> matchesAsWordCodes = Sets.newLinkedHashSet(baseIndex.getWordCodes());
    matchesAsWordCodes.retainAll(witnessIndex.getWordCodes());
    //    System.out.println(matchesAsWordCodes);
    return matchesAsWordCodes;
  }

  private static Match convertWordCodeToMatch(WitnessIndex base, WitnessIndex witness, Integer match) {
    Word word1 = base.getWordOnPosition(base.getPosition(match));
    Word word2 = witness.getWordOnPosition(witness.getPosition(match));
    return new Match(word1, word2);
  }

}
