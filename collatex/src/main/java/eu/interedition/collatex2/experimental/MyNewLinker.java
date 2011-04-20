package eu.interedition.collatex2.experimental;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ListMultimap;
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
    IWitnessIndex index = indexer.index(b, matches, matchResult);
    for (ITokenSequence sequence : index.getTokenSequences()) {
      // System.out.println("Trying to find token sequence: "+sequence);
      final INormalizedToken fixedWitnessToken = sequence.getFirstToken();
      final List<INormalizedToken> matchesForFixedTokenWitness = matches.get(fixedWitnessToken);
      if (matchesForFixedTokenWitness.isEmpty()) {
        throw new RuntimeException("No match found in base for fixed witness token! token: "+fixedWitnessToken);
      }
      INormalizedToken fixedToken = matchesForFixedTokenWitness.get(0);
      List<INormalizedToken> possibilities = matches.get(sequence.getLastToken());
      for (INormalizedToken possibility : possibilities) {
        // System.out.println("Trying possibilty: "+possibility);
        int distance = afgeleide.indexOf(possibility) - afgeleide.indexOf(fixedToken);
        if (distance == 1) {
          alignedTokens.put(sequence.getLastToken(), possibility);
          // System.out.println(possibility+" wins !");
        }
      }
    }
    return alignedTokens;
  }
}
