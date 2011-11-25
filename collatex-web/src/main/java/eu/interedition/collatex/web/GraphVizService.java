package eu.interedition.collatex.web;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;
import eu.interedition.collatex.implementation.graph.JoinedVariantGraph;
import eu.interedition.collatex.implementation.graph.JoinedVariantGraphEdge;
import eu.interedition.collatex.implementation.graph.JoinedVariantGraphVertex;
import eu.interedition.collatex.interfaces.IVariantGraph;
import eu.interedition.collatex.interfaces.IWitness;
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

  public void toDot(IVariantGraph vg, Writer out) {
    JDOT_EXPORTER.export(out, JoinedVariantGraph.create(vg));
  }

  public boolean isSvgAvailable() {
    return (dotPath != null);
  }

  public void toSvg(IVariantGraph vg, OutputStream out) throws IOException {
    Preconditions.checkState(isSvgAvailable());

    final FileBackedOutputStream dotBuf = new FileBackedOutputStream(102400);

    Writer dotWriter = null;
    try {
      toDot(vg, dotWriter = new OutputStreamWriter(dotBuf, Charset.defaultCharset()));
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

  private static final VertexNameProvider<JoinedVariantGraphVertex> JVERTEX_ID_PROVIDER = new IntegerNameProvider<JoinedVariantGraphVertex>();

  private static final VertexNameProvider<JoinedVariantGraphVertex> JVERTEX_LABEL_PROVIDER = new VertexNameProvider<JoinedVariantGraphVertex>() {
    @Override
    public String getVertexName(JoinedVariantGraphVertex v) {
      return v.getNormalized();
    }
  };
  private static final EdgeNameProvider<JoinedVariantGraphEdge> JEDGE_LABEL_PROVIDER = new EdgeNameProvider<JoinedVariantGraphEdge>() {
    @Override
    public String getEdgeName(JoinedVariantGraphEdge e) {
      List<String> sigils = Lists.newArrayList();
      for (IWitness witness : e.getWitnesses()) {
        sigils.add(witness.getSigil());
      }
      Collections.sort(sigils);
      return Joiner.on(",").join(sigils);
    }
  };

  private static final DOTExporter<JoinedVariantGraphVertex, JoinedVariantGraphEdge> JDOT_EXPORTER = new DOTExporter<JoinedVariantGraphVertex, JoinedVariantGraphEdge>(//
          JVERTEX_ID_PROVIDER, JVERTEX_LABEL_PROVIDER, JEDGE_LABEL_PROVIDER //
  );
}
