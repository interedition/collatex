package eu.interedition.server.ui;

import com.google.common.base.Strings;
import eu.interedition.server.collatex.VariantGraphConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;

/**
 * Panel with settings for the HTTP server.
 * <p/>
 * The settings gathered from the user are:
 * <ol>
 * <li>the number of the {@link #getPort() TCP port} on which the server will listen for HTTP requests</li>
 * <li>the {@link #getDotPath() path to GraphViz' <code>dot</code> executable}, which is used by the hosted web
 * application in order to create SVG-based renderings of variant graphs</li>
 * </ol>
 *
 * @see <a href="http://www.graphviz.org/">GraphViz</a>
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@org.springframework.stereotype.Component
public class ServerSetupPanel extends JPanel implements InitializingBean {

  private final JFormattedTextField portTextField = new JFormattedTextField(new DecimalFormat("#####"));
  private final JTextField dotPathTextField = new JTextField(30);

  @Autowired
  private ServerController controller;

  @Autowired
  private ServerConsole console;

  @Autowired
  private Preferences preferences;

  @Autowired
  private VariantGraphConverter variantGraphConverter;

  @Autowired
  private TaskExecutor tasks;

  @Override
  public void afterPropertiesSet() throws Exception {
    setLayout(new GridBagLayout());
    final GridBagConstraints gbc = new GridBagConstraints();

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.ipadx = 5;
    gbc.anchor = GridBagConstraints.LINE_END;
    add(new JLabel("Port:"), gbc);

    portTextField.setToolTipText("The TCP port on which the server shall listen for HTTP requests");
    portTextField.setColumns(6);
    portTextField.setValue(Integer.parseInt(preferences.get("port", "7369")));
    portTextField.addPropertyChangeListener("value", new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        preferences.put("port", Integer.toString(getPort()));
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
    add(dotPathTextField, gbc);

    gbc.gridx++;
    add(new JButton(new SelectDotPathAction()), gbc);

    gbc.gridx = 1;
    gbc.gridy++;
    add(new JButton(controller), gbc);

    setBorder(BorderFactory.createTitledBorder("Server settings"));

    setDotPath(new File(preferences.get("dotPath", "")));
    if (getDotPath() == null) {
      tasks.execute(new GraphVizDotPathAutodetector(this));
    }
  }

  /**
   * The TCP port on which the server shall listen for HTTP requests.
   *
   * @return the port number (&gt; 1)
   */
  public int getPort() {
    return Math.max(1, ((Number) portTextField.getValue()).intValue());
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
          variantGraphConverter.setDotPath(null);
        } else {
          dotPathTextField.setText(dotPath.getAbsolutePath());
          preferences.put("dotPath", dotPath.getAbsolutePath());
          variantGraphConverter.setDotPath(dotPath);
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
      if (fileChooser.showDialog(console, "Select") == JFileChooser.APPROVE_OPTION) {
        setDotPath(fileChooser.getSelectedFile());
      }
    }
  }

}
