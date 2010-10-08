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

  @Override
  public int compareTo(WordCoordinate o) {
    if (this.getWitnessNumber() == o.getWitnessNumber()) {
      return Integer.valueOf(this.getPositionInWitness()).compareTo(Integer.valueOf(o.getPositionInWitness()));
    }
    return Integer.valueOf(this.getWitnessNumber()).compareTo(Integer.valueOf(o.getWitnessNumber()));
  }
}
