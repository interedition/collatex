package eu.interedition.server;

import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Detects instances of GraphViz dot executables.
 * <p/>
 * First it tries to run <code>which</code> looking for executables in the path on a UNIX-like system. If this does not
 * yield any results, it tries to run <code>where.exe</code> in order to locate dot executables in a Windows filesystem.
 *
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public class GraphVizDotPathAutodetector implements Runnable {

  private final Charset charset = Charset.defaultCharset();
  private final ServletContainerSetupPanel setupPanel;

  /**
   * Constructor.
   *
   * @param setupPanel the settings panel which receives the detected path
   */
  public GraphVizDotPathAutodetector(ServletContainerSetupPanel setupPanel) {
    this.setupPanel = setupPanel;
  }

  @Override
  public void run() {
    String path = null;
    InputStream stream = null;
    try {
      final Process which = new ProcessBuilder("which", "dot").start();
      path = CharStreams.toString(new InputStreamReader(stream = which.getInputStream(), charset)).trim();
      which.waitFor();
    } catch (IOException e) {
    } catch (InterruptedException e) {
    } finally {
      Closeables.closeQuietly(stream);
    }

    if (Strings.isNullOrEmpty(path)) {
      try {
        final Process where = new ProcessBuilder("where.exe", "dot.exe").start();
        path = CharStreams.toString(new InputStreamReader(stream = where.getInputStream(), charset)).trim();
        where.waitFor();
      } catch (IOException e) {
      } catch (InterruptedException e) {
      } finally {
        Closeables.closeQuietly(stream);
      }

    }

    if (Strings.isNullOrEmpty(path)) {
      return;
    }

    final String[] paths = path.split("[\r\n]+");
    setupPanel.setDotPath(new File(paths[0].trim()));
  }
}
