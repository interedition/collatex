package eu.interedition.collatex2.implementation.alignmenttable;

import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class Column3 implements IColumn {
  private final Map<String, INormalizedToken> sigliToTokens;

  public Column3(final INormalizedToken token) {
    this.sigliToTokens = Maps.newHashMap();
    sigliToTokens.put(token.getSigil(), token);
  }

  @Override
  public boolean containsWitness(final String sigil) {
    return sigliToTokens.containsKey(sigil);
  }

  @Override
  public INormalizedToken getToken(final String sigil) {
    return sigliToTokens.get(sigil);
  }

}
