package eu.interedition.collatex2.implementation.input;

import java.util.List;

import org.mortbay.log.Log;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;

public class Phrase implements IPhrase {
  private final List<INormalizedToken> tokens;

  public Phrase(final List<INormalizedToken> tokens) {
    this.tokens = tokens;
  }

  //  // TODO: rename parameter "remove" to bigram
  //  public static Phrase create(final BiGram remove) {
  //    final List<NormalizedToken> tokens = Lists.newArrayList(remove.getFirstToken(), remove.getLastToken());
  //    return new Phrase(tokens);
  //  }

  //  public static Phrase create(final IWitness aa, final int startPosition, final int endPosition) {
  //    final List<NormalizedToken> tokens2 = aa.getTokens(startPosition, endPosition);
  //    return new Phrase(tokens2);
  //  }

  //  // Note: not too pleased with this method! Not immutable!
  //  public void add(final BiGram nextBiGram) {
  //    tokens.add(nextBiGram.getLastToken());
  //  }

  public String getNormalized() {
    String replacementString = "";
    String divider = "";
    for (final INormalizedToken token : tokens) {
      replacementString += divider + token.getNormalized();
      divider = " ";
    }
    return replacementString;

  }

  // TODO: add test for defensive behavior!
  public INormalizedToken getFirstToken() {
    if (isEmpty()) {
      throw new RuntimeException("This ngram is empty!");
    }
    return tokens.get(0);
  }

  //TODO: make defensive and add test!
  public INormalizedToken getLastToken() {
    return tokens.get(tokens.size() - 1);
  }

  public boolean isEmpty() {
    return tokens.isEmpty();
  }

  public Phrase trim() {
    final List<INormalizedToken> subList = tokens.subList(1, tokens.size() - 1);
    return new Phrase(subList);
  }

  public static Phrase create(final INormalizedToken token) {
    return new Phrase(Lists.newArrayList(token));
  }

  @Override
  public int getBeginPosition() {
    return getFirstToken().getPosition();
  }

  @Override
  public int getEndPosition() {
    return getLastToken().getPosition();
  }

  @Override
  public String toString() {
    if (isEmpty()) {
      return "<empty>";
    }
    return getNormalized() + ":" + getBeginPosition() + ":" + getEndPosition();
  }

  @Override
  public String getSigil() {
    return getFirstToken().getSigil();
  }

  @Override
  public List<INormalizedToken> getTokens() {
    return tokens;
  }

  @Override
  public boolean equals(final Object obj) {
    Log.info("Phrase.equals() called");
    if (!(obj instanceof Phrase)) {
      return false;
    }
    //    return tokens.equals(((Phrase) obj).getTokens());
    return toString().equals(((Phrase) obj).toString());
  }
}
