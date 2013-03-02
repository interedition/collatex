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

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Archipelago {
  Logger LOG = Logger.getLogger(Archipelago.class.getName());

  private ArrayList<Island> islands;
  private final Set<Integer> islandvectors = Sets.newHashSet(); // row - column, all islands should have direction 1, so this diff should be the same for all coordinates on the island.

  public Archipelago() {
    setIslands(new ArrayList<Island>());
  }

  public Archipelago(Island isl) {
    setIslands(new ArrayList<Island>());
    getIslands().add(isl);
  }

  public void add(Island island) {
    // islands on the archipelago are sorted on size (large -> small) and direction
    //    for (Island i : getIslands()) {
    //      if (island.size() > i.size()) {
    //        getIslands().add(getIslands().indexOf(i), island);
    //        return;
    //
    //      } else
    //        try {
    //          Island disl = island;
    //          Island di = i;
    //          if (island.size() > i.size() && disl.direction() > di.direction()) {
    //            getIslands().add(getIslands().indexOf(i), island);
    //            return;
    //          }
    //        } catch (Exception e) {}
    //    }
    getIslands().add(island);
    Coordinate leftEnd = island.getLeftEnd();
    islandvectors.add(leftEnd.row - leftEnd.column);
  }

  // this is not a real iterator implementation but it works...
  public ArrayList<Island> iterator() {
    return getIslands();
  }

  public int size() {
    return getIslands().size();
  }

  public void mergeIslands() {
    int i = 0;
    int j = 1;
    int[] rr = new int[size()];
    for (i = 0; i < size(); i++) {
      for (j = i + 1; j < size(); j++) {
        if (getIslands().get(i).overlap(getIslands().get(j))) {
          (getIslands().get(i)).merge(getIslands().get(j));
          getIslands().get(j).clear();
          rr[j] = 1;
        }
      }
    }
    for (i = (rr.length - 1); i > 0; i--) {
      if (rr[i] == 1) getIslands().remove(i);
    }
  }

  public Object numOfConflicts() {
    int result = 0;
    int num = getIslands().size();
    for (int i = 0; i < num; i++)
      for (int j = i + 1; j < num; j++) {
        //				System.out.println("compare "+islands.get(j)+" with "+islands.get(i));				
        if (getIslands().get(j).isCompetitor(getIslands().get(i))) result++;
      }
    return result;
  }

  public Island get(int i) {
    return getIslands().get(i);
  }

  public Archipelago copy() {
    Archipelago result = new Archipelago();
    for (Island isl : getIslands()) {
      result.add(new Island(isl));
    }
    return result;
  }

  public boolean conflictsWith(Island island) {
    for (Island isl : getIslands()) {
      if (isl.isCompetitor(island)) return true;
    }
    return false;
  }

  public int value() {
    int result = 0;
    for (Island isl : getIslands()) {
      result += isl.value();
    }
    return result;
  }

  public ArrayList<Coordinate> findGaps() {
    ArrayList<Coordinate> list = new ArrayList<Coordinate>();
    return findGaps(list);
  }

  public ArrayList<Coordinate> findGaps(Coordinate begin, Coordinate end) {
    ArrayList<Coordinate> list = new ArrayList<Coordinate>();
    list.add(begin);
    list.add(end);
    return findGaps(list);
  }

  public ArrayList<Coordinate> findGaps(ArrayList<Coordinate> list) {
    ArrayList<Coordinate> result = new ArrayList<Coordinate>(list);
    for (Island isl : getIslands()) {
      Coordinate left = isl.getLeftEnd();
      Coordinate right = isl.getRightEnd();
      boolean found = false;
      for (int i = 0; i < result.size(); i++) {
        if (left.column < result.get(i).column || (left.column == result.get(i).column && left.row < result.get(i).row)) {
          result.add(i, right);
          result.add(i, left);
          found = true;
          break;
        }
      }
      if (!found) {
        result.add(left);
        result.add(right);
      }
    }
    result.remove(result.size() - 1);
    result.remove(0);
    return result;
  }

  public boolean containsCoordinate(int row, int column) {
    return Objects.equal(getCoordinatesMap().get(row), column);
  }

  public boolean islandsCompete(Island i1, Island i2) {
    return i1.isCompetitor(i2);
  }

  public void setIslands(ArrayList<Island> islands) {
    this.islands = islands;
  }

  public ArrayList<Island> getIslands() {
    return islands;
  }

  public Island findClosestIsland(Island island1, Island island2) {
    Island closest = null;
    double minimum1 = 10000;
    double minimum2 = 10000;
    for (Island fixedIsland : getIslands()) {
      minimum1 = Math.min(minimum1, distance(island1, fixedIsland));
      minimum2 = Math.min(minimum2, distance(island2, fixedIsland));
    }
    if (minimum1 < minimum2) {
      closest = island1;
    } else if (minimum2 < minimum1) {
      closest = island2;
    } else {
      if (LOG.isLoggable(Level.FINE)) {
        LOG.log(Level.FINE, "{0} -> {1}", new Object[] { island1, island2 });
      }

      throw new RuntimeException("no minimum found, help!");
    }
    return closest;
  }

  protected void remove(int i) {
    getIslands().remove(i);
  }

  @Override
  public String toString() {
    String result = "";
    for (Island island : getIslands()) {
      if (result.isEmpty())
        result = "[ " + island;
      else
        result += ", " + island;
    }
    result += " ]";
    return result;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(islands);
  }

  @Override
  public boolean equals(Object object) {
    if (object == null) return false;
    if (object.getClass() != this.getClass()) return false;
    if (((Archipelago) object).size() != this.size()) return false;
    for (int i = 0; i < size(); i++) {
      if (!((Archipelago) object).get(i).equals(get(i))) return false;
    }
    return true;
  }

  private Map<Integer, Integer> getCoordinatesMap() {
    final Map<Integer, Integer> map = Maps.newHashMap();
    for (final Island isl : iterator()) {
      for (final Coordinate c : isl) {
        map.put(c.getRow(), c.getColumn());
      }
    }
    return map;
  }

  private double distance(Island isl1, Island isl2) {
    double result = 0.0;
    int isl1_L_x = isl1.getLeftEnd().column;
    int isl1_L_y = isl1.getLeftEnd().row;
    int isl1_R_x = isl1.getRightEnd().column;
    int isl1_R_y = isl1.getRightEnd().row;
    int isl2_L_x = isl2.getLeftEnd().column;
    int isl2_L_y = isl2.getLeftEnd().row;
    int isl2_R_x = isl2.getRightEnd().column;
    int isl2_R_y = isl2.getRightEnd().row;
    result = distance(isl1_L_x, isl1_L_y, isl2_L_x, isl2_L_y);
    double d = distance(isl1_L_x, isl1_L_y, isl2_R_x, isl2_R_y);
    if (d < result) result = d;
    d = distance(isl1_R_x, isl1_R_y, isl2_L_x, isl2_L_y);
    if (d < result) result = d;
    d = distance(isl1_R_x, isl1_R_y, isl2_R_x, isl2_R_y);
    if (d < result) result = d;
    return result;
  }

  private double distance(int a_x, int a_y, int b_x, int b_y) {
    double result = 0.0;
    result = Math.sqrt((a_x - b_x) * (a_x - b_x) + (a_y - b_y) * (a_y - b_y));
    return result;
  }

  public Set<Integer> getIslandVectors() {
    return islandvectors;
  }

  public double smallestDistance(Island isl) {
    double minimum = 10000;
    for (Island fixedIsland : getIslands()) {
      minimum = Math.min(minimum, distance(isl, fixedIsland));
    }
    return minimum;
  }

  public double smallestDistanceToIdealLine(Island isl) {
    double minimum = 10000;
    Island closestIsland = null;
    for (Island fixedIsland : getIslands()) {
      double prev = minimum;
      minimum = Math.min(minimum, distance(isl, fixedIsland));
      if (prev > minimum) {
        closestIsland = fixedIsland;
      }
    }
    if (closestIsland == null) {
      return minimum;
    }
    Coordinate leftEnd = isl.getLeftEnd();
    int islandVector = leftEnd.row - leftEnd.column;
    Coordinate leftEnd0 = closestIsland.getLeftEnd();
    int closestIslandVector = leftEnd0.row - leftEnd0.column;
    return Math.abs(islandVector - closestIslandVector);
  }
}
