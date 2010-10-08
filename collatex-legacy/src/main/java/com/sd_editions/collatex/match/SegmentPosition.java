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

public class SegmentPosition {

  public final String witnessId;
  public final Integer position;

  public SegmentPosition(String witnessId1, Integer position1) {
    this.witnessId = witnessId1;
    this.position = position1;
  }

  @Override
  public String toString() {
    return witnessId + ":" + position;
  }

  @SuppressWarnings("boxing")
  public SegmentPosition nextSegmentPosition() {
    return new SegmentPosition(witnessId, position + 1);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SegmentPosition)) return false;
    SegmentPosition segmentPosition = (SegmentPosition) obj;
    return segmentPosition.witnessId.equals(this.witnessId) && segmentPosition.position.equals(this.position);
  }

  @Override
  public int hashCode() {
    return 10 * witnessId.hashCode() + position.hashCode();
  }
}
