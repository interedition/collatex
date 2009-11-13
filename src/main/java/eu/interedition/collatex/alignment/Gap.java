package eu.interedition.collatex.alignment;

import com.sd_editions.collatex.permutations.collate.Addition;
import com.sd_editions.collatex.permutations.collate.Omission;
import com.sd_editions.collatex.permutations.collate.Replacement;

import eu.interedition.collatex.input.BaseContainerPart;
import eu.interedition.collatex.input.BaseElement;
import eu.interedition.collatex.visualization.Modification;

public class Gap<T extends BaseElement> {
  final BaseContainerPart<T> _partA;
  final BaseContainerPart<T> _partB;
  final Match<T> next;

  public Gap(final BaseContainerPart<T> partA, final BaseContainerPart<T> partB, final Match<T> _next) {
    this._partA = partA;
    this._partB = partB;
    this.next = _next;
  }

  //TODO: rename method to getPartA
  public BaseContainerPart<T> getPhraseA() {
    return _partA;
  }

  //TODO: rename method to getPartB
  public BaseContainerPart<T> getPhraseB() {
    return _partB;
  }

  public Addition createAddition() {
    return new Addition(_partA.getBeginPosition(), _partB);
  }

  public Omission createOmission() {
    return new Omission(_partA);
  }

  public Replacement createReplacement() {
    return new Replacement(_partA, _partB);
  }

  public boolean isAddition() {
    return !_partA.hasGap() && _partB.hasGap();
  }

  public boolean isOmission() {
    return _partA.hasGap() && !_partB.hasGap();
  }

  public boolean isReplacement() {
    return _partA.hasGap() && _partB.hasGap();
  }

  public boolean isValid() {
    return _partA.hasGap() || _partB.hasGap();
  }

  @Override
  public String toString() {
    String result = "NonMatch: addition: " + isAddition() + " base: " + _partA;
    if (isAtTheEnd()) {
      result += "; nextWord: none";
    } else {
      result += "; nextWord: " + getNextMatch().getBaseWord();
    }
    result += "; witness: " + _partB;
    return result;
  }

  public Modification analyse() {
    if (isAddition()) {
      return createAddition();
    }
    if (isOmission()) {
      return createOmission();
    }
    if (isReplacement()) {
      return createReplacement();
    }
    throw new RuntimeException("Not a modification!");
  }

  // Note: this the next match after the gap for the second witness!
  public Match<T> getNextMatch() {
    if (next == null) {
      throw new RuntimeException("There is no next match!");
    }
    return next;
  }

  public boolean isAtTheEnd() {
    return next == null;
  }

}
