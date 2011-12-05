package eu.interedition.collatex.web;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;
import eu.interedition.collatex.implementation.graph.JoinedVariantGraph;
import eu.interedition.collatex.implementation.graph.JoinedVariantGraphEdge;
import eu.interedition.collatex.implementation.graph.JoinedVariantGraphVertex;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraph;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphEdge;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphTransposition;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphVertex;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IVariantGraph;
import eu.interedition.collatex.interfaces.IWitness;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

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

  }

  private String toLabel(PersistentVariantGraphEdge e) {
    return Joiner.on(", ").join(e.getWitnesses()).replaceAll("\"", "\\\"");
  }

  private String toLabel(PersistentVariantGraphVertex v) {
    final SortedSet<IWitness> witnesses = v.getWitnesses();
    if (witnesses.isEmpty()) {
      return "";
    }
    final SortedSet<INormalizedToken> tokens = v.getTokens(Sets.newTreeSet(Collections.singleton(witnesses.first())));
    return Joiner.on(" ").join(Iterables.transform(tokens, new Function<INormalizedToken, String>() {
      @Override
      public String apply(INormalizedToken input) {
        return input.getContent();
      }
    })).replaceAll("\"", "\\\"");
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
