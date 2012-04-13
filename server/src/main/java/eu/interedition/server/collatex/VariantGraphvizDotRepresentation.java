package eu.interedition.server.collatex;

import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphEdge;
import eu.interedition.collatex.graph.VariantGraphTransposition;
import eu.interedition.collatex.graph.VariantGraphVertex;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VariantGraphvizDotRepresentation extends WriterRepresentation implements VariantGraphRepresentation {
  private VariantGraph graph;

  public VariantGraphvizDotRepresentation() {
    super(MediaType.TEXT_PLAIN);
  }

  @Override
  public Representation forGraph(VariantGraph graph) {
    this.graph = graph;
    return this;
  }

  @Override
  public void write(Writer writer) throws IOException {
    final PrintWriter out = new PrintWriter(writer);
    final String indent = "  ";
    final String connector = " -> ";

    out.println("digraph G {");

    for (VariantGraphVertex v : graph.vertices()) {
      out.print(indent + "v" + v.getNode().getId());
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

    out.flush();
  }

  private String toLabel(VariantGraphEdge e) {
    return VariantGraphEdge.TO_CONTENTS.apply(e).replaceAll("\"", "\\\"");
  }

  private String toLabel(VariantGraphVertex v) {
    return VariantGraphVertex.TO_CONTENTS.apply(v).replaceAll("\"", "\\\"");
  }
}
