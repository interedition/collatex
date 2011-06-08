package eu.interedition.collatex2.experimental;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.experimental.matching.MyNewMatcher;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class MatchResultAnalyzer {

  public IMatchResult analyze(IWitness superbase, IWitness witness) {
    //Warning: TheAligner does matching also!
    MyNewMatcher matcher = new MyNewMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(superbase, witness);
    // unmatched tokens
    Set<INormalizedToken> unmatchedTokens = Sets.newLinkedHashSet();
    for (INormalizedToken token : witness.getTokens()) {
      if (!matches.containsKey(token)) {
        unmatchedTokens.add(token);
      }
    }
    // unsure tokens (have to check: base -> witness, and witness -> base) 
    Set<INormalizedToken> unsureTokens = Sets.newLinkedHashSet();
    for (INormalizedToken token : witness.getTokens()) {
      int count = matches.keys().count(token);
      if (count > 1) {
        unsureTokens.add(token);
      }
    }
    Multiset<INormalizedToken> bag = ImmutableMultiset.copyOf(matches.values());
    Set<INormalizedToken> unsureBaseTokens =  Sets.newLinkedHashSet();
    for (INormalizedToken token : superbase.getTokens()) {
      int count = bag.count(token);
      if (count > 1) {
        unsureBaseTokens.add(token);
      }
    }    
    Collection<Entry<INormalizedToken, INormalizedToken>> entries = matches.entries();
    for (Entry<INormalizedToken, INormalizedToken> entry : entries) {
      if (unsureBaseTokens.contains(entry.getValue())) {
        unsureTokens.add(entry.getKey());
      }
    }
    // sure tokens
    // have to check unsure tokens because of (base -> witness && witness -> base)
    Set<INormalizedToken> sureTokens = Sets.newLinkedHashSet();
    for (INormalizedToken token: witness.getTokens()) {
      if (matches.keys().count(token)==1&&!unsureTokens.contains(token)) {
        sureTokens.add(token);
      }
    }
    return new MatchResult(unmatchedTokens, unsureTokens, sureTokens);
  }
}
