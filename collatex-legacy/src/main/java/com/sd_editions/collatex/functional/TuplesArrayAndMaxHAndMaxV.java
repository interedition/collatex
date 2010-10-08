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

package com.sd_editions.collatex.functional;

import java.util.Arrays;
import java.util.Collections;

import com.sd_editions.collatex.Collate.Tuple;

public class TuplesArrayAndMaxHAndMaxV {
  private Tuple[] tuples;
  private int maxH, maxV;

  public TuplesArrayAndMaxHAndMaxV(int anz) {
    this.tuples = new Tuple[anz];
  }

  public int getMaxH() {
    return maxH;
  }

  public int getMaxV() {
    return maxV;
  }

  public Tuple[] getTuplesArray() {
    return tuples;
  }

  public void set(int i, Tuple next) {
    tuples[i] = next;
    if (next.baseIndex > maxH) {
      maxH = next.baseIndex;
    }
    if (next.witnessIndex > maxV) {
      maxV = next.witnessIndex;
    }
  }

  public void sort() {
    Collections.sort(Arrays.asList(tuples));
  }

}
