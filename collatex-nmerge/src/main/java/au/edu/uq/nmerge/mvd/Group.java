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

import java.io.UnsupportedEncodingException;

/**
 * A Group is a name for a group of Versions or other Groups.
 */
public class Group {
  /**
   * required for serializable classes
   */
  static final long serialVersionUID = 1;
  /**
   * id for the "top level" group - the notional mother of all groups
   */
  static final short TOP_LEVEL = 0;
  /**
   * parent group id
   */
  short parent;
  /**
   * name of group
   */
  public String name;
  /**
   * size of group in bytes for serialization
   */
  int groupSize;
  /**
   * transient open property
   */
  private boolean open;

  /**
   * Construct a Group.
   *
   * @param parent can be 0 which means a top-level group
   * @param name   name of group for display
   */
  public Group(short parent, String name) {
    this.name = name;
    this.parent = parent;
  }

  /**
   * Return the size of this Group object for serialization
   *
   * @return the size in bytes
   */
  int dataSize() throws UnsupportedEncodingException {
    if (groupSize == 0) {
      byte[] bytes = name.getBytes("UTF-8");
      groupSize = 2 + 2 + bytes.length;
    }
    return groupSize;
  }

  /**
   * Get the parent of this Group. 0 means no parent
   *
   * @return the parent
   */
  public short getParent() {
    return parent;
  }

  /**
   * Set the parent of this Group. 0 means no parent
   *
   * @param parent the new parent id = index+1
   */
  public void setParent(short parent) {
    this.parent = parent;
  }

  /**
   * Get the transient open property
   *
   * @return the current open value
   */
  public boolean isOpen() {
    return open;
  }

  /**
   * Set the transient open property
   *
   * @param open the new value of open
   */
  public void setOpen(boolean open) {
    this.open = open;
  }

  /**
   * Set the group's name
   *
   * @param name the new name for the group
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Convert a Group to a string for debugging
   *
   * @return a human-readable string Group
   */
  public String toString() {
    return "name:" + name + ";parent:" + parent + ";open:" + open;
  }
}
