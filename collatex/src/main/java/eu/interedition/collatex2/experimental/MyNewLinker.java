package eu.interedition.collatex2.experimental;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;

public class MyNewLinker {

  public Map<ITokenSequence, IPhrase> link(IWitness a, IWitness b) {
    // do the matching
    MyNewMatcher matcher = new MyNewMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b);
    // add start and end tokens as matches
    final INormalizedToken startNullToken = a.getTokens().get(0);
    matches.put(new StartToken(), startNullToken);
    final INormalizedToken endNullToken = a.getTokens().get(a.size()-1);
    matches.put(new EndToken(b.size()), endNullToken);
     // Calculate MatchResult
    MatchResultAnalyzer analyzer = new MatchResultAnalyzer();
    IMatchResult matchResult = analyzer.analyze(a, b);
    // Calculate witness sequences using indexer
    MyNewWitnessIndexer indexer = new MyNewWitnessIndexer();
    IWitnessIndex index = indexer.index(b, matchResult);
    // Calculate 'afgeleide': Ignore non matches from the base
    BaseAfgeleider afgeleider = new BaseAfgeleider();
    List<INormalizedToken> afgeleide = afgeleider.calculateAfgeleide(a, matches);
    // try and find matches in the base for each sequence in the witness 
    Map<ITokenSequence, IPhrase> result = Maps.newLinkedHashMap();
    for (ITokenSequence sequence : index.getTokenSequences()) {
      // System.out.println("Trying to find token sequence: "+sequence.getNormalized());
      List<INormalizedToken> matchedBaseTokens;
      if (sequence.expandsToTheRight()) {
        matchedBaseTokens = findMatchingBaseTokensForSequenceToTheRight(sequence, matches, afgeleide);
      } else {
        matchedBaseTokens = findMatchingBaseTokensForSequenceToTheLeft(sequence, matches, afgeleide);
      }
      if (!matchedBaseTokens.isEmpty()) {
        // System.out.println("Matched!");
        result.put(sequence, new Phrase(matchedBaseTokens));
      }
    }
    return result;
  }
 
  //this method is just a bridge between the old and the new situation
  public Map<INormalizedToken, INormalizedToken> link2(IWitness a, IWitness b) {
    Map<ITokenSequence, IPhrase> linkedSequences = link(a, b);
    // do the matching
    MyNewMatcher matcher = new MyNewMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b);
    // Calculate MatchResult
    MatchResultAnalyzer analyzer = new MatchResultAnalyzer();
    IMatchResult matchResult = analyzer.analyze(a, b);
    // result map
    Map<INormalizedToken, INormalizedToken> alignedTokens = Maps.newLinkedHashMap();
    // put sure matches in the result map
    for (INormalizedToken token: matchResult.getSureTokens()) {
      alignedTokens.put(token, matches.get(token).get(0));
    }
    // add matched sequences to the aligned tokens
    for (ITokenSequence sequence : linkedSequences.keySet()) {
      IPhrase matchedBasePhrase = linkedSequences.get(sequence);
      Iterator<INormalizedToken> iterator = matchedBasePhrase.getTokens().iterator();
      for (INormalizedToken token : sequence.getTokens()) {
        INormalizedToken possibility = iterator.next();
        alignedTokens.put(token, possibility);
      }  
    }
    return alignedTokens;
  }
  
  
  // This method should return the matching base tokens for a given sequence 
  // Note: this method works for sequences that have the fixed token on the left and expand to the right
  private List<INormalizedToken> findMatchingBaseTokensForSequenceToTheRight(ITokenSequence sequence, ListMultimap<INormalizedToken, INormalizedToken> matches, List<INormalizedToken> afgeleide) {
    final INormalizedToken fixedWitnessToken = sequence.getFirstToken();
    final List<INormalizedToken> matchesForFixedTokenWitness = matches.get(fixedWitnessToken);
    if (matchesForFixedTokenWitness.isEmpty()) {
      throw new RuntimeException("No match found in base for fixed witness token! token: "+fixedWitnessToken);
    }
    if (matchesForFixedTokenWitness.size()!=1) {
      throw new RuntimeException("Multiple matches found in base for fixed witness token! tokens: "+fixedWitnessToken);
    }
    INormalizedToken fixedBaseToken = matchesForFixedTokenWitness.get(0);
    // traverse here the rest of the token sequence
    List<INormalizedToken> restTokens = Lists.newArrayList(sequence.getTokens());
    restTokens.remove(fixedWitnessToken);
    return tryTheDifferentPossibilities(matches, afgeleide, fixedBaseToken, restTokens, 1);
  }

  // This method should return the matching base tokens for a given sequence 
  // Note: this method works for sequences that have the fixed token on the right and expand to the left
  private List<INormalizedToken> findMatchingBaseTokensForSequenceToTheLeft(ITokenSequence sequence, ListMultimap<INormalizedToken, INormalizedToken> matches, List<INormalizedToken> afgeleide) {
    final INormalizedToken fixedWitnessToken = sequence.getLastToken();
    final List<INormalizedToken> matchesForFixedTokenWitness = matches.get(fixedWitnessToken);
    if (matchesForFixedTokenWitness.isEmpty()) {
      throw new RuntimeException("No match found in base for fixed witness token! token: "+fixedWitnessToken);
    }
    if (matchesForFixedTokenWitness.size()!=1) {
      throw new RuntimeException("Multiple matches found in base for fixed witness token! tokens: "+fixedWitnessToken);
    }
    INormalizedToken fixedBaseToken = matchesForFixedTokenWitness.get(0);
    // traverse here the rest of the token sequence
    List<INormalizedToken> restTokens = Lists.newArrayList(sequence.getTokens());
    restTokens.remove(fixedWitnessToken);
    Collections.reverse(restTokens);
    final List<INormalizedToken> matchedBaseTokens = tryTheDifferentPossibilities(matches, afgeleide, fixedBaseToken, restTokens, -1);
    Collections.reverse(matchedBaseTokens);
    return matchedBaseTokens;
  }

  private List<INormalizedToken> tryTheDifferentPossibilities(ListMultimap<INormalizedToken, INormalizedToken> matches, List<INormalizedToken> afgeleide, INormalizedToken fixedBaseToken,
      List<INormalizedToken> restTokens, int expectedDirection) {
    boolean validWholeSequence = true;
    List<INormalizedToken> matchedBaseTokens = Lists.newArrayList(fixedBaseToken);
    INormalizedToken lastToken = fixedBaseToken;
    for (INormalizedToken token : restTokens) {
      List<INormalizedToken> possibilities = matches.get(token);
      boolean valid = false;
      for (INormalizedToken possibility : possibilities) {
        int direction = afgeleide.indexOf(possibility) - afgeleide.indexOf(lastToken);
        if (direction == expectedDirection) {
          matchedBaseTokens.add(possibility);
          lastToken = possibility;
          valid = true;
          break;
        }
      }
      validWholeSequence = valid;
      if (!valid) {
        break;
      }
    }  
    if (validWholeSequence) {
      return matchedBaseTokens;
    } 
    return Collections.emptyList();
  }
}
