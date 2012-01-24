package eu.interedition.server;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads the web application archive if it is not available on the local machine yet.
 * <p/>
 * The archive is downloaded from where the application was (Java Web Start's codebase) and unpacked into the
 * application's data directory.
 *
 * @see eu.interedition.server.ServerApplicationFrame#getWebappArchive()
 * @see javax.jnlp.BasicService#getCodeBase()
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class WebApplicationDownloader implements Runnable {

  private final ServerApplicationFrame frame;

  /**
   * Constructor.
   *
   * @param frame the application's frame for lookup of the web application's location
   */
  public WebApplicationDownloader(ServerApplicationFrame frame) {
    this.frame = frame;
  }

  @Override
  public void run() {
    final File webappArchive = ServerApplicationFrame.getWebappArchive();
    if (webappArchive.isDirectory()) {
      return;
    }

    try {
      final BasicService bs = (BasicService) ServiceManager.lookup(BasicService.class.getName());
      final URI webapp = bs.getCodeBase().toURI().resolve(webappArchive.getName() + ".war");

      if (!webappArchive.mkdirs()) {
        frame.error(new IllegalStateException(webappArchive.getPath() + " could not be created"), null);
        return;
      }

      InputStream netStream = null;
      OutputStream fileStream = null;
      boolean downloadCompleted = false;
      try {
        netStream = new ProgressMonitorInputStream(frame, "Downloading server code...", webapp.toURL().openStream());
        final ZipInputStream zipStream = new ZipInputStream(netStream);
        while (true) {
          ZipEntry currentEntry = zipStream.getNextEntry();
          if (currentEntry == null) {
            break;
          }
          if (currentEntry.isDirectory()) {
            new File(webappArchive, currentEntry.getName()).mkdirs();
          } else {
            try {
              fileStream = new FileOutputStream(new File(webappArchive, currentEntry.getName()));
              ByteStreams.copy(zipStream, fileStream);
            } finally {
              Closeables.closeQuietly(fileStream);
            }
          }
        }
        downloadCompleted = true;
      } catch (InterruptedIOException e) {
      } catch (IOException e) {
        frame.error(e, "I/O error while downloading " + webapp.toString());
      } finally {
        Closeables.closeQuietly(fileStream);
        Closeables.closeQuietly(netStream);
      }

      if (!downloadCompleted && webappArchive.isDirectory()) {
        try {
          Files.deleteRecursively(webappArchive);
        } catch (IOException e) {
        }
      }
    } catch (UnavailableServiceException e) {
      frame.error(e, "Cannot locate the Java Web Start runtime");
    } catch (URISyntaxException e) {
      frame.error(e, "Invalid application base URL");
    }
  }
}
