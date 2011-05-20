package eu.interedition.collatex2.experimental;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.input.NullToken;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class MyNewWitnessIndexer {
  private final List<ITokenSequence> tokenSequences;

  public MyNewWitnessIndexer() {
    tokenSequences = Lists.newArrayList();
  }

  public IWitnessIndex index(IWitness witness, IMatchResult result) {
    processTokens(witness.getTokens(), result);
//    // TODO: REMOVE THIS PART! REMOVE THIS PART!
//    // here we try to do the mapping
//    // we lopen alle woorden uit de witness af
//    // daarna kijken we in de matches map
//    // drie mogelijkheden... geen match, enkele match, multiple match
//    // we skippen de woorden met geen of een enkele match
//    INormalizedToken previous =  new StartToken();
//    for (INormalizedToken token : witness.getTokens()) {
//      if (result.getUnsureTokens().contains(token)) {
//        tokenSequences.add(new TokenSequence(previous, token));
//      }
//      int count = matches.keys().count(token);
//      if (count == 1) {
//        previous = token;
//      }
//    }
    return new NewWitnessIndex(tokenSequences);
  }
  
  protected void processTokens(List<INormalizedToken> tokens, IMatchResult result) {      
    int position=0; //NOTE: position => index in path
    for (INormalizedToken token : tokens) {
      makeTokenUniqueIfneeded(token, tokens, position, result);
      position++;
    }
  }
  
  //NOTE: Remove index parameter?
  private void makeTokenUniqueIfneeded(INormalizedToken token, List<INormalizedToken> tokens, int position, IMatchResult result) {
    // System.out.println("Trying "+token.getNormalized());
    // check uniqueness
    final boolean unique = !result.getUnsureTokens().contains(token);
    if (!unique) {
//      List<INormalizedToken> tempList = Lists.newArrayList(token);
//      addAll(tempList); //TODO: extract separate add method with single vertex parameter!
//    } else {
      final List<INormalizedToken> leftTokens = findUniqueTokensToTheLeft(tokens, result, position);
      final List<INormalizedToken> rightTokens = findUniqueTokensToTheRight(tokens, result, position);
      addAll(leftTokens, true);
      addAll(rightTokens, false);
    }
  }

  //NOTE: I would rather use an index to move to the left and right here!
  //NOTE: or an iterator!
  private List<INormalizedToken> findUniqueTokensToTheLeft(List<INormalizedToken> path, IMatchResult result, int position) {
    List<INormalizedToken> tokens = Lists.newArrayList();
    boolean found = false; // not nice!
    for (int i = position ; !found && i > -1; i-- ) {
      INormalizedToken leftToken = path.get(i);
      if (!result.getUnmatchedTokens().contains(leftToken)) {
        found = !result.getUnsureTokens().contains(leftToken);
        tokens.add(0, leftToken);
      }
    }
    if (!found) {
      tokens.add(0, new StartToken());
    }
    return tokens;
  }
  
  //NOTE: I would rather use an index to move to the left and right here!
  //NOTE: or an iterator!
  private List<INormalizedToken> findUniqueTokensToTheRight(List<INormalizedToken> path, IMatchResult result, int position) {
    List<INormalizedToken> tokens = Lists.newArrayList();
    boolean found = false; // not nice!
    for (int i = position ; !found && i < path.size(); i++ ) {
      INormalizedToken rightToken = path.get(i);
      if (!result.getUnmatchedTokens().contains(rightToken)) {
        found = !result.getUnsureTokens().contains(rightToken);
        tokens.add(rightToken);
      }
    }
    if (!found) {
      tokens.add(new NullToken());
    }
    return tokens;
  }

  private void addAll(List<INormalizedToken> tokens, boolean leftAligned) {
    TokenSequence tokenSequence = new TokenSequence(tokens, leftAligned);
    tokenSequences.add(tokenSequence);
  }

}
