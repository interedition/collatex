package eu.interedition.collatex.lab;

import static eu.interedition.collatex.CollationAlgorithmFactory.*;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.MatrixLinker.MatchMatrixLinker;
import eu.interedition.collatex.graph.GraphFactory;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.suffixtree.SuffixTree;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CollateXLaboratory extends JFrame {
  private static final Logger     LOG               = LoggerFactory.getLogger(CollateXLaboratory.class);
  public static final BasicStroke DASHED_STROKE     = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] { 5.0f }, 0.0f);
  public static final BasicStroke SOLID_STROKE      = new BasicStroke(1.5f);

  private final GraphFactory      graphFactory;
  private final WitnessPanel      witnessPanel      = new WitnessPanel();

  private final VariantGraphModel variantGraphModel = new VariantGraphModel();
  private final VariantGraphPanel variantGraphPanel;

  private final EditGraphModel    editGraphModel    = new EditGraphModel();
  private final EditGraphPanel    editGraphPanel;

  private final JTable            matchMatrixTable  = new JTable();

  private final SuffixTreePanel   suffixTreePanel;

  private final JComboBox         algorithm;
  private final JTabbedPane       tabbedPane;

  public CollateXLaboratory(GraphFactory graphFactory) {
    super("CollateX Laboratory");
    this.graphFactory = graphFactory;

    this.algorithm = new JComboBox(new Object[] { "Dekker", "Needleman-Wunsch" });
    this.algorithm.setEditable(false);
    this.algorithm.setFocusable(false);
    this.algorithm.setMaximumSize(new Dimension(200, this.algorithm.getMaximumSize().height));

    this.tabbedPane = new JTabbedPane();
    this.tabbedPane.addTab("Variant Graph", variantGraphPanel = new VariantGraphPanel(variantGraphModel));
    this.tabbedPane.addTab("Edit Graph", editGraphPanel = new EditGraphPanel(editGraphModel));
    this.tabbedPane.addTab("Suffix Tree", suffixTreePanel = new SuffixTreePanel());
    this.tabbedPane.addTab("Match Matrix", new JScrollPane(matchMatrixTable));
    matchMatrixTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    matchMatrixTable.setShowGrid(true);
    matchMatrixTable.setGridColor(new Color(0, 0, 0, 32));
    matchMatrixTable.setColumnSelectionAllowed(true);

    final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPane.setContinuousLayout(true);
    splitPane.setLeftComponent(witnessPanel);
    splitPane.setRightComponent(tabbedPane);
    add(splitPane, BorderLayout.CENTER);

    final JToolBar toolBar = new JToolBar();
    toolBar.setBorderPainted(true);
    toolBar.add(algorithm);
    toolBar.addSeparator();
    toolBar.add(new AddWitnessAction());
    toolBar.add(new RemoveWitnessesAction());
    toolBar.add(new CollateAction());
    toolBar.add(new TokenLinkAction());
    toolBar.add(new SuffixTreeAction());
    toolBar.add(new MatchMatrixAction());
    add(toolBar, BorderLayout.NORTH);

    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(800, 600);
    pack();

    splitPane.setDividerLocation(0.3f);
  }

  public static void main(String[] args) throws Exception {
    new CollateXLaboratory(GraphFactory.create()).setVisible(true);
  }

  private class AddWitnessAction extends AbstractAction {

    private AddWitnessAction() {
      super("Add");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      witnessPanel.newWitness();
    }
  }

  private class RemoveWitnessesAction extends AbstractAction {

    private RemoveWitnessesAction() {
      super("Remove");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      witnessPanel.removeEmptyWitnesses();
    }
  }

  private class CollateAction extends AbstractAction {

    private CollateAction() {
      super("Collate");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final List<SimpleWitness> w = witnessPanel.getWitnesses();

      LOG.debug("Collating {}", Iterables.toString(w));

      Transaction transaction = graphFactory.getDatabase().beginTx();
      try {
        final EqualityTokenComparator comparator = new EqualityTokenComparator();
        final VariantGraph pvg = graphFactory.newVariantGraph();

        final CollationAlgorithm collator = "Dekker".equals(algorithm.getSelectedItem()) ? dekker(comparator) : needlemanWunsch(comparator);
        for (SimpleWitness witness : w) {
          collator.collate(pvg, witness);
        }

        variantGraphModel.update(pvg.join().rank());

        transaction.success();
      } finally {
        transaction.finish();
      }

      LOG.debug("Collated {}", Iterables.toString(w));

      variantGraphPanel.getModel().setGraphLayout(new SugiyamaLayout<VariantGraphVertexModel, VariantGraphEdgeModel>(variantGraphModel));
      tabbedPane.setSelectedIndex(0);
    }
  }

  private class TokenLinkAction extends AbstractAction {

    private TokenLinkAction() {
      super("Link");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final List<SimpleWitness> w = witnessPanel.getWitnesses();

      if (w.size() < 2) {
        return;
      }

      final Transaction transaction = graphFactory.getDatabase().beginTx();
      try {
        final EqualityTokenComparator comparator = new EqualityTokenComparator();
        final VariantGraph pvg = graphFactory.newVariantGraph();

        CollationAlgorithmFactory.dekker(comparator).collate(pvg, w.get(0));

        editGraphModel.update(graphFactory.newEditGraph(pvg).build(pvg, w.get(1), comparator));
      } finally {
        transaction.finish();
      }

      editGraphPanel.getModel().setGraphLayout(new SugiyamaLayout(editGraphModel));
      tabbedPane.setSelectedIndex(1);
    }
  }

  private class SuffixTreeAction extends AbstractAction {

    private SuffixTreeAction() {
      super("Suffix Tree");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final List<SimpleWitness> w = witnessPanel.getWitnesses();

      if (w.size() < 1) {
        return;
      }

      final SimpleWitness witness = w.get(0);
      tabbedPane.setSelectedIndex(2);
      final SuffixTreeModel treeModel = new SuffixTreeModel(SuffixTree.create(witness, new EqualityTokenComparator()));
      suffixTreePanel.getModel().setGraphLayout(new TreeLayout(treeModel, 100, 50));
    }
  }

  private class MatchMatrixAction extends AbstractAction {

    private MatchMatrixAction() {
      super("Match Matrix");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final List<SimpleWitness> w = witnessPanel.getWitnesses();

      if (w.size() < 2) {
        return;
      }

      final Transaction transaction = graphFactory.getDatabase().beginTx();
      try {
        final EqualityTokenComparator comparator = new EqualityTokenComparator();
        final VariantGraph vg = graphFactory.newVariantGraph();

        CollationAlgorithmFactory.dekker(comparator).collate(vg, w.get(0));
        final SimpleWitness witness = w.get(1);

        matchMatrixTable.setModel(new MatchMatrixTableModel(MatchMatrixLinker.buildMatrix(vg, witness, comparator), vg, witness));

        final TableColumnModel columnModel = matchMatrixTable.getColumnModel();
        columnModel.getColumn(0).setCellRenderer(matchMatrixTable.getTableHeader().getDefaultRenderer());
        for (int col = 1; col < matchMatrixTable.getColumnCount(); col++) {
          columnModel.getColumn(col).setCellRenderer(MATCH_MATRIX_CELL_RENDERER);
        }

        tabbedPane.setSelectedIndex(3);
      } finally {
        transaction.finish();
      }
    }
  }

  private static final TableCellRenderer MATCH_MATRIX_CELL_RENDERER = new TableCellRenderer() {
                                                                      private JLabel label;

                                                                      @Override
                                                                      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                                                                        if (label == null) {
                                                                          label = new JLabel();
                                                                          label.setOpaque(true);
                                                                          label.getInsets().set(5, 5, 5, 5);
                                                                        }
                                                                        switch ((Status) value) {
                                                                          case PREFERRED_MATCH:
                                                                            label.setBackground(isSelected ? Color.GREEN : Color.GREEN.darker());
                                                                            break;

                                                                          case OPTIONAL_MATCH:
                                                                            label.setBackground(isSelected ? Color.YELLOW : Color.YELLOW.darker());
                                                                            break;

                                                                          case EMPTY:
                                                                            label.setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
                                                                            break;

                                                                          default:
                                                                            label.setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
                                                                            break;
                                                                        }

                                                                        return label;
                                                                      }
                                                                    };

}
