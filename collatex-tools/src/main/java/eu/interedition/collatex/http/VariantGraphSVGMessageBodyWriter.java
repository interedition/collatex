package eu.interedition.collatex.http;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;
import eu.interedition.collatex.VariantGraph;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Provider
@Produces("image/svg+xml")
public class VariantGraphSVGMessageBodyWriter implements MessageBodyWriter<VariantGraph> {

  final ExecutorService threadPool = Executors.newCachedThreadPool();
  final String dotPath;

  public VariantGraphSVGMessageBodyWriter(String dotPath) {
    this.dotPath = detectDot(dotPath);
  }


  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return VariantGraph.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(VariantGraph variantGraph, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(final VariantGraph variantGraph, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    if (dotPath == null) {
      throw new WebApplicationException(Response.Status.NO_CONTENT);
    }

    final Process dotProc = Runtime.getRuntime().exec(dotPath + " -Grankdir=LR -Gid=VariantGraph -Tsvg");

    final Future<Void> inputTask = threadPool.submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        new VariantGraphDotMessageBodyWriter().writeTo(variantGraph, type, genericType, annotations, mediaType, httpHeaders, dotProc.getOutputStream());
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
        ByteStreams.copy(svgSource = svgBuf.getSupplier().getInput(), entityStream);
      }
    } catch (InterruptedException e) {
    } finally {
      Closeables.closeQuietly(svgSource);
      svgBuf.reset();
      Closeables.close(entityStream, false);
    }
  }

  protected String detectDot(String dotPath) {
    if (dotPath != null && new File(dotPath).canExecute()) {
      return dotPath;
    }

    dotPath = null;
    InputStream stream = null;
    try {
      final Process which = new ProcessBuilder("which", "dot").start();
      dotPath = CharStreams.toString(new InputStreamReader(stream = which.getInputStream(), Charset.defaultCharset())).trim();
      which.waitFor();
    } catch (IOException e) {
    } catch (InterruptedException e) {
    } finally {
      Closeables.closeQuietly(stream);
    }

    if (Strings.isNullOrEmpty(dotPath)) {
      try {
        final Process where = new ProcessBuilder("where.exe", "dot.exe").start();
        dotPath = CharStreams.toString(new InputStreamReader(stream = where.getInputStream(), Charset.defaultCharset())).trim();
        where.waitFor();
      } catch (IOException e) {
      } catch (InterruptedException e) {
      } finally {
        Closeables.closeQuietly(stream);
      }
    }

    if (!Strings.isNullOrEmpty(dotPath)) {
      dotPath = dotPath.split("[\r\n]+")[0].trim();
    }

    return (Strings.isNullOrEmpty(dotPath) ? null : dotPath);
  }
}
