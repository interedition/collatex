package eu.interedition.collatex2.experimental;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class MyNewLinker {

  public Map<INormalizedToken, INormalizedToken> link(IWitness a, IWitness b) {
    Map<INormalizedToken, INormalizedToken> alignedTokens = Maps.newLinkedHashMap();
    MyNewMatcher matcher = new MyNewMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b);
    MatchResultAnalyzer analyzer = new MatchResultAnalyzer();
    IMatchResult matchResult = analyzer.analyze(a, b);
    for (INormalizedToken token: b.getTokens()) {
      if (matches.keys().count(token)==1&&!matchResult.getUnsureTokens().contains(token)) {
        alignedTokens.put(token, matches.get(token).get(0));
      }
    }
    matches.put(new StartToken(), a.getTokens().get(0));
    BaseAfgeleider afgeleider = new BaseAfgeleider();
    List<INormalizedToken> afgeleide = afgeleider.calculateAfgeleide(a, matches);
    // System.out.println("Afgeleide: "+afgeleide);
    MyNewWitnessIndexer indexer = new MyNewWitnessIndexer();
    IWitnessIndex index = indexer.index(b, matchResult);
    for (ITokenSequence sequence : index.getTokenSequences()) {
      //TODO: remove this constraint!
      if (!sequence.isLeftAligned()) {
        continue;
      }
      // System.out.println("Trying to find token sequence: "+sequence.getNormalized());
      final INormalizedToken fixedWitnessToken = sequence.getFirstToken();
      final List<INormalizedToken> matchesForFixedTokenWitness = matches.get(fixedWitnessToken);
      if (matchesForFixedTokenWitness.isEmpty()) {
        throw new RuntimeException("No match found in base for fixed witness token! token: "+fixedWitnessToken);
      }
      INormalizedToken fixedToken = matchesForFixedTokenWitness.get(0);
      // traverse here the rest of the token sequence
      List<INormalizedToken> restTokens = Lists.newArrayList(sequence.getTokens());
      restTokens.remove(sequence.getFirstToken());
      boolean validWholeSequence = true;
      List<INormalizedToken> matchedBaseTokens = Lists.newArrayList();
      INormalizedToken lastToken = fixedToken;
      for (INormalizedToken token : restTokens) {
        List<INormalizedToken> possibilities = matches.get(token);
        boolean valid = false;
        for (INormalizedToken possibility : possibilities) {
          int distance = afgeleide.indexOf(possibility) - afgeleide.indexOf(lastToken);
          if (distance == 1) {
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
        // System.out.println("Matched!");
        for (INormalizedToken token : restTokens) {
          INormalizedToken possibility = matchedBaseTokens.remove(0);
          alignedTokens.put(token, possibility);
        }  
      }
    }
    return alignedTokens;
  }
}
