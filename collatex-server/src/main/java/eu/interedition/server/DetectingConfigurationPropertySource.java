package eu.interedition.server;

import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class DetectingConfigurationPropertySource extends EnumerablePropertySource<Object> {

  private static final String[] PROPERTY_NAMES = new String[] { "interedition.data", "interedition.dot" };
  private static final Logger LOGGER = LoggerFactory.getLogger(DetectingConfigurationPropertySource.class);

  public DetectingConfigurationPropertySource(String name) {
    super(name, new Object());
  }

  @Override
  public String[] getPropertyNames() {
    return PROPERTY_NAMES;
  }

  @Override
  public Object getProperty(String name) {
    if ("interedition.data".equals(name)) {
      final File userHome = new File(System.getProperty("user.home"));
      final String osName = System.getProperty("os.name").toLowerCase();

      String dataDirectoryPath;
      if (osName.contains("mac os x")) {
        dataDirectoryPath = new File(userHome, "Library/Application Support/Interedition").getAbsolutePath();
      } else if (osName.contains("windows")) {
        dataDirectoryPath = new File(userHome, "Application Data/Interedition").getAbsolutePath();
      } else {
        dataDirectoryPath = new File(userHome, ".interedition").getAbsolutePath();
      }

      final File dataDirectory = new File(dataDirectoryPath);
      if (!dataDirectory.isDirectory() && !dataDirectory.mkdirs()) {
        if (LOGGER.isWarnEnabled()) {
          LOGGER.warn("Cannot create data directory " + dataDirectory.getPath());
        }
        return null;
      }
      return dataDirectory;
    } else if ("interedition.dot".equals(name)) {
      InputStream stream = null;
      String dotPath = null;
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
    return null;
  }
}
