package eu.interedition.server.collatex;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;
import eu.interedition.collatex.graph.VariantGraph;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VariantGraphSVGRepresentation extends OutputRepresentation implements VariantGraphRepresentation {

  @Autowired
  private VariantGraphConverter converter;

  @Autowired
  TaskExecutor taskExecutor;

  @Autowired
  private VariantGraphvizDotRepresentation dotRepresentation;

  private VariantGraph graph;

  public VariantGraphSVGRepresentation() {
    super(MediaType.IMAGE_SVG);
  }

  public VariantGraphSVGRepresentation forGraph(VariantGraph graph) {
    this.graph = graph;
    return this;
  }

  @Transactional
  @Override
  public void write(WritableByteChannel writableChannel) throws IOException {
    super.write(writableChannel);
  }

  @Transactional
  @Override
  public void write(Writer writer) throws IOException {
    super.write(writer);
  }

  @Transactional
  @Override
  public void write(OutputStream out) throws IOException {
    Preconditions.checkState(converter.isSvgAvailable());

    final Process dotProc = Runtime.getRuntime().exec(converter.dotPath + " -Grankdir=LR -Gid=VariantGraph -Tsvg");

    final Future<Void> inputTask = new ExecutorServiceAdapter(taskExecutor).submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        Writer dotWriter = null;
        try {
          dotRepresentation.forGraph(graph).write(dotWriter = new OutputStreamWriter(dotProc.getOutputStream(), Charset.forName("UTF-8")));
        } finally {
          Closeables.close(dotWriter, false);
        }

        return null;
      }
    });

    InputStream svgResult = null;
    final FileBackedOutputStream svgBuf = new FileBackedOutputStream(102400);
    try {
      ByteStreams.copy(svgResult = new BufferedInputStream(dotProc.getInputStream()), svgBuf);
    } finally {
      Closeables.close(svgBuf, false);
      Closeables.close(svgResult, false);
    }

    try {
      inputTask.get();
    } catch (InterruptedException e) {
    } catch (ExecutionException e) {
      Throwables.propagateIfInstanceOf(e.getCause(), IOException.class);
      throw Throwables.propagate(e);
    }

    InputStream svgSource = null;
    try {
      if (dotProc.waitFor() == 0) {
        ByteStreams.copy(svgSource = svgBuf.getSupplier().getInput(), out);
        out.flush();
      }
    } catch (InterruptedException e) {
    } finally {
      Closeables.closeQuietly(svgSource);
      svgBuf.reset();
    }
  }
}
