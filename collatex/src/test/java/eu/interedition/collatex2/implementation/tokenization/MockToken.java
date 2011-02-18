package eu.interedition.collatex2.implementation.tokenization;

import eu.interedition.collatex2.interfaces.IToken;

public class MockToken implements IToken {
  private final String content;

  public MockToken(String content) {
    this.content = content;
  }

  @Override
  public String getContent() {
    return content;
  }
}
