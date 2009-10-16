package com.sd_editions.collatex.match;

public class WordCoordinate implements Comparable<WordCoordinate> {
  private static final String[] LETTER = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
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
    return "[" + LETTER[witnessNumber] + "," + (positionInWitness + 1) + "]";
  }

  public int compareTo(WordCoordinate o) {
    if (this.getWitnessNumber() == o.getWitnessNumber()) {
      return Integer.valueOf(this.getPositionInWitness()).compareTo(Integer.valueOf(o.getPositionInWitness()));
    }
    return Integer.valueOf(this.getWitnessNumber()).compareTo(Integer.valueOf(o.getWitnessNumber()));
  }
}
