package eu.interedition.collatex.matrixlinker;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import eu.interedition.collatex.matrixlinker.MatchMatrix.Coordinate;
import eu.interedition.collatex.matrixlinker.MatchMatrix.Island;

public class Archipelago {
  Logger LOG = LoggerFactory.getLogger(Archipelago.class);

  private ArrayList<MatchMatrix.Island> islands;

  public Archipelago() {
    setIslands(new ArrayList<MatchMatrix.Island>());
  }

  public Archipelago(MatchMatrix.Island isl) {
    setIslands(new ArrayList<MatchMatrix.Island>());
    getIslands().add(isl);
  }

  public void add(MatchMatrix.Island island) {
    for (MatchMatrix.Island i : getIslands()) {
      if (island.size() > i.size()) {
        getIslands().add(getIslands().indexOf(i), island);
        return;
      } else
        try {
          MatchMatrix.Island disl = island;
          MatchMatrix.Island di = i;
          if (island.size() > i.size() && disl.direction() > di.direction()) {
            getIslands().add(getIslands().indexOf(i), island);
            return;
          }
        } catch (Exception e) {}
    }
    getIslands().add(island);
  }

  // this is not a real iterator implementation but it works...
  public ArrayList<MatchMatrix.Island> iterator() {
    return getIslands();
  }

  protected void remove(int i) {
    getIslands().remove(i);
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

  public MatchMatrix.Island get(int i) {
    return getIslands().get(i);
  }

  public Archipelago copy() {
    Archipelago result = new Archipelago();
    for (MatchMatrix.Island isl : getIslands()) {
      result.add(new MatchMatrix.Island(isl));
    }
    return result;
  }

  public boolean conflictsWith(MatchMatrix.Island island) {
    for (MatchMatrix.Island isl : getIslands()) {
      if (isl.isCompetitor(island)) return true;
    }
    return false;
  }

  @Override
  public String toString() {
    String result = "";
    for (MatchMatrix.Island island : getIslands()) {
      if (result.isEmpty())
        result = "[ " + island;
      else
        result += ", " + island;
    }
    result += " ]";
    return result;
  }

  public int value() {
    int result = 0;
    for (MatchMatrix.Island isl : getIslands()) {
      result += isl.value();
    }
    return result;
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

  public ArrayList<MatchMatrix.Coordinate> findGaps() {
    ArrayList<MatchMatrix.Coordinate> list = new ArrayList<MatchMatrix.Coordinate>();
    return findGaps(list);
  }

  public ArrayList<MatchMatrix.Coordinate> findGaps(MatchMatrix.Coordinate begin, MatchMatrix.Coordinate end) {
    ArrayList<MatchMatrix.Coordinate> list = new ArrayList<MatchMatrix.Coordinate>();
    list.add(begin);
    list.add(end);
    return findGaps(list);
  }

  public ArrayList<MatchMatrix.Coordinate> findGaps(ArrayList<MatchMatrix.Coordinate> list) {
    ArrayList<MatchMatrix.Coordinate> result = new ArrayList<MatchMatrix.Coordinate>(list);
    for (MatchMatrix.Island isl : getIslands()) {
      MatchMatrix.Coordinate left = isl.getLeftEnd();
      MatchMatrix.Coordinate right = isl.getRightEnd();
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

  private Map<Integer, Integer> getCoordinatesMap() {
    final Map<Integer, Integer> map = Maps.newHashMap();
    for (final Island isl : iterator()) {
      for (final Coordinate c : isl) {
        map.put(c.getRow(), c.getColumn());
      }
    }
    return map;
  }

  public boolean islandsCompete(Island i1, Island i2) {
    return i1.isCompetitor(i2);
  }

  public void setIslands(ArrayList<MatchMatrix.Island> islands) {
    this.islands = islands;
  }

  public ArrayList<MatchMatrix.Island> getIslands() {
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
      LOG.info("{} -> {}", island1, island2);
      throw new RuntimeException("no minimum found, help!");
    }
    return closest;
  }

  private double distance(MatchMatrix.Island isl1, MatchMatrix.Island isl2) {
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

  public double smallestDistance(Island isl) {
    double minimum = 10000;
    for (Island fixedIsland : getIslands()) {
      minimum = Math.min(minimum, distance(isl, fixedIsland));
    }
    return minimum;
  }

}
