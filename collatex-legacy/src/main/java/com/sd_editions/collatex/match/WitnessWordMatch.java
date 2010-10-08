/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sd_editions.collatex.match;

public abstract class WitnessWordMatch implements Comparable<WitnessWordMatch> {
  int positionInWitness;
  //  int type;
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
