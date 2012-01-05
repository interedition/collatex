package eu.interedition.app;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public class ServerControllerAction extends AbstractAction {
  private static final String BASE_DIRECTORY_PATH = System.getProperty("basedir");
  private static final String START_LABEL = "Start Server";
  private static final String STOP_LABEL = "Stop Server";

  private final ServerLaunchFrame frame;

  private Desktop desktop = Desktop.getDesktop();
  private Server server;
  private final File webApplicationRoot;

  public ServerControllerAction(ServerLaunchFrame frame) {
    super(START_LABEL);
    this.frame = frame;
    this.webApplicationRoot = (BASE_DIRECTORY_PATH == null ? null : new File(BASE_DIRECTORY_PATH, "web"));
    putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/org/freedesktop/tango/16x16/apps/internet-web-browser.png"), "Browse Web"));
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    if (server == null) {
      start();
    } else {
      stop();
    }
  }

  public void start() {
    if (server != null) {
      return;
    }

    final ServerSetupPanel setupPanel = frame.getSetupPanel();
    final int port = setupPanel.getPort();
    
    final File dotPath = setupPanel.getDotPath();
    if (dotPath != null) {
      try {
        System.setProperty("collatex.graphviz.dot", dotPath.getCanonicalPath());
      } catch (IOException e) {
      }
    }

    final ServerOperationDialog dialog = new ServerOperationDialog(frame, START_LABEL);
    setEnabled(false);

    frame.getExecutorService().execute(new Runnable() {
      @Override
      public void run() {
        final SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port);

        server = new Server();
        server.setStopAtShutdown(true);
        server.addConnector(connector);
        if (webApplicationRoot != null) {
          server.setHandler(new WebAppContext(webApplicationRoot.toURI().toString(), "/"));
        }
        try {
          server.start();
          putValue(Action.NAME, STOP_LABEL);
          setEnabled(true);
        } catch (Exception e) {
          server = null;
          frame.error(e, "Cannot start server");
        }
        dialog.setVisible(false);
      }
    });
    dialog.setVisible(true);

    if (server != null) {
      try {
        final URI serverUrl = new URI("http", null, getHostname(), port, "/", null, null);
        setupPanel.setServerUrl(serverUrl);
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
          try {
            desktop.browse(serverUrl);
          } catch (IOException e) {
            frame.error(e, "Cannot browse URL");
          }
        }
      } catch (URISyntaxException e) {
      }
    }
  }

  public void stop() {
    if (server == null) {
      return;
    }
    final ServerOperationDialog dialog = new ServerOperationDialog(frame, STOP_LABEL);
    setEnabled(false);

    frame.getExecutorService().execute(new Runnable() {
      @Override
      public void run() {
        try {
          frame.getSetupPanel().setServerUrl(null);
          server.stop();
          server.setStopAtShutdown(false);
          server = null;
          putValue(Action.NAME, START_LABEL);
          setEnabled(true);
        } catch (Exception e) {
          frame.error(e, "Cannot stop server");
        }
        dialog.setVisible(false);
      }
    });
    dialog.setVisible(true);
  }

  protected static String getHostname() {
    try {
      return InetAddress.getLocalHost().getCanonicalHostName();
    } catch (UnknownHostException e) {
      return "localhost";
    }
  }

  private class ServerOperationDialog extends JDialog {
    private ServerOperationDialog(Frame owner, String label) {
      super(owner, label, true);

      final JProgressBar progressBar = new JProgressBar();
      progressBar.setIndeterminate(true);
      progressBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      add(progressBar, BorderLayout.CENTER);
      
      pack();

      final Dimension size = getSize();
      final Point oLoc = owner.getLocation();
      final Dimension oSize = owner.getSize();
      setLocation(Math.max(oLoc.x + (oSize.width - size.width) / 2, oLoc.x), Math.max(oLoc.y + (oSize.height - size.height) / 2, oLoc.y));
    }
  }
}
