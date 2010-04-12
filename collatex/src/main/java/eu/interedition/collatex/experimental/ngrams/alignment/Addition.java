package eu.interedition.collatex.experimental.ngrams.alignment;


import eu.interedition.collatex.experimental.ngrams.NGram;

public class Addition extends Modification {
  private final NGram addition;
  private final NGram nextMatchA;

  public Addition(final NGram nextMatchA, final NGram addition) {
    this.nextMatchA = nextMatchA;
    this.addition = addition;
  }

  public int getPosition() {
    if (nextMatchA == null || nextMatchA.isEmpty()) {
      throw new RuntimeException("There is no next match!");
    }
    return nextMatchA.getFirstToken().getPosition();
  }

  public NGram getAddedWords() {
    return addition;
  }

  @Override
  public String toString() {
    // TODO should not be get Normalized?
    String result = "addition: " + addition.getNormalized();
    // TODO I would like to have only 
    if (nextMatchA == null || nextMatchA.isEmpty()) {
      result += " position: at the end";
    } else {
      result += " position: " + getPosition();
    }
    return result;
  }

  @Override
  public void accept(final ModificationVisitor modificationVisitor) {
    modificationVisitor.visitAddition(this);
  }

}
