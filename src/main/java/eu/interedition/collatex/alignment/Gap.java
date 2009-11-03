package eu.interedition.collatex.alignment;

import com.sd_editions.collatex.permutations.collate.Addition;
import com.sd_editions.collatex.permutations.collate.Omission;
import com.sd_editions.collatex.permutations.collate.Replacement;

import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.visualization.Modification;

public class Gap {
  final Phrase phraseA;
  final Phrase phraseB;
  final Match next;

  public Gap(Phrase _phraseA, Phrase _phraseB, Match _next) {
    this.phraseA = _phraseA;
    this.phraseB = _phraseB;
    this.next = _next;
  }

  public Phrase getPhraseA() {
    return phraseA;
  }

  public Phrase getPhraseB() {
    return phraseB;
  }

  public Addition createAddition() {
    return new Addition(phraseA.getStartPosition(), phraseB);
  }

  public Omission createOmission() {
    return new Omission(phraseA);
  }

  public Replacement createReplacement() {
    return new Replacement(phraseA, phraseB);
  }

  public boolean isAddition() {
    return !phraseA.hasGap() && phraseB.hasGap();
  }

  public boolean isOmission() {
    return phraseA.hasGap() && !phraseB.hasGap();
  }

  public boolean isReplacement() {
    return phraseA.hasGap() && phraseB.hasGap();
  }

  public boolean isValid() {
    return phraseA.hasGap() || phraseB.hasGap();
  }

  @Override
  public String toString() {
    String result = "NonMatch: addition: " + isAddition() + " base: " + phraseA;
    if (phraseA.isAtTheEnd()) {
      result += "; nextWord: none";
    } else {
      result += "; nextWord: " + phraseA.getNextWord();
    }
    result += "; witness: " + phraseB;
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
  public Match getNextMatch() {
    if (next == null) {
      throw new RuntimeException("There is no next match!");
    }
    return next;
  }

}
