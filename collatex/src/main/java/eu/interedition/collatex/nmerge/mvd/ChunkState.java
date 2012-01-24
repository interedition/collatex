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
 * The state of Chunks
 *
 * @author Desmond Schmidt 11/11/07 revised 21/5/09
 */
public enum ChunkState {
  /**
   * default state as e.g. background text after find
   */
  NONE,
  /**
   * merged state after compare for shared text
   */
  MERGED,
  /**
   * text of first version after compare
   */
  DELETED,
  /**
   * text of second version after compare
   */
  ADDED,
  /**
   * text found by search
   */
  FOUND,
  /**
   * parent of transposition
   */
  PARENT,
  /**
   * child of transposition
   */
  CHILD;
}
