package eu.interedition.collatex.web.io;

import com.google.common.collect.Iterables;
import com.google.common.collect.RowSortedTable;
import com.google.common.io.Closeables;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraph;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphEdge;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphVertex;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IWitness;
import eu.interedition.collatex.web.WebToken;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.neo4j.graphdb.Transaction;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphJSONSerializer extends AbstractHttpMessageConverter<PersistentVariantGraph> {

  protected static final MediaType APPLICATION_VARIANTGRAPH_JSON = new MediaType("application", "collatex.graph+json");

  protected static final MediaType APPLICATION_ALIGNMENTTABLE_JSON = new MediaType("application", "collatex.table+json");

  private JsonFactory jsonFactory = new JsonFactory();

  public VariantGraphJSONSerializer() {
    super(APPLICATION_VARIANTGRAPH_JSON, APPLICATION_ALIGNMENTTABLE_JSON);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return PersistentVariantGraph.class.isAssignableFrom(clazz);
  }

  @Override
  protected boolean canRead(MediaType mediaType) {
    return false;
  }

  @Override
  protected PersistentVariantGraph readInternal(Class<? extends PersistentVariantGraph> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    throw new HttpMessageNotReadableException(clazz.toString());
  }

  @Override
  protected void writeInternal(PersistentVariantGraph graph, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    final OutputStream body = outputMessage.getBody();
    final JsonGenerator jgen = jsonFactory.createJsonGenerator(body);
    final Transaction tx = graph.newTransaction();
    try {
      final MediaType contentType = outputMessage.getHeaders().getContentType();
      if (contentType != null && contentType.isCompatibleWith(APPLICATION_VARIANTGRAPH_JSON)) {
        jgen.writeStartArray();
        for (PersistentVariantGraphVertex vertex : graph.traverseVertices(null)) {
          final String vertexId = toId(vertex);

          jgen.writeStartObject();
          jgen.writeStringField("id", vertexId);
          jgen.writeStringField("name", PersistentVariantGraphVertex.TO_CONTENTS.apply(vertex));

          jgen.writeObjectFieldStart("data");
          jgen.writeStringField("$color", color(vertex));
          jgen.writeStringField("$type", type(vertex));
          jgen.writeNumberField("$dim", 10);
          jgen.writeEndObject();

          jgen.writeArrayFieldStart("adjacencies");
          for (PersistentVariantGraphEdge edge : vertex.getOutgoingPaths(null)) {
            jgen.writeStartObject();
            jgen.writeStringField("nodeFrom", vertexId);
            jgen.writeStringField("nodeTo", toId(edge.getEnd()));

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
      } else {
        final SortedSet<IWitness> witnesses = graph.getWitnesses();
        final RowSortedTable<Integer,IWitness,SortedSet<INormalizedToken>> table = graph.toTable();

        jgen.writeStartObject();

        jgen.writeNumberField("rows", table.rowKeySet().size());
        jgen.writeNumberField("columns", table.columnKeySet().size());
        jgen.writeArrayFieldStart("sigils");

        for (IWitness witness : witnesses) {
          jgen.writeString(witness.getSigil());
        }
        jgen.writeEndArray();


        jgen.writeArrayFieldStart("table");
        for (Integer row : table.rowKeySet()) {
          final Map<IWitness,SortedSet<INormalizedToken>> cells = table.row(row);
          jgen.writeStartArray();
          for (IWitness witness : witnesses) {
            final SortedSet<INormalizedToken> cell = cells.get(witness);
            if (cell == null) {
              jgen.writeNull();
            } else {
              jgen.writeStartArray();
              for (INormalizedToken token : cell) {
                if (token instanceof WebToken) {
                  jgen.writeTree(((WebToken) token).getJsonNode());
                } else {
                  jgen.writeString(token.getContent());
                }
              }
              jgen.writeEndArray();
            }

          }
          jgen.writeEndArray();
        }
        jgen.writeEndArray();

        jgen.writeEndObject();

      }

      tx.success();
    } finally {
      tx.finish();
      Closeables.closeQuietly(jgen);
      Closeables.closeQuietly(body);
    }
  }

  private static String toId(PersistentVariantGraphVertex vertex) {
    return Long.toString(vertex.getNode().getId());
  }

  private static String type(PersistentVariantGraphVertex vertex) {
    final PersistentVariantGraph graph = vertex.getGraph();
    if (graph.getStart().equals(vertex) || graph.getEnd().equals(vertex)) {
      return "star";
    } else if (Iterables.size(vertex.getIncomingPaths(null)) == 1 && Iterables.size(vertex.getOutgoingPaths(null)) == 1) {
      return "circle";
    } else {
      return "triangle";
    }
  }

  private static String color(PersistentVariantGraphVertex vertex) {
    if (vertex.getWitnesses().size() == 0) {
      return COLORS[0];
    }
    final int rank = vertex.getRank();
    return (rank >= COLORS.length ? "white" : COLORS[rank]);
  }

  private static final String[] COLORS = {"grey", "blue", "green", "red", "brown", "orange", "purple", "cyan", "taupe", "yellow", "lightred"};
}
