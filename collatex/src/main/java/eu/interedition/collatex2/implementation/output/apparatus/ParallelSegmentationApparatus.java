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

package eu.interedition.collatex2.implementation.output.apparatus;

import java.util.Iterator;
import java.util.List;

import eu.interedition.collatex2.implementation.output.jgraph.IJVariantGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.output.jgraph.JVariantGraphCreator;
import eu.interedition.collatex2.implementation.output.rankedgraph.IRankedVariantGraphVertex;
import eu.interedition.collatex2.implementation.output.rankedgraph.VariantGraphRanker;
import eu.interedition.collatex2.implementation.output.segmented_graph.ISegmentedVariantGraph;
import eu.interedition.collatex2.implementation.output.segmented_graph.ISegmentedVariantGraphVertex;
import eu.interedition.collatex2.implementation.output.segmented_graph.JGraphToSegmentedVariantGraphConverter;
import eu.interedition.collatex2.interfaces.IApparatus;
import eu.interedition.collatex2.interfaces.IApparatusEntry;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

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
    JVariantGraphCreator creator = new JVariantGraphCreator();
    IJVariantGraph joinedGraph = creator.parallelSegmentate(graph);
    JGraphToSegmentedVariantGraphConverter converter = new JGraphToSegmentedVariantGraphConverter();
    ISegmentedVariantGraph segmentedVariantGraph = converter.convert(joinedGraph);
    
    // NOTE: forget the normal variant graph after this point; only use the segmented one!
    // TODO: look at the other piece of code also!
    VariantGraphRanker ranker = new VariantGraphRanker(segmentedVariantGraph);
    List<IApparatusEntry> entries = Lists.newArrayList();
    Iterator<IRankedVariantGraphVertex> iterator = ranker.iterator();
    Iterator<ISegmentedVariantGraphVertex> vertexIterator = segmentedVariantGraph.iterator();
    //skip startVertex
    vertexIterator.next();
    while(iterator.hasNext()) {
      //nextVertex is a IRankedVariantGraphVertex which is not the 
      //same as a real vertex!
      IRankedVariantGraphVertex nextVertex = iterator.next();
      ISegmentedVariantGraphVertex next = vertexIterator.next();
      if (next.equals(segmentedVariantGraph.getEndVertex())) {
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
