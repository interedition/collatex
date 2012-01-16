package eu.interedition.server;

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
 * Boots/Shuts down the servlet container.
 * <p/>
 * The action performs a start/ shutdown depending on its current state. The bootstrapping and shutdown
 * run asynchronously with a progress dialog signalling the ongoing action to the user.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ServletContainerControllerAction extends AbstractAction {
  private static final String START_LABEL = "Start Server";
  private static final String START_DESCRIPTION = "Starts the Interedition Server and opens its homepage in a browser";
  private static final String STOP_LABEL = "Stop Server";
  private static final String STOP_DESCRIPTION = "Shuts down the Interedition Server";

  private final ServerApplicationFrame frame;
  private final Desktop desktop = Desktop.getDesktop();

  private Server server;

  /**
   * Constructor.
   *
   * @param frame the parent frame to which modal progress dialogs of this action bind
   */
  public ServletContainerControllerAction(ServerApplicationFrame frame) {
    super(START_LABEL);
    this.frame = frame;
    putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/org/freedesktop/tango/16x16/apps/internet-web-browser.png"), "Browse Web"));
    putValue(Action.SHORT_DESCRIPTION, START_DESCRIPTION);
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    if (server == null) {
      start();
    } else {
      stop();
    }
  }

  /**
   * Boot the servlet container.
   */
  public void start() {
    if (server != null) {
      return;
    }
    final File webappArchive = ServerApplicationFrame.getWebappArchive();
    if (!webappArchive.isDirectory()) {
      JOptionPane.showMessageDialog(frame, "The server code has not been downloaded yet.\nPlease ensure you have a connection to the Internet and restart the application in order to download it.", "Server code not available", JOptionPane.WARNING_MESSAGE);
      return;
    }

    final ServletContainerSetupPanel setupPanel = frame.getSetupPanel();
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
        server.setHandler(new WebAppContext(webappArchive.getAbsolutePath(), "/"));
        try {
          server.start();
          putValue(Action.NAME, STOP_LABEL);
          putValue(Action.SHORT_DESCRIPTION, STOP_DESCRIPTION);
        } catch (Exception e) {
          server = null;
          frame.error(e, "Cannot start server");
        }
        setEnabled(true);
        dialog.setVisible(false);
      }
    });
    dialog.setVisible(true);

    if (server != null) {
      try {
        String host;
        try {
          host = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
          host = "localhost";
        }

        if (desktop.isSupported(Desktop.Action.BROWSE)) {
          try {
            desktop.browse(new URI("http", null, host, port, "/", null, null));
          } catch (IOException e) {
            frame.error(e, "Cannot browse URL");
          }
        }
      } catch (URISyntaxException e) {
      }
    }
  }

  /**
   * Shuts down the servlet container.
   */
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
          server.stop();
          server.setStopAtShutdown(false);
          server = null;
          putValue(Action.NAME, START_LABEL);
          putValue(Action.SHORT_DESCRIPTION, START_DESCRIPTION);
        } catch (Exception e) {
          frame.error(e, "Cannot stop server");
        }
        setEnabled(true);
        dialog.setVisible(false);
      }
    });
    dialog.setVisible(true);
  }

  private class ServerOperationDialog extends JDialog {
    private ServerOperationDialog(Frame owner, String labelText) {
      super(owner, "Server Status", true);

      final JLabel label = new JLabel(labelText);
      label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      label.setHorizontalAlignment(JLabel.CENTER);
      add(label, BorderLayout.NORTH);

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
