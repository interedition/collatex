package eu.interedition.collatex.web;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;
import eu.interedition.collatex2.implementation.output.jgraph.IJVariantGraphEdge;
import eu.interedition.collatex2.implementation.output.jgraph.IJVariantGraphVertex;
import eu.interedition.collatex2.implementation.output.jgraph.JVariantGraphCreator;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;
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
    JDOT_EXPORTER.export(out, new JVariantGraphCreator().parallelSegmentate(vg));
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

  private static final VertexNameProvider<IJVariantGraphVertex> JVERTEX_ID_PROVIDER = new IntegerNameProvider<IJVariantGraphVertex>();

  private static final VertexNameProvider<IJVariantGraphVertex> JVERTEX_LABEL_PROVIDER = new VertexNameProvider<IJVariantGraphVertex>() {
    @Override
    public String getVertexName(IJVariantGraphVertex v) {
      return v.getNormalized();
    }
  };
  private static final EdgeNameProvider<IJVariantGraphEdge> JEDGE_LABEL_PROVIDER = new EdgeNameProvider<IJVariantGraphEdge>() {
    @Override
    public String getEdgeName(IJVariantGraphEdge e) {
      List<String> sigils = Lists.newArrayList();
      for (IWitness witness : e.getWitnesses()) {
        sigils.add(witness.getSigil());
      }
      Collections.sort(sigils);
      return Joiner.on(",").join(sigils);
    }
  };

  private static final DOTExporter<IJVariantGraphVertex, IJVariantGraphEdge> JDOT_EXPORTER = new DOTExporter<IJVariantGraphVertex, IJVariantGraphEdge>(//
          JVERTEX_ID_PROVIDER, JVERTEX_LABEL_PROVIDER, JEDGE_LABEL_PROVIDER //
  );
}
