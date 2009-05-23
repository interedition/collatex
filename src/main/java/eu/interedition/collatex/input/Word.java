package eu.interedition.collatex.input;

public class Word {
  private final String witnessId;
  public final String original;
  public final String normalized;
  public final int position;

  public Word(String _witnessId, String _original, int _position) {
    if (_original.isEmpty()) throw new IllegalArgumentException("Word cannot be empty!");
    this.witnessId = _witnessId;
    this.original = _original;
    this.normalized = original.toLowerCase().replaceAll("[`~'!@#$%^&*():;,\\.]", "");
    this.position = _position;
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

}
