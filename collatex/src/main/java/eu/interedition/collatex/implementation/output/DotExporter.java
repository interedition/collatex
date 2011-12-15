package eu.interedition.collatex.implementation.output;

import eu.interedition.collatex.implementation.graph.EditGraph;
import eu.interedition.collatex.implementation.graph.EditGraphEdge;
import eu.interedition.collatex.implementation.graph.EditGraphVertex;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;

public class DotExporter {
  private static final String DOT_PATH = "C:\\Program Files\\Graphviz2.26.3\\bin\\dot";

  // EditGraph
  static final VertexNameProvider<EditGraphVertex> EG_VERTEX_ID_PROVIDER = new IntegerNameProvider<EditGraphVertex>(); // {
  //    @Override
  //    public String getVertexName(final EditGraphVertex v) {
  //      return v.toString().replaceAll("eu.interedition.collatex2.implementation.edit_graph.", "").replace('@', '_');
  //    }
  //  };
  static final VertexNameProvider<EditGraphVertex> EG_VERTEX_LABEL_PROVIDER = new VertexNameProvider<EditGraphVertex>() {
    @Override
    public String getVertexName(final EditGraphVertex v) {
      return v.toString();
    }
  };
  static final EdgeNameProvider<EditGraphEdge> EG_EDGE_LABEL_PROVIDER = new EdgeNameProvider<EditGraphEdge>() {
    @Override
    public String getEdgeName(final EditGraphEdge e) {
      return String.valueOf(e.getScore());
    }
  };
  static final DOTExporter<EditGraphVertex, EditGraphEdge> EG_DOT_EXPORTER = new DOTExporter<EditGraphVertex, EditGraphEdge>(//
      EG_VERTEX_ID_PROVIDER, EG_VERTEX_LABEL_PROVIDER, EG_EDGE_LABEL_PROVIDER //
  );

  public static String toDot(final EditGraph graph) {
    final Writer writer = new StringWriter();
    //EG_DOT_EXPORTER.export(writer, graph);
    final String string = writer.toString();
    return string;
  }

  public static void generateSVG(String svg, String dot, String title) {
    try {
      File temp = File.createTempFile("temp", ".dot");
      temp.deleteOnExit();
      FileWriter fileWriter = new FileWriter(temp);
      //      System.out.println(replaceFirst);
      fileWriter.write(dot.replaceFirst("G", "\"" + title + "\""));
      fileWriter.close();
      String[] commands = new String[] { DOT_PATH, "-Grankdir=LR", "-Tsvg", "-o" + svg, temp.getAbsolutePath() };
      Process child = Runtime.getRuntime().exec(commands);
      child.waitFor();
      int exitValue = child.exitValue();
      System.out.println(MessageFormat.format("svg {0} created", svg));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
