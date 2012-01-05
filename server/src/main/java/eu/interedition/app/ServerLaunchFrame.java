package eu.interedition.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ServerLaunchFrame extends JFrame {
  private static final Logger LOG = LoggerFactory.getLogger(ServerLaunchFrame.class.getPackage().getName());

  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private final ServerSetupPanel setupPanel = new ServerSetupPanel(this);

  public ServerLaunchFrame() {
    setTitle("Interedition Microservices");

    final JLabel logo = new JLabel(new ImageIcon(getClass().getResource("/eu/interedition/style/interedition_logo.png"), "Interedition-Logo"));
    logo.setBorder(BorderFactory.createLoweredBevelBorder());

    final JPanel logoPanel = new JPanel();
    logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
    logoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    logoPanel.add(logo);
    add(logoPanel, BorderLayout.WEST);

    add(setupPanel, BorderLayout.CENTER);

    pack();
    setLocation(200, 200);
    setResizable(false);
    setVisible(true);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        exit();
      }
    });
  }

  public ExecutorService getExecutorService() {
    return executorService;
  }

  public ServerSetupPanel getSetupPanel() {
    return setupPanel;
  }

  public static void main(String... args) {
    new ServerLaunchFrame();
  }

  public void error(Throwable t, String message) {
    if (LOG.isErrorEnabled()) {
      LOG.error(message, t);
    }
    
    StringBuilder dialogMessage = new StringBuilder(t.getMessage());
    if (message != null) {
      dialogMessage.insert(0, String.format("<b>%s</b><br><br>", message));
    }
    JOptionPane.showMessageDialog(this, t.getMessage(), "Unexpected error", JOptionPane.ERROR_MESSAGE);
  }

  public void exit() {
    setupPanel.getControllerAction().stop();

    setVisible(false);
    System.exit(0);
  }

}
