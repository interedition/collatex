package eu.interedition.app;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ServerConfigurationPanel extends JPanel {

  private final ServerLaunchFrame frame;

  private final JTextField portTextField = new JTextField(5);
  private final JTextField dotPathTextField = new JTextField(40);
  private final JTextField serverUrl = new JTextField(40);

  private File dotPath;
  private int port = 7369;

  public ServerConfigurationPanel(ServerLaunchFrame frame) {
    super(new GridBagLayout());
    final GridBagConstraints gbc = new GridBagConstraints();
    this.frame = frame;

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.ipadx = 5;
    gbc.anchor = GridBagConstraints.LINE_END;
    add(new JLabel("Port:"), gbc);

    portTextField.setText(Integer.toString(port));
    gbc.gridx++;
    gbc.ipadx = 0;
    gbc.anchor = GridBagConstraints.LINE_START;
    add(portTextField, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.ipadx = 5;
    gbc.insets = new Insets(10, 0, 0, 0);
    gbc.anchor = GridBagConstraints.LINE_END;
    add(new JLabel("GraphViz dot path:"), gbc);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.LINE_START;
    add(dotPathTextField, gbc);

    gbc.gridx++;
    add(new JButton(new SelectDotPathAction()), gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.ipadx = 5;
    gbc.insets = new Insets(10, 0, 0, 0);
    gbc.anchor = GridBagConstraints.LINE_END;
    add(new JLabel("URL:"), gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.LINE_START;
    serverUrl.setEditable(false);
    serverUrl.setEnabled(false);
    serverUrl.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        serverUrl.selectAll();
      }

      @Override
      public void focusLost(FocusEvent e) {
        serverUrl.select(0, 0);
      }
    });
    add(serverUrl, gbc);
    
    gbc.gridx++;
    add(new JButton(new ServerLaunchAction(frame)), gbc);

    setBorder(BorderFactory.createTitledBorder("Server settings"));

    frame.getExecutorService().execute(new DotPathAutodetector());
  }

  public File getDotPath() {
    return dotPath;
  }

  public int getPort() {
    return port;
  }

  public void setServerUrl(final URI serverUrl) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (serverUrl == null) {
          ServerConfigurationPanel.this.serverUrl.setText("");
          ServerConfigurationPanel.this.serverUrl.setEnabled(false);
        } else {
          ServerConfigurationPanel.this.serverUrl.setText(serverUrl.toString());
          ServerConfigurationPanel.this.serverUrl.setEnabled(true);
        }
      }
    });
  }

  public void setDotPath(final File dotPath) {
    this.dotPath = dotPath;
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          dotPathTextField.setText(dotPath == null ? "" : dotPath.getCanonicalPath());
        } catch (IOException e) {
        }
      }
    });
  }

  private class SelectDotPathAction extends AbstractAction {

    private final JFileChooser fileChooser;

    private SelectDotPathAction() {
      super("Select...");
      putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/org/freedesktop/tango/16x16/actions/document-open.png"), "Open File"));

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

  private class DotPathAutodetector implements Runnable {
    @Override
    public void run() {
      String path = null;
      InputStream stream = null;
      try {
        final Process which = new ProcessBuilder("which", "dot").start();
        path = CharStreams.toString(new InputStreamReader(stream = which.getInputStream(), Charset.defaultCharset()));
        which.waitFor();
      } catch (IOException e) {
      } catch (InterruptedException e) {
      } finally {
        Closeables.closeQuietly(stream);
      }

      if (path == null || path.trim().length() == 0) {
        try {
          final Process where = new ProcessBuilder("where.exe", "dot.exe").start();
          path = CharStreams.toString(new InputStreamReader(stream = where.getInputStream(), Charset.defaultCharset()));
          where.waitFor();
        } catch (IOException e) {
        } catch (InterruptedException e) {
        } finally {
          Closeables.closeQuietly(stream);
        }

      }

      if (path == null || path.trim().length() == 0) {
        return;
      }

      final String[] paths = path.trim().split("[\r\n]+");
      path = (path.length() == 0 ? null : paths[0].trim());
      if (path == null) {
        return;
      }

      final File file = new File(path);
      setDotPath(file.canExecute() ? file : null);
    }
  }
}
