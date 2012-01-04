package eu.interedition.app;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.x509.IPAddressName;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public class ServerLaunchAction extends AbstractAction {
  private static final String BASE_DIRECTORY_PATH = System.getProperty("basedir");
  private static final String START_LABEL = "Start Server";
  private static final String STOP_LABEL = "Stop Server";

  private final ServerLaunchFrame frame;

  private Desktop desktop = Desktop.getDesktop();
  private Server server;
  private final File webApplicationRoot;

  public ServerLaunchAction(ServerLaunchFrame frame) {
    super(START_LABEL);
    this.frame = frame;
    this.webApplicationRoot = (BASE_DIRECTORY_PATH == null ? null : new File(BASE_DIRECTORY_PATH, "web"));
    putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/org/freedesktop/tango/16x16/apps/internet-web-browser.png"), "Browse Web"));
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    setEnabled(false);
    frame.getExecutorService().execute(new Runnable() {
      @Override
      public void run() {
        if (server == null) {
          final SelectChannelConnector connector = new SelectChannelConnector();
          connector.setPort(frame.getConfigurationPanel().getPort());

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

            final URI serverUrl = new URI("http", null, getHostname(), connector.getPort(), "/", null, null);
            frame.getConfigurationPanel().setServerUrl(serverUrl);
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
              try {
                desktop.browse(serverUrl);
              } catch (IOException e) {
                frame.error(e, "Cannot browse URL");
              }
            }
          } catch (Exception e) {
            frame.error(e, "Cannot start server");
          }


        } else {
          try {
            frame.getConfigurationPanel().setServerUrl(null);
            server.stop();
            server.setStopAtShutdown(false);
            server = null;
            putValue(Action.NAME, START_LABEL);
            setEnabled(true);
          } catch (Exception e) {
            frame.error(e, "Cannot stop server");
          }
        }
      }
    });
  }
  
  protected static String getHostname() {
    try {
      return InetAddress.getLocalHost().getCanonicalHostName();
    } catch (UnknownHostException e) {
      return "localhost";
    }
  }
}
