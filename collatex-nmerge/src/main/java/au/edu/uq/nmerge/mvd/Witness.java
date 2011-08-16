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
package au.edu.uq.nmerge.mvd;

/**
 * Definition of a version. This is not the same as the versions of
 * a Pair - that records WHICH versions the pair belongs to. This
 * class records what a particular Version is.
 */
public class Witness {
  /**
   * siglum or other short name e.g. A
   */
  public String shortName;

  /**
   * full description if desired: e.g. Codex Vaticanus 1234
   */
  public String longName;

  /**
   * Create an instance of Version
   *
   * @param shortName siglum or other short name
   * @param longName  longer name if desired
   */
  public Witness(String shortName, String longName) {
    this.shortName = shortName;
    this.longName = longName;
  }

  /**
   * Convert a Version to a string for debugging
   *
   * @return a human-readable string Version
   */
  public String toString() {
    return "shortName:" + shortName + ";longName:" + longName;
  }
}
