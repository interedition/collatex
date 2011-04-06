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
    for (INormalizedToken token: b.getTokens()) {
      if (matches.keys().count(token)==1) {
        alignedTokens.put(token, matches.get(token).get(0));
      }
    }
    IWitnessIndex index = new MyNewWitnessIndex(b, matches);
    WitnessAfgeleide afgeleider = new WitnessAfgeleide();
    List<INormalizedToken> afgeleide = afgeleider.calculateAfgeleide(a, matches);
    for (ITokenSequence sequence : index.getTokenSequences()) {
      // System.out.println("Trying to find token sequence: "+sequence);
      INormalizedToken fixedToken = matches.get(sequence.getFirstToken()).get(0);
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
