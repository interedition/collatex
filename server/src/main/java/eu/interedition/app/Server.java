package eu.interedition.app;

import org.mortbay.jetty.nio.SelectChannelConnector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.Executors;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Server extends JFrame {

  public Server() {
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setTitle("Interedition Microservices");

    add(new JLabel(new ImageIcon(getClass().getResource("/eu/interedition/style/interedition_logo.png"), "Interedition-Logo")), BorderLayout.CENTER);
    add(new JButton(new ServerControlAction()), BorderLayout.SOUTH);

    pack();
    setVisible(true);
  }

  public static void main(String... args) {
    new Server();
  }

  private class ServerControlAction extends AbstractAction {

    private org.mortbay.jetty.Server server;

    private ServerControlAction() {
      super("Start Server");
    }

    @Override
    public void actionPerformed(ActionEvent event) {
      setEnabled(false);
      Executors.newSingleThreadExecutor().execute(new Runnable() {
        @Override
        public void run() {
          if (server == null) {
            final SelectChannelConnector connector = new SelectChannelConnector();
            connector.setPort(9090);

            server = new org.mortbay.jetty.Server();
            server.addConnector(connector);
            //server.setHandler(new WebAppContext(webappFile.toURI().toString(), CONTEXT_PATH));
            server.setStopAtShutdown(true);
            try {
              server.start();
              putValue(Action.NAME, "Stop Server");
              setEnabled(true);
            } catch (Exception e) {
            }
          } else {
            try {
              server.stop();
              server.setStopAtShutdown(false);
              server = null;
              putValue(Action.NAME, "Start Server");
              setEnabled(true);
            } catch (Exception e) {
            }
          }
        }
      });
    }
  }
}
