package eu.interedition.collatex.matrixlinker;

import java.util.ArrayList;
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import eu.interedition.collatex.matrixlinker.MatchMatrix.Coordinates;
import eu.interedition.collatex.matrixlinker.MatchMatrix.Island;

public class Archipelago {

  protected ArrayList<MatchMatrix.Island> islands;

  public Archipelago() {
    islands = new ArrayList<MatchMatrix.Island>();
  }

  public Archipelago(MatchMatrix.Island isl) {
    islands = new ArrayList<MatchMatrix.Island>();
    islands.add(isl);
  }

  public void add(MatchMatrix.Island island) {
    for (MatchMatrix.Island i : islands) {
      if (island.size() > i.size()) {
        islands.add(islands.indexOf(i), island);
        return;
      } else
        try {
          MatchMatrix.Island disl = island;
          MatchMatrix.Island di = i;
          if (island.size() > i.size() && disl.direction() > di.direction()) {
            islands.add(islands.indexOf(i), island);
            return;
          }
        } catch (Exception e) {}
    }
    islands.add(island);
  }

  // this is not a real iterator implementation but it works...
  public ArrayList<MatchMatrix.Island> iterator() {
    return islands;
  }

  protected void remove(int i) {
    islands.remove(i);
  }

  public int size() {
    return islands.size();
  }

  public void mergeIslands() {
    int i = 0;
    int j = 1;
    int[] rr = new int[size()];
    for (i = 0; i < size(); i++) {
      for (j = i + 1; j < size(); j++) {
        if (islands.get(i).overlap(islands.get(j))) {
          (islands.get(i)).merge(islands.get(j));
          islands.get(j).clear();
          rr[j] = 1;
        }
      }
    }
    for (i = (rr.length - 1); i > 0; i--) {
      if (rr[i] == 1) islands.remove(i);
    }
  }

  public Object numOfConflicts() {
    int result = 0;
    int num = islands.size();
    for (int i = 0; i < num; i++)
      for (int j = i + 1; j < num; j++) {
        //				System.out.println("compare "+islands.get(j)+" with "+islands.get(i));				
        if (islands.get(j).isCompetitor(islands.get(i))) result++;
      }
    return result;
  }

  public MatchMatrix.Island get(int i) {
    return islands.get(i);
  }

  public Archipelago copy() {
    Archipelago result = new Archipelago();
    for (MatchMatrix.Island isl : islands) {
      result.add(new MatchMatrix.Island(isl));
    }
    return result;
  }

  public boolean conflictsWith(MatchMatrix.Island island) {
    for (MatchMatrix.Island isl : islands) {
      if (isl.isCompetitor(island)) return true;
    }
    return false;
  }

  @Override
  public String toString() {
    String result = "";
    for (MatchMatrix.Island island : islands) {
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
    for (MatchMatrix.Island isl : islands) {
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

  public ArrayList<MatchMatrix.Coordinates> findGaps() {
    ArrayList<MatchMatrix.Coordinates> list = new ArrayList<MatchMatrix.Coordinates>();
    return findGaps(list);
  }

  public ArrayList<MatchMatrix.Coordinates> findGaps(MatchMatrix.Coordinates begin, MatchMatrix.Coordinates end) {
    ArrayList<MatchMatrix.Coordinates> list = new ArrayList<MatchMatrix.Coordinates>();
    list.add(begin);
    list.add(end);
    return findGaps(list);
  }

  public ArrayList<MatchMatrix.Coordinates> findGaps(ArrayList<MatchMatrix.Coordinates> list) {
    ArrayList<MatchMatrix.Coordinates> result = new ArrayList<MatchMatrix.Coordinates>(list);
    for (MatchMatrix.Island isl : islands) {
      MatchMatrix.Coordinates left = isl.getLeftEnd();
      MatchMatrix.Coordinates right = isl.getRightEnd();
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
      for (final Coordinates c : isl) {
        map.put(c.getRow(), c.getColumn());
      }
    }
    return map;
  }
}
