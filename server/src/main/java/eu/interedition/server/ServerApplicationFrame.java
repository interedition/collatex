package eu.interedition.server;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

/**
 * Main frame and entry point of the standalone server application.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ServerApplicationFrame extends JFrame {
  private static final Logger LOG = LoggerFactory.getLogger(ServerApplicationFrame.class.getPackage().getName());
  private static final ResourceBundle VERSIONS = ResourceBundle.getBundle("version");

  private static File dataDirectory;

  private final ExecutorService executorService = Executors.newCachedThreadPool();
  private final Preferences preferences = Preferences.userNodeForPackage(ServerApplicationFrame.class);
  private final ServletContainerSetupPanel setupPanel = new ServletContainerSetupPanel(this);

  /**
   * Sets up the UI of the frame and makes it visible.
   */
  public ServerApplicationFrame() {
    setTitle("Interedition Microservices");

    final JLabel logo = new JLabel(new ImageIcon(getClass().getResource("/eu/interedition/style/interedition_logo.png"), "Interedition-Logo"));
    logo.setBorder(BorderFactory.createLoweredBevelBorder());

    final JPanel logoPanel = new JPanel();
    logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
    logoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    logoPanel.add(logo);
    add(logoPanel, BorderLayout.WEST);

    add(setupPanel, BorderLayout.CENTER);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        exit();
      }
    });

    pack();
    setLocation(200, 200);
    setResizable(false);
    setVisible(true);
  }

  /**
   * Provides for asynchronous execution of tasks.
   *
   * @return a thread pool
   */
  public ExecutorService getExecutorService() {
    return executorService;
  }

  /**
   * Central preferences for the application.
   *
   * @return the preference node specific to this application
   */
  public Preferences getPreferences() {
    return preferences;
  }

  /**
   * The directory containing the JEE web application to be run by the server.
   * <p/>
   * It lives in the application's data directory.
   *
   * @return a file referring to the source directory of the hosted web application
   * @see WebApplicationDownloader
   */
  public static File getWebappArchive() {
    return new File(dataDirectory, "webapp-" + VERSIONS.getString("webapp.version"));
  }

  public static File[] getOutdatedWebAppArchives() {
    final File webappArchive = getWebappArchive();
    return dataDirectory.listFiles(new FileFilter() {

      @Override
      public boolean accept(File f) {
        if (!f.isDirectory()) {
          return false;
        }

        final String name = f.getName();
        return name.startsWith("webapp-") && (name.contains("SNAPSHOT") || !f.equals(webappArchive));
      }
    });
  }
  /**
   * Access to the setup panel and its settings.
   *
   * @return the panel instantiated by this frame
   */
  public ServletContainerSetupPanel getSetupPanel() {
    return setupPanel;
  }

  /**
   * Exits the application.
   * <p/>
   * Should a servlet container still be running, it is shut down beforehand.
   */
  public void exit() {
    setupPanel.getControllerAction().stop();
    setVisible(false);
    System.exit(0);
  }

  /**
   * Notifies the user about an unexpected error.
   * <p/>
   * The message is emitted via a dialog and the frame's logger.
   *
   * @param owner   parent frame of the error dialog or <code>null</code> in case of a non-modal dialog
   * @param t       the throwable being the cause of the error
   * @param message an optional message prepended to the throwables'
   */
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

  /**
   * Notifies the user about an unexpected error.
   * <p/>
   * The error dialog will be owned by this frame.
   *
   * @see #error(javax.swing.JFrame, Throwable, String)
   */
  public void error(Throwable t, String message) {
    error(this, t, message);
  }

  /**
   * Entry point to the application.
   * <p/>
   * Upon start, the security manager is set to <code>null</code>, so Java Web Start's sandbox does not interfere with
   * the servlet container's classloading.
   *
   * @param args command line arguments (ignored)
   */
  public static void main(String... args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
    }

    System.setSecurityManager(null);

    initDataDirectory();

    for (File outdated : getOutdatedWebAppArchives()) {
      try {
        LOG.info("Deleting outdated webapp {}", outdated);
        Files.deleteRecursively(outdated);
      } catch (IOException e) {
      }
    }

    final ServerApplicationFrame applicationFrame = new ServerApplicationFrame();

    applicationFrame.getExecutorService().execute(new WebApplicationDownloader(applicationFrame));
  }

  /**
   * Platform-dependent setup of the filesystem location where this application stores its data.
   * <p/>
   * Also exports the location's path as a system property (<code>interedition.data</code>), so it can be picked up by
   * the hosted web application.
   */
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


}
