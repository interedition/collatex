package eu.interedition.server.ui;

import eu.interedition.server.ServerApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@org.springframework.stereotype.Component
public class ServerConsole extends JFrame implements InitializingBean {
  private static final Logger LOG = LoggerFactory.getLogger(ServerConsole.class);

  @Autowired
  private ServerApplication application;

  @Autowired
  private ServerSetupPanel setupPanel;

  public ServerConsole() {
    super("Interedition Microservices");
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
  public static <T extends Throwable> T error(JFrame owner, T t, String message) {
    if (LOG.isErrorEnabled()) {
      LOG.error(message, t);
    }

    StringBuilder dialogMessage = new StringBuilder(t.getMessage());
    if (message != null) {
      dialogMessage.insert(0, message + ":\n");
    }
    JOptionPane.showMessageDialog(owner, dialogMessage, "Unexpected error", JOptionPane.ERROR_MESSAGE);

    return t;
  }

  /**
   * Notifies the user about an unexpected error.
   * <p/>
   * The error dialog will be owned by this frame.
   *
   * @see ServerConsole#error(javax.swing.JFrame, T, String)
   */
  public <T extends Throwable> T error(T t, String message) {
    return error(this, t, message);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    final JLabel logo = new JLabel(new ImageIcon(getClass().getResource("/eu/interedition/style/interedition_logo.png"), "Interedition-Logo"));
    logo.setBorder(BorderFactory.createLoweredBevelBorder());

    final JPanel logoPanel = new JPanel();
    logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
    logoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    logoPanel.add(logo);
    add(logoPanel, BorderLayout.WEST);

    add(setupPanel, BorderLayout.CENTER);

    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    pack();
    setLocation(200, 200);
    setResizable(false);
  }
}
