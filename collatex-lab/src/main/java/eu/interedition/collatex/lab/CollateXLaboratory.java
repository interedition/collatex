package eu.interedition.collatex.lab;

import com.google.common.collect.Iterables;
import eu.interedition.collatex.implementation.alignment.VariantGraphBuilder;
import eu.interedition.collatex.implementation.graph.GraphFactory;
import eu.interedition.collatex.implementation.graph.VariantGraph;
import eu.interedition.collatex.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex.interfaces.IWitness;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CollateXLaboratory extends JFrame {
  private static final Logger LOG = LoggerFactory.getLogger(CollateXLaboratory.class);
  public static final BasicStroke DASHED_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] { 5.0f }, 0.0f);
  public static final BasicStroke SOLID_STROKE = new BasicStroke(1.5f);

  private final GraphFactory graphFactory;
  private final WitnessPanel witnessPanel = new WitnessPanel();

  private final VariantGraphModel variantGraphModel = new VariantGraphModel();
  private final VariantGraphPanel variantGraphPanel;

  private final EditGraphModel editGraphModel = new EditGraphModel();
  private final EditGraphPanel editGraphPanel;

  public CollateXLaboratory(GraphFactory graphFactory) {
    super("CollateX Laboratory");
    this.graphFactory = graphFactory;

    final JSplitPane graphPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    graphPane.setContinuousLayout(true);
    graphPane.setLeftComponent(variantGraphPanel = new VariantGraphPanel(variantGraphModel));
    graphPane.setRightComponent(editGraphPanel = new EditGraphPanel(editGraphModel));
    graphPane.setDividerLocation(0.5f);

    final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPane.setContinuousLayout(true);
    splitPane.setLeftComponent(witnessPanel);
    splitPane.setRightComponent(graphPane);
    add(splitPane, BorderLayout.CENTER);


    final JToolBar toolBar = new JToolBar();
    toolBar.setBorderPainted(true);
    toolBar.add(new AddWitnessAction());
    toolBar.add(new RemoveWitnessesAction());
    toolBar.add(new CollateAction());
    toolBar.add(new TokenLinkAction());
    add(toolBar, BorderLayout.NORTH);

    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(800, 600);
    pack();

    graphPane.setDividerLocation(0.5f);
  }

  public static void main(String[] args) throws Exception {
    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    new CollateXLaboratory(new GraphFactory()).setVisible(true);
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
      final List<IWitness> w = witnessPanel.getWitnesses();

      LOG.debug("Collating {}", Iterables.toString(w));

      final Transaction transaction = graphFactory.getDatabase().beginTx();
      try {
        final VariantGraph pvg = graphFactory.newVariantGraph();
        new VariantGraphBuilder(pvg).add(w.toArray(new IWitness[w.size()]));

        variantGraphModel.update(pvg.join().rank());

        transaction.success();
      } finally {
        transaction.finish();
      }

      LOG.debug("Collated {}", Iterables.toString(w));

      variantGraphPanel.getModel().setGraphLayout(new SugiyamaLayout<VariantGraphVertexModel, VariantGraphEdgeModel>(variantGraphModel));
    }
  }

  private class TokenLinkAction extends AbstractAction {

    private TokenLinkAction() {
      super("Link");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final List<IWitness> w = witnessPanel.getWitnesses();

      if (w.size() < 2) {
        return;
      }

      final Transaction transaction = graphFactory.getDatabase().beginTx();
      try {
        final VariantGraph pvg = graphFactory.newVariantGraph();
        new VariantGraphBuilder(pvg).add(w.get(0));
        
        editGraphModel.update(graphFactory.newEditGraph(pvg).build(pvg, w.get(1).getTokens(), new EqualityTokenComparator()));
      } finally {
        transaction.finish();
      }

      editGraphPanel.getModel().setGraphLayout(new SugiyamaLayout<EditGraphVertexModel, EditGraphEdgeModel>(editGraphModel));
    }
  }
}
