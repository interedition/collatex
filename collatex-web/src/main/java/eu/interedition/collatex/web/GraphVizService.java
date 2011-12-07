package eu.interedition.collatex.web;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraph;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphEdge;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphTransposition;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphVertex;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Service
public class GraphVizService implements InitializingBean {

  private final String configuredDotPath = System.getProperty("collatex.graphviz.dot", "/usr/bin/dot");
  private String dotPath;

  public String getConfiguredDotPath() {
    return configuredDotPath;
  }

  public void toDot(PersistentVariantGraph graph, Writer writer, boolean transpositions) {
    final Transaction tx = graph.newTransaction();
    try {
      final PrintWriter out = new PrintWriter(writer);
      final String indent = "  ";
      final String connector = (transpositions ? " -- " : " -> ");

      out.println((transpositions ? "graph" : "digraph") + " G {");

      for (PersistentVariantGraphVertex v : graph.traverseVertices(null)) {
        out.print(indent + "v" + v.getNode().getId());
        out.print(" [label = \"" + toLabel(v) + "\"]");
        out.println(";");
      }

      for (PersistentVariantGraphEdge e : graph.traverseEdges(null)) {
        out.print(indent + "v" + e.getStart().getNode().getId() + connector + "v" + e.getEnd().getNode().getId());
        out.print(" [label = \"" + toLabel(e) + "\"]");
        out.println(";");
      }

      if (transpositions) {
        for (PersistentVariantGraphTransposition t : graph.getTranspositions()) {
          out.println(indent + "v" + t.getStart().getNode().getId() + connector + "v" + t.getEnd().getNode().getId() + ";");
        }
      }

      out.println("}");

      out.flush();
      tx.success();
    } finally {
      tx.finish();
    }
  }

  private String toLabel(PersistentVariantGraphEdge e) {
    return PersistentVariantGraphEdge.TO_CONTENTS.apply(e).replaceAll("\"", "\\\"");
  }

  private String toLabel(PersistentVariantGraphVertex v) {
    return PersistentVariantGraphVertex.TO_CONTENTS.apply(v).replaceAll("\"", "\\\"");
  }

  public boolean isSvgAvailable() {
    return (dotPath != null);
  }

  public void toSvg(PersistentVariantGraph vg, OutputStream out, boolean transpositions) throws IOException {
    Preconditions.checkState(isSvgAvailable());

    final FileBackedOutputStream dotBuf = new FileBackedOutputStream(102400);

    Writer dotWriter = null;
    try {
      toDot(vg, dotWriter = new OutputStreamWriter(dotBuf, Charset.defaultCharset()), transpositions);
    } finally {
      Closeables.close(dotWriter, false);
    }

    final Process dotProc = Runtime.getRuntime().exec(dotPath + " -Grankdir=LR -Gid=VariantGraph -Tsvg");
    final OutputStream dotStdin = new BufferedOutputStream(dotProc.getOutputStream());
    try {
      ByteStreams.copy(dotBuf.getSupplier(), dotStdin);
    } finally {
      Closeables.close(dotStdin, false);
      dotBuf.reset();
    }

    InputStream svgResult = null;
    final FileBackedOutputStream svgBuf = new FileBackedOutputStream(102400);
    try {
      ByteStreams.copy(svgResult = new BufferedInputStream(dotProc.getInputStream()), svgBuf);
    } finally {
      Closeables.close(svgBuf, false);
      Closeables.close(svgResult, false);
    }

    InputStream svgSource = null;
    try {
      if (dotProc.waitFor() == 0) {
        ByteStreams.copy(svgSource = svgBuf.getSupplier().getInput(), out);
        out.flush();
        return;
      }
    } catch (InterruptedException e) {
    } finally {
      Closeables.closeQuietly(svgSource);
      svgBuf.reset();
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    final File dotExecutable = new File(configuredDotPath);
    this.dotPath = (dotExecutable.canExecute() ? dotExecutable.getCanonicalPath() : null);
  }
}
