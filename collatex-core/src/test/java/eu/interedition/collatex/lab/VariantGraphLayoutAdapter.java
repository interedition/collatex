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

package eu.interedition.collatex.lab;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import eu.interedition.collatex.VariantGraph;

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
public class VariantGraphLayoutAdapter extends AbstractLayout<VariantGraph.Vertex, VariantGraph.Edge> {

  public static enum Orientation {
    TOP, LEFT
  }

  private final Orientation orientation;
  private final int horzSpacing;
  private final int vertSpacing;

  private boolean executed = false;

  public VariantGraphLayoutAdapter(VariantGraph g, Orientation orientation, int horzSpacing, int vertSpacing) {
    super(g);
    this.orientation = orientation;
    this.horzSpacing = horzSpacing;
    this.vertSpacing = vertSpacing;
  }

  public void initialize() {
    if (!executed) {
      for (List<VariantGraphLayout.Cell> level : VariantGraphLayout.of((VariantGraph) getGraph())) {
        for (VariantGraphLayout.Cell cell : level) {

          if (orientation.equals(Orientation.TOP)) {
            double xCoordinate = 10.0 + (cell.x * horzSpacing);
            double yCoordinate = 10.0 + (cell.y * vertSpacing);
            setLocation(cell.vertex, xCoordinate, yCoordinate);
          } else {
            double yCoordinate = 10.0 + (cell.x * vertSpacing);
            double xCoordinate = 10.0 + (cell.y * horzSpacing);
            setLocation(cell.vertex, xCoordinate, yCoordinate);
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
