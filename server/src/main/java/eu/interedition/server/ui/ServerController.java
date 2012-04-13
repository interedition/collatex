package eu.interedition.server.ui;

import eu.interedition.server.ServerApplication;
import org.restlet.data.Protocol;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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
@org.springframework.stereotype.Component
public class ServerController extends AbstractAction implements InitializingBean, DisposableBean {
  private static final String START_LABEL = "Start Server";
  private static final String START_DESCRIPTION = "Starts the Interedition Server and opens its homepage in a browser";
  private static final String STOP_LABEL = "Stop Server";
  private static final String STOP_DESCRIPTION = "Shuts down the Interedition Server";

  @Autowired
  private TaskExecutor tasks;

  @Autowired
  private ServerSetupPanel setupPanel;

  @Autowired
  private ServerApplication application;

  @Autowired
  private ServerConsole console;

  private final Desktop desktop = Desktop.getDesktop();

  private org.restlet.Component component;

  public ServerController() {
    super(START_LABEL);
    putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/org/freedesktop/tango/16x16/apps/internet-web-browser.png"), "Browse Web"));
    putValue(Action.SHORT_DESCRIPTION, START_DESCRIPTION);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    component = new org.restlet.Component();
    component.getDefaultHost().attach(application);
  }

  @Override
  public void destroy() throws Exception {
    if (component.isStarted()) {
      component.stop();
    }
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    if (component.isStopped()) {
      start();
    } else {
      stop();
    }
  }

  /**
   * Boot the servlet container.
   */
  public void start() {
    if (component.isStarted()) {
      return;
    }
    final int port = setupPanel.getPort();

    final ServerOperationDialog dialog = new ServerOperationDialog(console, START_LABEL);
    setEnabled(false);

    tasks.execute(new Runnable() {
      @Override
      public void run() {
        component.getServers().clear();
        component.getServers().add(Protocol.HTTP, port);
        try {
          component.start();
          putValue(Action.NAME, STOP_LABEL);
          putValue(Action.SHORT_DESCRIPTION, STOP_DESCRIPTION);
        } catch (Exception e) {
          console.error(e, "Cannot start server");
        }
        setEnabled(true);
        dialog.setVisible(false);
      }
    });
    dialog.setVisible(true);

    if (component.isStarted()) {
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
            console.error(e, "Cannot browse URL");
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
    if (component.isStopped()) {
      return;
    }
    final ServerOperationDialog dialog = new ServerOperationDialog(console, STOP_LABEL);
    setEnabled(false);

    tasks.execute(new Runnable() {
      @Override
      public void run() {
        try {
          component.stop();
          putValue(Action.NAME, START_LABEL);
          putValue(Action.SHORT_DESCRIPTION, START_DESCRIPTION);
        } catch (Exception e) {
          console.error(e, "Cannot stop server");
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
