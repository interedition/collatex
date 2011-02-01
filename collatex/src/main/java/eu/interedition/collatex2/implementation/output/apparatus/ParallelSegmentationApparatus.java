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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.output.jgraph.JVariantGraphCreator;
import eu.interedition.collatex2.implementation.output.rankedgraph.IRankedVariantGraphVertex;
import eu.interedition.collatex2.implementation.output.rankedgraph.VariantGraphRanker;
import eu.interedition.collatex2.implementation.output.segmented_graph.ISegmentedVariantGraph;
import eu.interedition.collatex2.implementation.output.segmented_graph.ISegmentedVariantGraphVertex;
import eu.interedition.collatex2.implementation.output.segmented_graph.JGraphToSegmentedVariantGraphConverter;
import eu.interedition.collatex2.interfaces.IJVariantGraph;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class ParallelSegmentationApparatus {
  private static Logger logger = LoggerFactory.getLogger(ParallelSegmentationApparatus.class);
  
  private final List<ApparatusEntry> entries;
  private List<String> sigla;

  public List<ApparatusEntry> getEntries() {
    return entries;
  }

  public List<String> getSigla() {
    if (this.sigla == null) {
      final Set<String> sigli = Sets.newLinkedHashSet();
      for (final ApparatusEntry column : entries) {
        sigli.addAll(column.getSigla());
      }
      this.sigla = Lists.newArrayList(sigli);
    }
    return this.sigla;
  }

  /**
   * Factory method that builds a ParallelSegmentationApparatus from a VariantGraph
   * 
   */
  
  public static ParallelSegmentationApparatus build(IVariantGraph graph) {
    // we first create a SegmentedVariantGraph from the IVariantGraph
    // therefore create a JoinedGraph first
    IJVariantGraph joinedGraph = JVariantGraphCreator.parallelSegmentate(graph);
    JGraphToSegmentedVariantGraphConverter converter = new JGraphToSegmentedVariantGraphConverter();
    ISegmentedVariantGraph segmentedVariantGraph = converter.convert(joinedGraph);
    
    // NOTE: forget the normal variant graph after this point; only use the segmented one!
    // TODO: look at the other piece of code also!
    VariantGraphRanker ranker = new VariantGraphRanker(segmentedVariantGraph);
    List<ApparatusEntry> entries = Lists.newArrayList();
    Iterator<IRankedVariantGraphVertex> iterator = ranker.iterator();
    Iterator<ISegmentedVariantGraphVertex> vertexIterator = segmentedVariantGraph.iterator();
    //skip startVertex
    vertexIterator.next();
    while(iterator.hasNext()) {
      //nextVertex is a IRankedVariantGraphVertex which is not the 
      //same as a real vertex!
      IRankedVariantGraphVertex nextVertex = iterator.next();
      ISegmentedVariantGraphVertex next = vertexIterator.next();
      if (next.equals(graph.getEndVertex())) {
        continue;
      }
      int rank = nextVertex.getRank();
//      System.out.println("DEBUG: "+nextVertex.getNormalized()+":"+nextVertex.getRank());
      if (rank>entries.size()) {
        //Note: doing this over and over and over is not very efficient
        //Note: it might be better to make a graph and vertices based implementation
        List<IWitness> witnesses = graph.getWitnesses();
        List<String> sigla = Lists.newArrayList();
        for (IWitness witness : witnesses) {
          sigla.add(witness.getSigil());
        }
        ApparatusEntry apparatusEntry = new ApparatusEntry(sigla);
        for (IWitness witness : next.getWitnesses()) {
          IPhrase phrase = next.getPhrase(witness);
          apparatusEntry.addPhrase(witness.getSigil(), phrase);
        }
        entries.add(apparatusEntry);
      } else {
        ApparatusEntry apparatusEntry = entries.get(rank-1);
        for (IWitness witness : next.getWitnesses()) {
          IPhrase phrase = next.getPhrase(witness);
          apparatusEntry.addPhrase(witness.getSigil(), phrase);
        }
      }
    }

    
    // convert SegmentedVariantGraph to ParallelSegmentationApparatus
    return new ParallelSegmentationApparatus(entries);
  }

  private ParallelSegmentationApparatus(final List<ApparatusEntry> entries) {
    this.entries = entries;
  }
}
