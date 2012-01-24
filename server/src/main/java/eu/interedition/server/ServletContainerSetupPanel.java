package eu.interedition.server;

import com.google.common.base.Strings;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.text.DecimalFormat;

/**
 * Panel with settings for the servlet container.
 * <p/>
 * The settings gathered from the user are:
 * <ol>
 * <li>the number of the {@link #getPort() TCP port} on which the servlet container will listen for HTTP requests</li>
 * <li>the {@link #getDotPath() path to GraphViz' <code>dot</code> executable}, which is used by the hosted web
 * application in order to create SVG-based renderings of variant graphs</li>
 * </ol>
 *
 * @see <a href="http://www.graphviz.org/">GraphViz</a>
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ServletContainerSetupPanel extends JPanel {

  private final ServerApplicationFrame frame;

  private final JFormattedTextField portTextField = new JFormattedTextField(new DecimalFormat("#####"));
  private final JTextField dotPathTextField = new JTextField(40);
  private final JTextField serverUrlTextField = new JTextField(40);
  private final ServletContainerControllerAction launchAction;

  /**
   * Constructor.
   * <p/>
   * Starts the autodetection of a GraphViz dot executable in case none is set.
   *
   * @param frame reference to the application's frame for access to other components
   *
   * @see GraphVizDotPathAutodetector
   */
  public ServletContainerSetupPanel(final ServerApplicationFrame frame) {
    super(new GridBagLayout());
    final GridBagConstraints gbc = new GridBagConstraints();
    this.frame = frame;

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.ipadx = 5;
    gbc.anchor = GridBagConstraints.LINE_END;
    add(new JLabel("Port:"), gbc);

    portTextField.setToolTipText("The TCP port on which the servlet container shall listen for HTTP requests");
    portTextField.setColumns(6);
    portTextField.setValue(Integer.parseInt(frame.getPreferences().get("port", "7369")));
    portTextField.addPropertyChangeListener("value", new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        frame.getPreferences().put("port", Integer.toString(getPort()));
      }
    });

    gbc.gridx++;
    gbc.ipadx = 0;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.LINE_START;
    add(portTextField, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.anchor = GridBagConstraints.LINE_END;
    add(new JLabel("GraphViz dot path:"), gbc);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.LINE_START;
    dotPathTextField.setToolTipText("<html>Path to GraphViz' <code>dot</code> executable. GraphViz is optionally used by the hosted web application in order to create SVG-based renderings of collation results</html>");
    dotPathTextField.setEditable(false);
    dotPathTextField.setText(frame.getPreferences().get("dotPath", ""));
    add(dotPathTextField, gbc);

    gbc.gridx++;
    add(new JButton(new SelectDotPathAction()), gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.anchor = GridBagConstraints.LINE_END;
    add(new JLabel("URL:"), gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.LINE_START;
    serverUrlTextField.setToolTipText("URL of the current server instance");
    serverUrlTextField.setEditable(false);
    serverUrlTextField.setEnabled(false);
    serverUrlTextField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        serverUrlTextField.selectAll();
      }

      @Override
      public void focusLost(FocusEvent e) {
        serverUrlTextField.select(0, 0);
      }
    });
    add(serverUrlTextField, gbc);
    
    gbc.gridx++;
    launchAction = new ServletContainerControllerAction(frame);
    add(new JButton(launchAction), gbc);

    setBorder(BorderFactory.createTitledBorder("Server settings"));

    if (getDotPath() == null) {
      frame.getExecutorService().execute(new GraphVizDotPathAutodetector(this));
    }
  }

  /**
   * Access to the action instance controlling a servlet container.
   *
   * @return the controller
   */
  public ServletContainerControllerAction getControllerAction() {
    return launchAction;
  }

  /**
   * The TCP port on which the servlet container shall listen for HTTP requests.
   *
   * @return the port number (&gt; 1)
   */
  public int getPort() {
    return Math.max(1, ((Number) portTextField.getValue()).intValue());
  }

  /**
   * Sets the URL of the server to be displayed in this panel.
   *
   * @param serverUrl the URL instance or <code>null</code> in case the server is not running
   */
  public void setServerUrl(final URI serverUrl) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (serverUrl == null) {
          ServletContainerSetupPanel.this.serverUrlTextField.setText("");
          ServletContainerSetupPanel.this.serverUrlTextField.setEnabled(false);
        } else {
          ServletContainerSetupPanel.this.serverUrlTextField.setText(serverUrl.toString());
          ServletContainerSetupPanel.this.serverUrlTextField.setEnabled(true);
        }
      }
    });
  }

  /**
   * The path to GraphViz' dot executable.
   *
   * @return the path or <code>null</code> in case it is not set
   *
   */
  public File getDotPath() {
    final String path = dotPathTextField.getText();
    return Strings.isNullOrEmpty(path) ? null : new File(path);
  }

  /**
   * Sets the path to GraphViz' dot executable.
   * <p/>
   * Performs validity checks and only sets the path if it refers to an executable.
   *
   * @param dotPath the path or <code>null</code> in case it is not set
   *
   */
  public void setDotPath(final File dotPath) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (dotPath == null || !dotPath.isFile() || !dotPath.canExecute()) {
          dotPathTextField.setText("");
        } else {
          dotPathTextField.setText(dotPath.getAbsolutePath());
          frame.getPreferences().put("dotPath", dotPath.getAbsolutePath());
        }
      }
    });
  }

  private class SelectDotPathAction extends AbstractAction {

    private final JFileChooser fileChooser;

    private SelectDotPathAction() {
      super("Select...");
      putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/org/freedesktop/tango/16x16/actions/document-open.png"), "Open File"));
      putValue(Action.SHORT_DESCRIPTION, "<html>Select path to <code>dot</code> executable</html>");

      fileChooser = new JFileChooser(System.getProperty("user.dir"));
      fileChooser.setDialogTitle("Select GraphViz dot path");
      fileChooser.setFileFilter(new FileFilter() {
        @Override
        public boolean accept(File f) {
          return f.isDirectory() || f.canExecute();
        }

        @Override
        public String getDescription() {
          return "Executable files";
        }
      });
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fileChooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (fileChooser.showDialog(frame, "Select") == JFileChooser.APPROVE_OPTION) {
        setDotPath(fileChooser.getSelectedFile());
      }
    }
  }

}
