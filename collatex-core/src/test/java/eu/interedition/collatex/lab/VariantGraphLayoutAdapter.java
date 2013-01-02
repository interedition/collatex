package eu.interedition.collatex.lab;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import eu.interedition.collatex.jung.JungVariantGraph;
import eu.interedition.collatex.jung.JungVariantGraphEdge;
import eu.interedition.collatex.jung.JungVariantGraphVertex;

import java.util.List;

/**
 * Arranges the nodes with the Sugiyama Layout Algorithm.
 * <p/>
 * <a href="http://plg.uwaterloo.ca/~itbowman/CS746G/Notes/Sugiyama1981_MVU/">Link to the algorithm</a>
 * <p/>
 * Originally, source was posted to the Jung2 forum, for Jung 1.x. Not sure where the original
 * code came from, but ti didn;t work for Jung2, but it was not that complicated, so I pounded it
 * into shape for Jung2, complete with generics and such. Lays out either top-down to left-right.
 * <p/>
 * Seems to work. Paramterize with spacing and orientation.
 * <p/>
 *
 * @author C. Schanck (chris at schanck dot net)
 */
public class VariantGraphLayoutAdapter extends AbstractLayout<JungVariantGraphVertex, JungVariantGraphEdge> {

  public static enum Orientation {
    TOP, LEFT
  }

  private final Orientation orientation;
  private final int horzSpacing;
  private final int vertSpacing;

  private boolean executed = false;

  public VariantGraphLayoutAdapter(JungVariantGraph g, Orientation orientation, int horzSpacing, int vertSpacing) {
    super(g);
    this.orientation = orientation;
    this.horzSpacing = horzSpacing;
    this.vertSpacing = vertSpacing;
  }

  public void initialize() {
    if (!executed) {
      for (List<VariantGraphLayout.Cell> level : VariantGraphLayout.of((JungVariantGraph) getGraph())) {
        for (VariantGraphLayout.Cell cell : level) {

          if (orientation.equals(Orientation.TOP)) {
            double xCoordinate = 10.0 + (cell.x * horzSpacing);
            double yCoordinate = 10.0 + (cell.y * vertSpacing);
            setLocation((JungVariantGraphVertex) cell.vertex, xCoordinate, yCoordinate);
          } else {
            double yCoordinate = 10.0 + (cell.x * vertSpacing);
            double xCoordinate = 10.0 + (cell.y * horzSpacing);
            setLocation((JungVariantGraphVertex) cell.vertex, xCoordinate, yCoordinate);
          }
        }
      }
      executed = true;
    }

  }

  public void reset() {
    executed = false;
  }

  public String toString() {
    return "Jung Sugiyama";
  }

}
