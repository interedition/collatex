package eu.interedition.collatex2.input;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;

public class Phrase implements IPhrase {
  private final List<INormalizedToken> tokens;

  public static final Comparator<IPhrase> PHRASECOMPARATOR = new Comparator<IPhrase>() {
    @Override
    public int compare(final IPhrase p1, final IPhrase p2) {
      return p1.compareTo(p2);
    }
  };

  public Phrase(final List<INormalizedToken> tokens1) {
    this.tokens = tokens1;
  }

  //  // TODO rename parameter "remove" to bigram
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
    final StringBuilder normalized = new StringBuilder();
    String divider = "";
    for (final INormalizedToken token : tokens) {
      normalized.append(divider).append(token.getNormalized());
      divider = " ";
    }
    return normalized.toString();

  }

  //TODO: add escaping!
  @Override
  public String getContent() {
    final StringBuilder content = new StringBuilder();
    String divider = "";
    for (final INormalizedToken token : tokens) {
      content.append(divider).append(token.getContent());
      divider = " ";
    }
    return content.toString();
  }

  // TODO add test for defensive behavior!
  public INormalizedToken getFirstToken() {
    if (isEmpty()) {
      throw new RuntimeException("This ngram is empty!");
    }
    return tokens.get(0);
  }

  //TODO make defensive and add test!
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
    return getContent(); /*getNormalized() + ":" + getBeginPosition() + ":" + getEndPosition();*/
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
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Phrase)) {
      return false;
    }
    return tokens.equals(((Phrase) obj).getTokens());
  }

  @Override
  public int size() {
    return tokens.size();
  }

  @Override
  public int compareTo(final IPhrase other) {
    final int beginDelta = getBeginPosition() - other.getBeginPosition();
    if (beginDelta != 0) {
      return beginDelta;
    }
    final int endDelta = getEndPosition() - other.getEndPosition();
    if (endDelta != 0) {
      return endDelta;
    }
    final int sizeDelta = getTokens().size() - other.getTokens().size();
    return sizeDelta;
  }

  @Override
  public IPhrase createSubPhrase(final int startIndex, final int endIndex) {
    return new Phrase(tokens.subList(startIndex - 1, endIndex));
  }

  @Override
  public void addTokenToRight(final INormalizedToken token) {
    tokens.add(token);
  }

  @Override
  public void addTokenToLeft(final INormalizedToken token) {
    tokens.add(0, token);
  }

}
