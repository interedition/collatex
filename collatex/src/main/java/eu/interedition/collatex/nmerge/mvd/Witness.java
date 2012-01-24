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
package eu.interedition.collatex.nmerge.mvd;

/**
 * Definition of a version. This is not the same as the versions of
 * a Pair - that records WHICH versions the pair belongs to. This
 * class records what a particular Version is.
 */
public class Witness {
  /**
   * siglum or other short name e.g. A
   */
  public String siglum;

  /**
   * Create an instance of Version
   *
   * @param siglum siglum or other short name
   */
  public Witness(String siglum) {
    this.siglum = siglum;
  }

  public String getSiglum() {
    return siglum;
  }

  /**
   * Convert a Version to a string for debugging
   *
   * @return a human-readable string Version
   */
  public String toString() {
    return siglum;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Witness) {
      return siglum.equals(((Witness) obj).siglum);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return siglum.hashCode();
  }
}
