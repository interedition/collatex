/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.dekker.matrix;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A DirectedIsland is a collections of Coordinates all on the same
 * diagonal. The direction of this diagonal can be -1, 0, or 1.
 * The zero is for a DirectedIsland of only one Coordinate.
 * Directions 1 and -1 examples
 * Coordinates (0,0) (1,1) have Direction 1
 * Coordinates (1,1) (2,1) have Direction -1
 * I.e. if the row-coordinate gets larger and the col-coordinate also, the
 * direction is 1 (positive) else it is -1 (negative)
 */
public class Island implements Iterable<Coordinate>, Comparable<Island> {

  private int direction = 0;
  private final List<Coordinate> islandCoordinates = new ArrayList<>();

  public Island() {}

  public Island(Island other) {
    for (Coordinate c : other.islandCoordinates) {
      add(new Coordinate(c));
    }
  }

  public Island(Coordinate first, Coordinate last) {
    add(first);
    Coordinate newCoordinate = first;
    while (!newCoordinate.equals(last)) {
      newCoordinate = new Coordinate(newCoordinate.getRow() + 1, newCoordinate.getColumn() + 1);
      //        LOG.debug("{}", newCoordinate);
      add(newCoordinate);
    }
  }

  public boolean add(Coordinate coordinate) {
    boolean result = false;
    if (islandCoordinates.isEmpty()) {
      result = islandCoordinates.add(coordinate);
    } else if (!contains(coordinate) && neighbour(coordinate)) {
      if (direction == 0) {
        Coordinate existing = islandCoordinates.get(0);
        direction = (existing.row - coordinate.row) / (existing.column - coordinate.column);
        result = islandCoordinates.add(coordinate);
      } else {
        Coordinate existing = islandCoordinates.get(0);
        if (existing.column != coordinate.column) {
          int new_direction = (existing.row - coordinate.row) / (existing.column - coordinate.column);
          if (new_direction == direction) result = islandCoordinates.add(coordinate);
        }
      }
    }
    return result;
  }

  public int direction() {
    return direction;
  }

  public Island removePoints(Island di) {
    Island result = new Island(this);
    for (Coordinate c : di) {
      result.removeSameColOrRow(c);
    }
    return result;
  }

  public void removeCoordinate(Coordinate c) {
    islandCoordinates.remove(c);
  }

  public Coordinate getCoorOnRow(int row) {
    for (Coordinate coor : islandCoordinates) {
      if (coor.getRow() == row) return coor;
    }
    return null;
  }

  public Coordinate getCoorOnCol(int col) {
    for (Coordinate coor : islandCoordinates) {
      if (coor.getColumn() == col) return coor;
    }
    return null;
  }

  public void merge(Island di) {
    for (Coordinate c : di) {
      add(c);
    }
  }

  /**
   * Two islands are competitors if there is a horizontal or
   * vertical line which goes through both islands
   */
  public boolean isCompetitor(Island isl) {
    for (Coordinate c : isl) {
      for (Coordinate d : islandCoordinates) {
        if (c.sameColumn(d) || c.sameRow(d)) return true;
      }
    }
    return false;
  }

  public boolean contains(Coordinate c) {
    return islandCoordinates.contains(c);
  }

  public boolean neighbour(Coordinate c) {
    if (contains(c)) return false;
    for (Coordinate islC : islandCoordinates) {
      if (c.bordersOn(islC)) {
        return true;
      }
    }
    return false;
  }

  public Coordinate getLeftEnd() {
    Coordinate coor = islandCoordinates.get(0);
    for (Coordinate c : islandCoordinates) {
      if (c.column < coor.column) coor = c;
    }
    return coor;
  }

  public Coordinate getRightEnd() {
    Coordinate coor = islandCoordinates.get(0);
    for (Coordinate c : islandCoordinates) {
      if (c.column > coor.column) coor = c;
    }
    return coor;
  }

  public boolean overlap(Island isl) {
    for (Coordinate c : isl) {
      if (contains(c) || neighbour(c)) return true;
    }
    return false;
  }

  public int size() {
    return islandCoordinates.size();
  }

  public void clear() {
    islandCoordinates.clear();
  }

  public int value() {
    final int size = size();
    return (size < 2 ? size : direction + size * size);
  }

  protected boolean removeSameColOrRow(Coordinate c) {
    ArrayList<Coordinate> remove = new ArrayList<>();
    for (Coordinate coor : islandCoordinates) {
      if (coor.sameColumn(c) || coor.sameRow(c)) {
        remove.add(coor);
      }
    }
    if (remove.isEmpty()) return false;
    for (Coordinate coor : remove) {
      islandCoordinates.remove(coor);
    }
    return true;
  }

  @Override
  public Iterator<Coordinate> iterator() {
    return Collections.unmodifiableList(islandCoordinates).iterator();
  }

  @Override
  public int hashCode() {
    return islandCoordinates.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;

    if (!obj.getClass().equals(Island.class)) return false;

    Island isl = (Island) obj;
    if (isl.size() != size()) return false;

    boolean result = true;
    for (Coordinate c : isl) {
      result &= this.contains(c);
    }
    return result;
  }

  @Override
  public String toString() {
    if (islandCoordinates.isEmpty()) {
      throw new RuntimeException("Unexpected situation: island coordinates empty!");
    }
    return MessageFormat.format("Island ({0}-{1}) size: {2} direction: {3}", islandCoordinates.get(0), islandCoordinates.get(islandCoordinates.size() - 1), size(), direction());
  }

  @Override
  public int compareTo(Island i) {
    return this.getLeftEnd().compareTo(i.getLeftEnd());
  }
}
