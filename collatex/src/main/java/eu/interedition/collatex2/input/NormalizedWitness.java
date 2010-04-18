package eu.interedition.collatex2.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;

public class NormalizedWitness implements Iterable<INormalizedToken>, IWitness {
  private String sigil;
  private List<INormalizedToken> tokens;

  public NormalizedWitness() {    
  }
  
  public NormalizedWitness(final String sigil, final List<INormalizedToken> tokens) {
    this.sigil = sigil;
    this.tokens = tokens;
  }

  public NormalizedWitness(final String sigil) {
    this.sigil = sigil;
    this.tokens = Lists.newArrayList();
  }

  // Note: not pleased with this method! implement Iterable!
  public List<INormalizedToken> getTokens() {
    return tokens;
  }

  public void setTokens(List<INormalizedToken> tokens) {
    this.tokens = tokens;
  }
  
  public String getSigil() {
    return sigil;
  }

  public void setSigil(String sigil) {
    this.sigil = sigil;
  }
  
  // TODO check whether iterator.remove() throws exception!
  @Override
  public Iterator<INormalizedToken> iterator() {
    return tokens.iterator();
  }

  public IPhrase createPhrase(final int startPosition, final int endPosition) {
    // TODO this problemCase shouldn't occur
    final boolean problemCase = (startPosition - 1 > endPosition);
    final List<INormalizedToken> subList = problemCase ? new ArrayList<INormalizedToken>() : tokens.subList(startPosition - 1, endPosition);
    return new Phrase(subList);
  }

  public int size() {
    return tokens.size();
  }

  @Override
  public List<String> findRepeatingTokens() {
    final Multimap<String, INormalizedToken> normalizedTokenMap = ArrayListMultimap.create();
    for (final INormalizedToken token : getTokens()) {
      normalizedTokenMap.put(token.getNormalized(), token);
    }
    final List<String> repeatingNormalizedTokens = Lists.newArrayList();
    for (final String key : normalizedTokenMap.keySet()) {
      final Collection<INormalizedToken> tokenCollection = normalizedTokenMap.get(key);
      if (tokenCollection.size() > 1) {
        repeatingNormalizedTokens.add(key);
      }
    }
    return repeatingNormalizedTokens;
  }
}
