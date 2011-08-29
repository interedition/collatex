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

package eu.interedition.collatex.web.io;

import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.jgrapht.alg.DijkstraShortestPath;

import java.io.IOException;
import java.util.Iterator;

public class VariantGraphSerializer extends JsonSerializer<IVariantGraph> {
  @Override
  public Class<IVariantGraph> handledType() {
    return IVariantGraph.class;
  }

  @Override
  public void serialize(IVariantGraph value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
    jgen.writeStartArray();
    for (Iterator<IVariantGraphVertex> iterator = value.iterator(); iterator.hasNext(); ) {
      final IVariantGraphVertex vertex = iterator.next();
      final String vertexId = vertex.toString();

      jgen.writeStartObject();
      jgen.writeStringField("id", vertexId);
      jgen.writeStringField("name", vertex.getNormalized());

      jgen.writeObjectFieldStart("data");
      jgen.writeStringField("$color", color(value, vertex));
      jgen.writeStringField("$type", type(value, vertex));
      jgen.writeNumberField("$dim", 10);
      jgen.writeEndObject();

      jgen.writeArrayFieldStart("adjacencies");
      for (IVariantGraphEdge edge : value.outgoingEdgesOf(vertex)) {
        jgen.writeStartObject();
        jgen.writeStringField("nodeFrom", vertexId);
        jgen.writeStringField("nodeTo", value.getEdgeTarget(edge).toString());

        jgen.writeObjectFieldStart("data");
        jgen.writeStringField("$color", "grey");
        jgen.writeStringField("$type", "arrow");
        jgen.writeEndObject();

        jgen.writeEndObject();
      }
      jgen.writeEndArray();

      jgen.writeEndObject();
    }

    jgen.writeEndArray();
  }

  private static String type(IVariantGraph graph, IVariantGraphVertex vertex) {
    if (graph.inDegreeOf(vertex) == 0 || graph.outDegreeOf(vertex) == 0) {
      return "star";
    } else if (!(graph.inDegreeOf(vertex) == 1 && graph.outDegreeOf(vertex) == 1)) {
      return "triangle";
    }
    return "circle";
  }

  private static String color(IVariantGraph graph, IVariantGraphVertex vertex) {
    if (vertex.getWitnesses().size() == 0) {
      return COLORS[0];
    }
    DijkstraShortestPath<IVariantGraphVertex, IVariantGraphEdge> dsp = new DijkstraShortestPath<IVariantGraphVertex, IVariantGraphEdge>(graph, graph.getStartVertex(), vertex);
    int topologicalIndex = (int) dsp.getPathLength();
    if (topologicalIndex > COLORS.length) {
      return "white";
    }
    return COLORS[topologicalIndex];
  }

  //  private String color(IVariantGraphVertex vertex) {
  //    if (vertex.getWitnesses().size() == 0) {
  //      return COLORS[0];
  //    } else {
  //      int i = 1;
  //      while (i < COLORS.length) {
  //        if (vertex.containsWitness(witnesses[i].getSigil())) {
  //          return COLORS[i + 1];
  //        }
  //        i += 1;
  //      }
  //    }
  //    return "white";
  //  }

  private static final String[] COLORS = {"grey", "blue", "green", "red", "brown", "orange", "purple", "cyan", "taupe", "yellow", "lightred"};
}
