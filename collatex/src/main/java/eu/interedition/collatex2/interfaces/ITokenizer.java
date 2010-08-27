package eu.interedition.collatex2.interfaces;

public interface ITokenizer {

  Iterable<IToken> tokenize(String sigle, String content);
}
