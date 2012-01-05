package eu.interedition.app;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ServerLaunchFrame extends JFrame {
  private static final Logger LOG = LoggerFactory.getLogger(ServerLaunchFrame.class.getPackage().getName());
  private static ResourceBundle versions = ResourceBundle.getBundle("version");

  private final ExecutorService executorService = Executors.newCachedThreadPool();
  private final ServerSetupPanel setupPanel = new ServerSetupPanel(this);
  private static File dataDirectory;

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

    executorService.execute(new WebApplicationDownloader(this));
  }

  public ExecutorService getExecutorService() {
    return executorService;
  }

  public static File getDataDirectory() {
    return dataDirectory;
  }

  public static File getWebappArchive() {
    return new File(dataDirectory, "webapp-" + versions.getString("webapp.version"));
  }

  public ServerSetupPanel getSetupPanel() {
    return setupPanel;
  }

  public static void main(String... args) {
    System.setSecurityManager(null);
    initDataDirectory();
    new ServerLaunchFrame();
  }

  protected static void initDataDirectory() {
    final File userHome = new File(System.getProperty("user.home"));
    final String osName = System.getProperty("os.name").toLowerCase();
    if (osName.contains("mac os x")) {
      dataDirectory = new File(userHome, "Library/Application Support/Interedition");
    } else if (osName.contains("windows")) {
      dataDirectory = new File(userHome, "Application Data/Interedition");
    } else {
      dataDirectory = new File(userHome, ".interedition");
    }

    if (!dataDirectory.isDirectory() && !dataDirectory.mkdirs()) {
      error(null, new IllegalStateException("Cannot create data directory " + dataDirectory.getPath()), null);
      System.exit(1);
    }

    try {
      System.setProperty("interedition.data", dataDirectory.getCanonicalPath());
    } catch (IOException e) {
      error(null, e, "Cannot determine canonical path of " + dataDirectory.getPath());
      System.exit(1);
    }
  }

  public void error(Throwable t, String message) {
    error(this, t, message);
  }
  
  public static void error(JFrame owner, Throwable t, String message) {
    if (LOG.isErrorEnabled()) {
      LOG.error(message, t);
    }
    
    StringBuilder dialogMessage = new StringBuilder(t.getMessage());
    if (message != null) {
      dialogMessage.insert(0, message + ":\n");
    }
    JOptionPane.showMessageDialog(owner, dialogMessage, "Unexpected error", JOptionPane.ERROR_MESSAGE);
  }

  public void exit() {
    setupPanel.getControllerAction().stop();
    setVisible(false);
    System.exit(0);
  }

}
