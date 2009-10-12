package eu.interedition.collatex.input;

import eu.interedition.collatex.input.visitors.ICollationResource;
import eu.interedition.collatex.input.visitors.IResourceVisitor;
import eu.interedition.collatex.tokenization.Token;

public class Word implements ICollationResource {
  private final String witnessId;
  public final String original;
  public final String normalized;
  public final int position;

  // TODO: add punctuation!!
  public Word(String _witnessId, String _original, int _position) {
    if (_original.isEmpty()) throw new IllegalArgumentException("Word cannot be empty!");
    this.witnessId = _witnessId;
    this.original = _original;
    this.normalized = original.toLowerCase().replaceAll("[`~'!@#$%^&*():;,\\.]", "");
    this.position = _position;
  }

  // TODO: notice the duplication here!
  // TODO: store punctuation!
  // TODO: extract regularization!
  public Word(String _witnessId, Token nextToken, int _position) {
    this.witnessId = _witnessId;
    this.original = nextToken.getText();
    this.position = _position;
    this.normalized = original.toLowerCase().replaceAll("[`~'!@#$%^&*():;,\\.]", "");
  }

  @Override
  public String toString() {
    return original;
  }

  @Override
  public int hashCode() {
    return original.hashCode() * (10 ^ position);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Word)) return false;
    Word other = (Word) obj;
    return other.original.equals(this.original) && (other.position == this.position);
  }

  public String getWitnessId() {
    return witnessId;
  }

  @Override
  public void accept(IResourceVisitor visitor) {
    visitor.visitWord(this);
  }

}
