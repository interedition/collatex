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

package eu.interedition.collatex.implementation.output;

import eu.interedition.collatex.implementation.graph.RankedVariantGraphVertex;
import eu.interedition.collatex.implementation.graph.SegmentedVariantGraph;
import eu.interedition.collatex.interfaces.IColumn;
import eu.interedition.collatex.interfaces.IVariantGraph;
import eu.interedition.collatex.interfaces.IVariantGraphVertex;
import eu.interedition.collatex.interfaces.IWitness;

import java.util.Iterator;
import java.util.List;

//TODO: remove explicit dependency on ranked graph implementation classes!
//TODO: The maximum dependency is implementation classes of the same package!
public class RankedGraphBasedAlignmentTable extends BaseAlignmentTable {
  private final IVariantGraph graph;

  public RankedGraphBasedAlignmentTable(IVariantGraph graph) {
    this.graph = graph;
    init();
  }

  private void init() {
    SegmentedVariantGraph segmentedVariantGraph = SegmentedVariantGraph.create(graph);
    Iterator<RankedVariantGraphVertex> iterator = segmentedVariantGraph.getRankedVertices().iterator();
    Iterator<IVariantGraphVertex> vertexIterator = graph.iterator();
    //skip startVertex
    vertexIterator.next();
    while(iterator.hasNext()) {
      //nextVertex is a IRankedVariantGraphVertex which is not the 
      //same as a real vertex!
      RankedVariantGraphVertex nextVertex = iterator.next();
      IVariantGraphVertex next = vertexIterator.next();
      if (next.equals(graph.getEndVertex())) {
        continue;
      }
      int rank = nextVertex.getRank();
//      System.out.println("DEBUG: "+nextVertex.getNormalized()+":"+nextVertex.getRank());
      if (rank>columns.size()) {
        addNewColumn(next);
      } else {
        ((VariantGraphBasedColumn)columns.get(rank-1)).addVertex(next);
      }
    }
//    System.out.println("TOTAL number of columns: "+columns.size());
  }

  private IColumn addNewColumn(IVariantGraphVertex vertex) {
    final IColumn column = new VariantGraphBasedColumn(vertex);
    columns.add(column);
    return column;
  }
  
  @Override
  public final List<IWitness> getWitnesses() {
    return graph.getWitnesses();
  }

}
