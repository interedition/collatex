/*
 * NMerge is Copyright 2009-2011 Desmond Schmidt
 *
 * This file is part of NMerge. NMerge is a Java library for merging
 * multiple versions into multi-version documents (MVDs), and for
 * reading, searching and comparing them.
 *
 * NMerge is free software: you can redistribute it and/or modify
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
package au.edu.uq.nmerge.graph;

import au.edu.uq.nmerge.mvd.Witness;
import com.google.common.collect.Ordering;

import java.util.List;
import java.util.Set;

/**
 * A form of unaligned arc that also stores its position from the start,
 * and its MUM.
 *
 * @author Desmond Schmidt 12/11/08
 */
public class VariantGraphSpecialArc<T> extends VariantGraphArc<T> implements Comparable<VariantGraphSpecialArc<T>> {
  /**
   * FIXME: Introduce token order!
   *
   * @deprecated
   */
  private static final Ordering<Object> ARBITRARY_TOKEN_ORDERING = Ordering.arbitrary();
  /**
   * if previously calculated save best MUM here
   */
  MaximalUniqueMatch best;
  /**
   * position from the start of the new version
   */
  int position;

  /**
   * Override the Arc constructor
   *
   * @param versions the versions of the arc
   * @param data     the data of the special arc
   * @param position the position of the arc along the new version
   */
  public VariantGraphSpecialArc(Set<Witness> versions, List<T> data, int position) {
    super(versions, data);
    this.position = position;
    // leave best null
  }

  /**
   * Get the best LCS or null
   *
   * @return null (and so you must calculate it) or an LCS
   */
  public MaximalUniqueMatch getBest() {
    return best;
  }

  /**
   * Set the best MUM
   *
   * @param best precalculated MUM or this arc
   */
  public void setBest(MaximalUniqueMatch best) {
    this.best = best;
  }

  /**
   * Reset best to null so it will be recalculate when required
   */
  public void reset() {
    best = null;
  }

  /**
   * Required to equate keys in the treemap: otherwise we
   * get duplicates
   */
  public boolean equals(Object other) {
    if (!(other instanceof VariantGraphSpecialArc))
      return false;
    else {
      VariantGraphSpecialArc otherArc = (VariantGraphSpecialArc) other;
      boolean result = super.equals(other) && position == otherArc.position;
      //if ( result )
      //	System.out.println("equals!");
      return result;
    }
  }

  public String toString() {
    String matchStr = (best != null) ? best.getMatch().toString() : "";
    return super.toString() + " Match: " + matchStr;
  }

  /**
   * This is used in TreeMap to order the keys. We sort first on MUM
   * values and then on alphabetical byte values. Order is reversed so
   * that the longest MUM will be at the top of the queue not the
   * smallest.
   *
   * @param o the special arc to compare to
   * @return 0 if equal, -1 if we are greater, 1 if less (for reverse
   *         ordering)
   */
  @Override
  public int compareTo(VariantGraphSpecialArc<T> o) {
    int oneLen = this.dataLen();
    int twoLen = o.dataLen();
    int mumValue = (this.best != null) ? this.best.compareTo(o.best) : 0;
    if (mumValue == 0) {
      // MUMs equal: compare the data
      for (int i = 0; i < oneLen && i < twoLen; i++) {
        final int result = ARBITRARY_TOKEN_ORDERING.compare(this.getData().get(i), o.data.get(i));
        if (result != 0) {
          return result;
        }
      }
      if (oneLen < twoLen)
        return 1;
      else if (oneLen > twoLen)
        return -1;
      else if (this.equals(o))
        return 0;
        // data equal: compare the from nodes and to nodes
      else if (this.from != null) {
        if (o.from != null) {
          if (this.from.nodeId > o.from.nodeId)
            return -1;
          else if (this.from.nodeId != o.from.nodeId)
            return 1;
            // from nodes equal, try to nodes
          else if (this.to != null) {
            if (o.to != null) {
              if (this.to.nodeId > o.to.nodeId)
                return -1;
              else if (this.to.nodeId < o.to.nodeId)
                return 1;
              else
                return 0;
            } else
              return -1;
          } else if (o.to != null)
            return 1;
          else
            return 0;
        } else
          return -1;
      } else if (o.from != null)
        return 1;
      else
        return 0;
    }
    return mumValue;
  }
}
