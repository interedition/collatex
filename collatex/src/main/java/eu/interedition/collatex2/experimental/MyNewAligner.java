package eu.interedition.collatex2.experimental;

import java.util.List;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class MyNewAligner {

  public List<IAlignedToken> align(IWitness a, IWitness b) {
    List<IAlignedToken> alignedTokens = Lists.newArrayList();
    MyNewMatcher matcher = new MyNewMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b);
    for (INormalizedToken token: a.getTokens()) {
      if (matches.keys().count(token)==1) {
        IAlignedToken alignedToken = new AlignedToken(token, matches.get(token).get(0));
        alignedTokens.add(alignedToken);
      }
    }
    return alignedTokens;
  }

}
