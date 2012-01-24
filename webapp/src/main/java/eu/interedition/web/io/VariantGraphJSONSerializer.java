package eu.interedition.web.io;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphEdge;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.input.SimpleToken;
import eu.interedition.web.collatex.WebToken;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.Transaction;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphJSONSerializer extends AbstractHttpMessageConverter<VariantGraph> {

  protected static final MediaType APPLICATION_VARIANTGRAPH_JSON = new MediaType("application", "collatex.graph+json");

  protected static final MediaType APPLICATION_ALIGNMENTTABLE_JSON = new MediaType("application", "collatex.table+json");

  private ObjectMapper objectMapper = new ObjectMapper();

  public VariantGraphJSONSerializer() {
    super(MediaType.APPLICATION_JSON);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return VariantGraph.class.isAssignableFrom(clazz);
  }

  @Override
  protected boolean canRead(MediaType mediaType) {
    return false;
  }

  @Override
  protected VariantGraph readInternal(Class<? extends VariantGraph> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    throw new HttpMessageNotReadableException(clazz.toString());
  }

  @Override
  protected void writeInternal(VariantGraph graph, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    final OutputStream body = outputMessage.getBody();
    final JsonGenerator jgen = objectMapper.getJsonFactory().createJsonGenerator(body);
    final Transaction tx = graph.newTransaction();
    try {
      final MediaType contentType = outputMessage.getHeaders().getContentType();
      if (contentType != null && contentType.isCompatibleWith(APPLICATION_VARIANTGRAPH_JSON)) {
        jgen.writeStartArray();
        for (VariantGraphVertex vertex : graph.vertices()) {
          final String vertexId = toId(vertex);

          jgen.writeStartObject();
          jgen.writeStringField("id", vertexId);
          jgen.writeStringField("name", VariantGraphVertex.TO_CONTENTS.apply(vertex));

          jgen.writeObjectFieldStart("data");
          jgen.writeStringField("$color", color(vertex));
          jgen.writeStringField("$type", type(vertex));
          jgen.writeNumberField("$dim", 10);
          jgen.writeEndObject();

          jgen.writeArrayFieldStart("adjacencies");
          for (VariantGraphEdge edge : vertex.outgoing()) {
            jgen.writeStartObject();
            jgen.writeStringField("nodeFrom", vertexId);
            jgen.writeStringField("nodeTo", toId(edge.to()));

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
        final List<Witness> witnesses = Ordering.from(Witness.SIGIL_COMPARATOR).sortedCopy(graph.witnesses());
        final RowSortedTable<Integer,Witness,Set<Token>> table = graph.toTable();

        jgen.writeStartObject();

        jgen.writeNumberField("rows", table.rowKeySet().size());
        jgen.writeNumberField("columns", table.columnKeySet().size());
        jgen.writeArrayFieldStart("sigils");

        for (Witness witness : witnesses) {
          jgen.writeString(witness.getSigil());
        }
        jgen.writeEndArray();


        jgen.writeArrayFieldStart("table");
        for (Integer row : table.rowKeySet()) {
          final Map<Witness,Set<Token>> cells = table.row(row);
          jgen.writeStartArray();
          for (Witness witness : witnesses) {
            final Set<Token> cellContents = cells.get(witness);
            if (cellContents == null) {
              jgen.writeNull();
            } else {
              final List<SimpleToken> cell = Ordering.natural().sortedCopy(Iterables.filter(cellContents, SimpleToken.class));
              jgen.writeStartArray();
              for (SimpleToken token : cell) {
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

  private static String toId(VariantGraphVertex vertex) {
    return Long.toString(vertex.getNode().getId());
  }

  private static String type(VariantGraphVertex vertex) {
    final VariantGraph graph = vertex.getGraph();
    if (graph.getStart().equals(vertex) || graph.getEnd().equals(vertex)) {
      return "star";
    } else if (Iterables.size(vertex.incoming()) == 1 && Iterables.size(vertex.outgoing()) == 1) {
      return "circle";
    } else {
      return "triangle";
    }
  }

  private static String color(VariantGraphVertex vertex) {
    if (vertex.witnesses().size() == 0) {
      return COLORS[0];
    }
    final int rank = vertex.getRank();
    return (rank >= COLORS.length ? "white" : COLORS[rank]);
  }

  private static final String[] COLORS = {"grey", "blue", "green", "red", "brown", "orange", "purple", "cyan", "taupe", "yellow", "lightred"};
}
