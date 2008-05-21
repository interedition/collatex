package com.sd_editions.collatex.match_spike;

public class WordCoordinate {
  int witnessNumber;
  int positionInWitness;

  public WordCoordinate(int witnessIndex, int wordIndex) {
    witnessNumber = witnessIndex;
    positionInWitness = wordIndex;
  }

  public int getWitnessNumber() {
    return witnessNumber;
  }

  public int getPositionInWitness() {
    return positionInWitness;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof WordCoordinate)) return false;
    WordCoordinate otherWordCoordinate = (WordCoordinate) obj;
    return (getWitnessNumber() == otherWordCoordinate.getWitnessNumber() && getPositionInWitness() == otherWordCoordinate.getPositionInWitness());
  }

  @Override
  public String toString() {
    return "[" + witnessNumber + "," + positionInWitness + "]";
  }
}
