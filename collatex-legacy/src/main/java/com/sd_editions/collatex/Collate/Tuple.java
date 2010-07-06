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

package com.sd_editions.collatex.Collate;

public class Tuple implements Comparable<Tuple> {
  public int baseIndex;
  public int witnessIndex;
  private boolean isTransposTupel;

  public Tuple(int baseIndex1, int witnessIndex1) {
    this.baseIndex = baseIndex1;
    this.witnessIndex = witnessIndex1;
    this.isTransposTupel = false;
  }

  public boolean isTransposTupel() {
    return isTransposTupel;
  }

  public void markAsTransposTupel() {
    this.isTransposTupel = true;
  }

  public int getBaseIndex() {
    return baseIndex;
  }

  public int getWitnessIndex() {
    return witnessIndex;
  }

  public int compareTo(Tuple tmp) {
    int newer = Math.abs(tmp.baseIndex - tmp.witnessIndex);
    int me = Math.abs(this.baseIndex - this.witnessIndex);
    if (me < newer) {
      return -1;
    } else if (me > newer) {
      return 1;
    } else if (me == newer) {
      int newer2 = tmp.witnessIndex;
      int me2 = this.witnessIndex;
      if (me2 == newer2) {
        return 0;
      } else if (me2 < newer2) {
        return -1;
      } else if (me2 > newer2) {
        return 1;

      }
    }
    return 0;
  }

  @Override
  public String toString() {
    return "[" + baseIndex + "," + witnessIndex + "]";
  }
}
