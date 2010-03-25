package com.sd_editions.collatex.match;

public abstract class WitnessWordMatch implements Comparable<WitnessWordMatch> {
  int positionInWitness;
  int type;
  protected static int EXACT_MATCH = 1;
  protected static int LEV_MATCH = 2;

  public WitnessWordMatch(int wordIndex) {
    positionInWitness = wordIndex;
  }

  //  public int getPositionInWitness() {
  //    return positionInWitness;
  //  }
  //
  //  @Override
  //  public boolean equals(Object obj) {
  //    if (!(obj instanceof WitnessWordMatch)) return false;
  //    WitnessWordMatch otherWordCoordinate = (WitnessWordMatch) obj;
  //    return (getWitnessNumber() == otherWordCoordinate.getWitnessNumber() && getPositionInWitness() == otherWordCoordinate.getPositionInWitness());
  //  }
  //
  //  private Object getWitnessNumber() {
  //    // TODO Auto-generated method stub
  //    return null;
  //  }
  //
  //  @Override
  //  public String toString() {
  //    return "[" + LETTER[witnessNumber] + "," + (positionInWitness + 1) + "]";
  //  }
  //
  //  public int compareTo(WitnessWordMatch o) {
  //    if (this.getWitnessNumber() == o.getWitnessNumber()) {
  //      return new Integer(this.getPositionInWitness()).compareTo(new Integer(o.getPositionInWitness()));
  //    }
  //    return new Integer(this.getWitnessNumber()).compareTo(new Integer(o.getWitnessNumber()));
  //  }
}
