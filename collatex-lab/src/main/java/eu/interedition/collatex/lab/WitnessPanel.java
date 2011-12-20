package eu.interedition.collatex.lab;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.input.SimpleWitness;
import eu.interedition.collatex.input.WhitespaceTokenizer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class WitnessPanel extends JPanel {
  private static final char[] SIGLA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

  private List<WitnessTextArea> witnesses = Lists.newArrayListWithCapacity(SIGLA.length);

  public WitnessPanel() {
    super();
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    setMinimumSize(new Dimension(400, 200));
    createWitness();
    createWitness();
  }

  public void newWitness() {
    WitnessTextArea newWitness = null;
    for (WitnessTextArea witness : witnesses) {
      if (Strings.isNullOrEmpty(witness.getTextContent())) {
        newWitness = witness;
        break;
      }
    }
    if (newWitness == null && (witnesses.size() < SIGLA.length)) {
      newWitness = createWitness();
    }
    if (newWitness != null) {
      newWitness.requestFocusInWindow();
    }
  }

  public List<SimpleWitness> getWitnesses() {
    List<SimpleWitness> witnesses = Lists.newArrayListWithCapacity(this.witnesses.size());
    for (WitnessTextArea textArea : this.witnesses) {
      final String textContent = textArea.getTextContent();
      if (!Strings.isNullOrEmpty(textContent)) {
        witnesses.add(new SimpleWitness(textArea.getSigil(), textContent, new WhitespaceTokenizer()));
      }
    }
    return witnesses;
  }

  public void removeEmptyWitnesses() {
    for (Iterator<WitnessTextArea> textAreaIt = Lists.reverse(witnesses).iterator(); textAreaIt.hasNext() && witnesses.size() > 2; ) {
      final WitnessTextArea textArea = textAreaIt.next();
      if (Strings.isNullOrEmpty(textArea.getTextContent())) {
        remove(SwingUtilities.getAncestorOfClass(JScrollPane.class, textArea));
        textAreaIt.remove();
      }
    }
    revalidate();
  }

  protected WitnessTextArea createWitness() {
    final WitnessTextArea newTextArea = new WitnessTextArea();
    witnesses.add(newTextArea);
    add(new JScrollPane(newTextArea));
    revalidate();
    return newTextArea;
  }

  class WitnessTextArea extends JTextArea {

    private WitnessTextArea() {
      super("");
      setLineWrap(true);
      setWrapStyleWord(true);
      setOpaque(false);
      setMinimumSize(new Dimension(100, 100));
      setPreferredSize(new Dimension(200, 100));
      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      addKeyListener(new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() != KeyEvent.VK_TAB) {
            return;
          }
          e.consume();

          int nextIndex = witnesses.indexOf(WitnessTextArea.this) + (e.isShiftDown() ? -1 : 1);
          if (nextIndex < 0) {
            nextIndex = witnesses.size() - 1;
          } else if (nextIndex >= witnesses.size()) {
            nextIndex = 0;
          }
          witnesses.get(nextIndex).requestFocusInWindow();
        }
      });
      addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
          selectAll();
        }

        @Override
        public void focusLost(FocusEvent e) {
          select(0, 0);
        }
      });
    }

    public String getTextContent() {
      return getText().trim();
    }

    public String getSigil() {
      return Character.toString(SIGLA[witnesses.indexOf(this)]);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;

      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g2.setColor(Color.WHITE);
      g2.fillRect(0, 0, getWidth(), getHeight());

      g2.setColor(new Color(196, 196, 255));
      g2.setFont(g2.getFont().deriveFont(60.0f));

      final String sigil = getSigil();
      final Rectangle bounds = g2.getFontMetrics().getStringBounds(sigil, g2).getBounds();
      g2.drawString(sigil, (getWidth() - bounds.width) / 2, (int) bounds.getHeight());

      super.paintComponent(g);
    }
  }
}
