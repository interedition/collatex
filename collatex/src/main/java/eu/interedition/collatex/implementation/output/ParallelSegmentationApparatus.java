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

import com.google.common.collect.Lists;
import eu.interedition.collatex.implementation.graph.JoinedVariantGraph;
import eu.interedition.collatex.implementation.graph.RankedVariantGraphVertex;
import eu.interedition.collatex.implementation.graph.SegmentedVariantGraph;
import eu.interedition.collatex.implementation.graph.SegmentedVariantGraphVertex;
import eu.interedition.collatex.interfaces.IApparatus;
import eu.interedition.collatex.interfaces.IApparatusEntry;
import eu.interedition.collatex.interfaces.IVariantGraph;
import eu.interedition.collatex.interfaces.IWitness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class ParallelSegmentationApparatus implements IApparatus {
  private static Logger logger = LoggerFactory.getLogger(ParallelSegmentationApparatus.class);
  
  private final List<IApparatusEntry> entries;
  private final List<IWitness> witnesses;

  @Override
  public List<IApparatusEntry> getEntries() {
    return entries;
  }

  @Override
  public List<IWitness> getWitnesses() {
    return witnesses;
  }
  
  /**
   * Factory method that builds a ParallelSegmentationApparatus from a VariantGraph
   * 
   */
  
  public static ParallelSegmentationApparatus build(IVariantGraph graph) {
    // we first create a SegmentedVariantGraph from the IVariantGraph
    // therefore create a JoinedGraph first
    JoinedVariantGraph joinedGraph = JoinedVariantGraph.create(graph);
    SegmentedVariantGraph segmentedVariantGraph = SegmentedVariantGraph.create(joinedGraph);
    
    // NOTE: forget the normal variant graph after this point; only use the segmented one!
    // TODO: look at the other piece of code also!
    List<IApparatusEntry> entries = Lists.newArrayList();
    Iterator<RankedVariantGraphVertex> iterator = segmentedVariantGraph.getRankedVertices().iterator();
    Iterator<SegmentedVariantGraphVertex> vertexIterator = segmentedVariantGraph.iterator();
    //skip startVertex
    vertexIterator.next();
    while(iterator.hasNext()) {
      //nextVertex is a IRankedVariantGraphVertex which is not the 
      //same as a real vertex!
      RankedVariantGraphVertex nextVertex = iterator.next();
      SegmentedVariantGraphVertex next = vertexIterator.next();
      if (next.equals(segmentedVariantGraph.getEnd())) {
        continue;
      }
      IApparatusEntry apparatusEntry;
      int rank = nextVertex.getRank();
      if (rank>entries.size()) {
        apparatusEntry = new ApparatusEntry(graph.getWitnesses());
        entries.add(apparatusEntry);
      } else {
        apparatusEntry = entries.get(rank-1);
      }
      ((ApparatusEntry)apparatusEntry).addVertex(next);
    }
    
    // convert SegmentedVariantGraph to ParallelSegmentationApparatus
    return new ParallelSegmentationApparatus(graph.getWitnesses(), entries);
  }

  private ParallelSegmentationApparatus(List<IWitness> witnesses, final List<IApparatusEntry> entries) {
    this.witnesses = witnesses;
    this.entries = entries;
  }
}
