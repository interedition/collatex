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

import java.io.Serializable;
import java.util.Comparator;

public class TupleComparatorBI implements Comparator<Tuple>, Serializable {
  private static final long serialVersionUID = 2724931576152430838L;

  public int compare(Tuple tupA, Tuple tupB) {
    double valTup_A = transformTuple(tupA);
    double valTup_B = transformTuple(tupB);
    if (valTup_A < valTup_B) return -1;
    if (valTup_A > valTup_B) return 1;

    return 0;
  }

  @SuppressWarnings("boxing")
  private double transformTuple(Tuple r) {
    Double str1 = new Double(Integer.toString(r.getBaseIndex()));
    Double str2 = new Double(Integer.toString(r.getWitnessIndex()));

    return str1 + (str2 * 0.1);
  }
}
