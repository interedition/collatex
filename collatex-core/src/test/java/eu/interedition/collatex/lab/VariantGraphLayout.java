package eu.interedition.collatex.lab;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SortedSetMultimap;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphLayout {

  private final VariantGraph graph;
  private final List<List<Cell>> grid = Lists.newLinkedList();

  /**
   * represents the size of the grid in horizontal grid elements
   */
  private int maxX = Integer.MIN_VALUE;

  /**
   * Implementation.
   * <p/>
   * First of all, the Algorithm searches the roots from the
   * Graph. Starting from this roots the Algorithm creates
   * levels and stores them in the member <code>levels</code>.
   * The Member levels contains LinkedList Objects and the LinkedList per level
   * contains Cell Wrapper Objects. After that the Algorithm
   * tries to solve the edge crosses from level to level and
   * goes top down and bottom up. After minimization of the
   * edge crosses the algorithm moves each node to its
   * bary center.
   */
  public static List<List<Cell>> of(VariantGraph graph) {
    final VariantGraphLayout layout = new VariantGraphLayout(graph);

    layout.fillLevels();
    layout.solveEdgeCrosses();
    layout.moveToBarycenter();

    return layout.grid;
  }

  private VariantGraphLayout(VariantGraph graph) {
    this.graph = graph;
  }

  private void fillLevels() {
    final SortedSetMultimap<Integer,VariantGraph.Vertex> ranks = VariantGraphRanking.of(graph).getByRank();
    for (Integer rank : ranks.keySet()) {
      final List<Cell> cells = Lists.<Cell>newLinkedList();
      for (VariantGraph.Vertex vertex : ranks.get(rank)) {
        cells.add(new Cell(rank, cells.size(), vertex));
      }
      grid.add(cells);
      maxX = Math.max(maxX, rank);
    }
  }

  private void solveEdgeCrosses() {
    int movementsCurrentLoop = -1;

    while (movementsCurrentLoop != 0) {
      // reset the movements per loop count
      movementsCurrentLoop = 0;

      // top down
      for (int i = 0; i < grid.size() - 1; i++) {
        movementsCurrentLoop += solveEdgeCrosses(true, i);
      }

      // bottom up
      for (int i = grid.size() - 1; i >= 1; i--) {
        movementsCurrentLoop += solveEdgeCrosses(false, i);
      }
    }
  }

  /**
   * @return movements
   */
  private int solveEdgeCrosses(boolean down, int level) {
    // Get the current level
    final List<Cell> cells = grid.get(level);
    // remember the old sort
    final List<Cell> levelSortBefore = Lists.newArrayList(cells);
    // new sort
    Collections.sort(cells);

    // test for movements
    int movements = 0;
    for (int j = 0; j < levelSortBefore.size(); j++) {
      if (levelSortBefore.get(j).avgWeight() != cells.get(j).avgWeight()) {
        movements++;
      }
    }

    // Collections Sort sorts the highest value to the first value
    for (Cell cell : Lists.reverse(cells)) {
      final VariantGraph.Vertex vertex = cell.vertex;

      for (VariantGraph.Edge edge : (down ? vertex.outgoing() : vertex.incoming())) {
        final Cell neighborCell = vertexToCell.get((down ? edge.to() : edge.from()));

        // do it only if the edge is a forward edge to a deeper level
        if (down && neighborCell.y > level) {
          neighborCell.addWeight(cell.avgWeight());
        }
        if (!down &&  neighborCell.y < level) {
          neighborCell.addWeight(cell.avgWeight());
        }
      }
    }
    return movements;
  }

  private void moveToBarycenter() {
    for (VariantGraph.Vertex vertex : graph.vertices()) {
      final Cell cell = vertexToCell.get(vertex);
      for (VariantGraph.Vertex neighbor : neighborsOf(vertex)) {
        if (cell.y != vertexToCell.get(neighbor).y) {
          cell.priority++;
        }
      }
    }

    for (List<Cell> level : grid) {
      int pos = 1;
      for (Cell cell : level) {
        // calculate the initial Grid Positions 1, 2, 3, .... per Level
        cell.x = pos++;
      }
    }

    int movementsCurrentLoop = -1;

    while (movementsCurrentLoop != 0) {
      // reset movements
      movementsCurrentLoop = 0;

      // top down
      for (int i = 1; i < grid.size(); i++) {
        movementsCurrentLoop += moveToBarycenter(i);
      }
      // bottom up
      for (int i = grid.size() - 1; i >= 0; i--) {
        movementsCurrentLoop += moveToBarycenter(i);
      }
    }
  }


  private int moveToBarycenter(int level) {
    // Counter for the movements
    int movements = 0;

    // Get the current level
    final List<Cell> cells = grid.get(level);

    for (int currentIndexInTheLevel = 0; currentIndexInTheLevel < cells.size(); currentIndexInTheLevel++) {
      final Cell cell = cells.get(currentIndexInTheLevel);

      float gridPositionsSum = 0;
      float countNodes = 0;

      for (VariantGraph.Vertex neighbor : neighborsOf(cell.vertex)) {
        Cell neighborCell = vertexToCell.get(neighbor);
        if (neighborCell.y != level) {
          gridPositionsSum += neighborCell.x;
          countNodes++;
        }
      }

      if (countNodes > 0) {
        float tmp = (gridPositionsSum / countNodes);
        int newGridPosition = Math.round(tmp);
        boolean toRight = (newGridPosition > cell.x);

        boolean moved = true;

        while (newGridPosition != cell.x && moved) {
          moved = move(toRight, cells, currentIndexInTheLevel, cell.priority);
          if (moved) {
            movements++;
          }
        }
      }
    }
    return movements;
  }

  private boolean move(boolean toRight, List<Cell> currentLevel, int currentIndexInTheLevel, int currentPriority) {
    final Cell cell = currentLevel.get(currentIndexInTheLevel);

    boolean moved;
    int neighborIndexInTheLevel = currentIndexInTheLevel + (toRight ? 1 : -1);
    int newX = cell.x + (toRight ? 1 : -1);

    if (0 > newX || newX >= maxX) {
      return false;
    }

    // if the node is the first or the last we can move
    if (toRight && currentIndexInTheLevel == currentLevel.size() - 1 || !toRight && currentIndexInTheLevel == 0) {
      moved = true;
    } else {
      // else get the neighbor and ask his gridposition
      // if he has the requested new grid position
      // check the priority
      final Cell neighborCell = currentLevel.get(neighborIndexInTheLevel);
      if (neighborCell.x == newX) {
        if (neighborCell.priority >= currentPriority) {
          return false;
        } else {
          moved = move(toRight, currentLevel, neighborIndexInTheLevel, currentPriority);
        }
      } else {
        moved = true;
      }
    }

    if (moved) {
      cell.x = newX;
    }
    return moved;
  }

  private List<VariantGraph.Vertex> neighborsOf(VariantGraph.Vertex vertex) {
    final List<VariantGraph.Vertex> neighbors = Lists.newLinkedList();
    for (VariantGraph.Edge outgoing : vertex.outgoing()) {
      neighbors.add(outgoing.to());
    }
    for (VariantGraph.Edge incoming : vertex.incoming()) {
      neighbors.add(incoming.from());
    }
    return neighbors;
  }

  private final Map<VariantGraph.Vertex, Cell> vertexToCell = Maps.newHashMap();

  class Cell implements Comparable<Cell> {
    /**
     * sum value for edge Crosses
     */
    double totalWeight = 0;

    /**
     * counter for additions to the edgeCrossesIndicator
     */
    int additions = 0;

    /**
     * current position in the grid
     */
    int x = 0;

    /**
     * the vertical level where the cell wrapper is inserted
     */
    int y;

    /**
     * priority for movements to the barycenter
     */
    int priority = 0;

    /**
     * reference to the wrapped cell
     */
    VariantGraph.Vertex vertex = null;


    Cell(int y, double weight, VariantGraph.Vertex vertex) {
      this.y = y;
      this.vertex = vertex;
      vertexToCell.put(vertex, this);
      addWeight(weight);
    }

    /**
     * Returns the average value for the edge crosses indicator for the cell
     */
    double avgWeight() {
      return (totalWeight / additions);
    }

    /**
     * Adds a value to the edge crosses indicator for the cell
     */
    void addWeight(double weight) {
      this.totalWeight += weight;
      this.additions++;
    }

    public int compareTo(Cell other) {
      final double result = (other.avgWeight() - this.avgWeight());
      if (result < 0) {
        return -1;
      } else if (result > 0) {
        return 1;
      } else {
        return 0;
      }
    }
  }
}
