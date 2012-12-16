package eu.interedition.collatex.io;

import com.google.common.io.Closeables;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.VariantGraphEdge;
import eu.interedition.collatex.VariantGraphTransposition;
import eu.interedition.collatex.VariantGraphVertex;
import eu.interedition.collatex.neo4j.Neo4jVariantGraph;
import eu.interedition.collatex.neo4j.Neo4jVariantGraphEdge;
import eu.interedition.collatex.neo4j.Neo4jVariantGraphVertex;
import org.neo4j.graphdb.Transaction;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Provider
@Produces(MediaType.TEXT_PLAIN)
public class VariantGraphDotMessageBodyWriter implements MessageBodyWriter<VariantGraph> {

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Neo4jVariantGraph.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(VariantGraph variantGraph, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(VariantGraph graph, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    final Transaction tx = ((Neo4jVariantGraph)graph).getDatabase().beginTx();
    try {
      final PrintWriter out = new PrintWriter(new OutputStreamWriter(entityStream, "UTF-8"));
      try {
        final String indent = "  ";
        final String connector = " -> ";

        out.println("digraph G {");

        for (VariantGraphVertex v : graph.vertices()) {
          out.print(indent + "v" + ((Neo4jVariantGraphVertex) v).getNode().getId());
          out.print(" [label = \"" + toLabel(v) + "\"]");
          out.println(";");
        }

        for (VariantGraphEdge e : graph.edges()) {
          out.print(indent + "v" + e.from().getNode().getId() + connector + "v" + e.to().getNode().getId());
          out.print(" [label = \"" + toLabel(e) + "\"]");
          out.println(";");
        }

        for (VariantGraphTransposition t : graph.transpositions()) {
          out.print(indent + "v" + t.from().getNode().getId() + connector + "v" + t.to().getNode().getId());
          out.print(" [color = \"lightgray\", style = \"dashed\" arrowhead = \"none\", arrowtail = \"none\" ]");
          out.println(";");
        }

        out.println("}");
      } finally {
        Closeables.close(out, false);
      }
    } finally {
      tx.finish();
    }
  }

  private String toLabel(VariantGraphEdge e) {
    return Neo4jVariantGraphEdge.TO_CONTENTS.apply(e).replaceAll("\"", "\\\"");
  }

  private String toLabel(VariantGraphVertex v) {
    return Neo4jVariantGraphVertex.TO_CONTENTS.apply(v).replaceAll("\"", "\\\"");
  }

}
