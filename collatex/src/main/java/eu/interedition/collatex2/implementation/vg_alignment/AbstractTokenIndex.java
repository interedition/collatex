package eu.interedition.collatex2.implementation.vg_alignment;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.input.NullToken;
import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.ITokenIndex;

//TODO: remove the explicit usage of NullToken in this class and in TokenIndexMatcher class!
public abstract class AbstractTokenIndex implements ITokenIndex {
  private final Map<String, List<INormalizedToken>> normalizedToTokens;

  protected AbstractTokenIndex() {
    normalizedToTokens = Maps.newLinkedHashMap();
  }

  protected void processTokens(List<INormalizedToken> tokens, List<String> repeatingTokens) {      
    int position=0; //NOTE: position => index in path
    for (INormalizedToken token : tokens) {
      makeTokenUniqueIfneeded(token, tokens, position, repeatingTokens);
      position++;
    }
  }
  
  @Override
  public boolean contains(String normalized) {
    return normalizedToTokens.containsKey(normalized);
  }

  //TODO: this is workaround! store real phrases instead of token!
  @Override
  public IPhrase getPhrase(String normalized) {
    if (!contains(normalized)) {
      throw new RuntimeException("Item does not exist!");
    }
    Collection<INormalizedToken> tokens = normalizedToTokens.get(normalized);
    return new Phrase(Lists.newArrayList(tokens));
  }

  @Override
  public int size() {
    return normalizedToTokens.size();
  }

  @Override
  public Set<String> keys() {
    return normalizedToTokens.keySet();
  }

  //NOTE: Remove index parameter?
  private void makeTokenUniqueIfneeded(INormalizedToken token, List<INormalizedToken> tokens, int position, List<String> repeatingTokens) {
    // System.out.println("Trying "+token.getNormalized());
    String normalized = token.getNormalized();
    // check uniqueness
    final boolean unique = !repeatingTokens.contains(normalized);
    if (unique) {
      List<INormalizedToken> tempList = Lists.newArrayList(token);
      addAll(tempList); //TODO: extract separate add method with single vertex parameter!
    } else {
      final List<INormalizedToken> leftTokens = findUniqueTokensToTheLeft(tokens, repeatingTokens, position);
      final List<INormalizedToken> rightTokens = findUniqueTokensToTheRight(tokens, repeatingTokens, position);
      addAll(leftTokens);
      addAll(rightTokens);
    }
  }

  
  //NOTE: I need an index to move to the left and right here!
  //NOTE: or an iterator!
  private List<INormalizedToken> findUniqueTokensToTheLeft(List<INormalizedToken> path, List<String> repeatingTokens, int position) {
    List<INormalizedToken> tokens = Lists.newArrayList();
    boolean found = false; // not nice!
    for (int i = position ; !found && i > -1; i-- ) {
      INormalizedToken leftToken = path.get(i);
      String normalizedNeighbour = leftToken.getNormalized();
      found = !repeatingTokens.contains(normalizedNeighbour);
      tokens.add(0, leftToken);
    }
    if (!found) {
      tokens.add(0, new NullToken());
    }
    return tokens;
  }
  
  //NOTE: I need an index to move to the left and right here!
  //NOTE: or an iterator!
  private List<INormalizedToken> findUniqueTokensToTheRight(List<INormalizedToken> path, List<String> repeatingTokens, int position) {
    List<INormalizedToken> tokens = Lists.newArrayList();
    boolean found = false; // not nice!
    for (int i = position ; !found && i < path.size(); i++ ) {
      INormalizedToken rightToken = path.get(i);
      String normalizedNeighbour = rightToken.getNormalized();
      found = !repeatingTokens.contains(normalizedNeighbour);
      tokens.add(rightToken);
    }
    if (!found) {
      tokens.add(new NullToken());
    }
    return tokens;
  }

  private void addAll(List<INormalizedToken> tokens) {
    StringBuilder normalized = new StringBuilder();
    String splitter = "";
    for (INormalizedToken token : tokens) {
      normalized.append(splitter).append(token.getNormalized());
      splitter = " ";
    }
    normalizedToTokens.put(normalized.toString(), tokens);
  }

}
