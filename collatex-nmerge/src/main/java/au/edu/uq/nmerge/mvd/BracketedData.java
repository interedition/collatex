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

import java.util.List;

/**
 * Represent an object output by the MvdTool as data
 * enclosed in [..] and using a ':' to separate the
 * header (after the '[') from the data parts
 *
 * @author Desmond Schmidt 2/6/09
 */
public abstract class BracketedData<T> {
  /**
   * the data of the chunk
   */
  protected List<T> realData;

  /**
   * length of data parsed to produce this object
   */
  protected int srcLen;

  /**
   * Create a BracketedData object
   */
  public BracketedData() {
  }

  /**
   * Create a BracketedData object using the data
   *
   * @param data     the original data
   */
  public BracketedData(List<T> data) {
    this.realData = data;
  }

  /**
   * Add some data to the chunk.
   *
   * @param bytes the new bytes to add to data
   */
  public void addData(List<T> bytes) {
    if (bytes != null && !bytes.isEmpty()) {
      realData.addAll(bytes);
    }
  }

  /**
   * Get the read unescaped data
   *
   * @return a byte array of raw data
   */
  public List<T> getData() {
    return realData;
  }
}