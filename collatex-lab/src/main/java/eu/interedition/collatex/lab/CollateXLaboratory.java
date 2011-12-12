package eu.interedition.collatex.lab;

import com.google.common.collect.Iterables;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import eu.interedition.collatex.implementation.alignment.VariantGraphBuilder;
import eu.interedition.collatex.implementation.graph.db.VariantGraphFactory;
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

  private final VariantGraphFactory variantGraphFactory;
  private final WitnessPanel witnessPanel = new WitnessPanel();
  private final VariantGraph variantGraph = new VariantGraph();
  private final VariantGraphPanel variantGraphPanel;

  public CollateXLaboratory(VariantGraphFactory variantGraphFactory) {
    super("CollateX Laboratory");
    this.variantGraphFactory = variantGraphFactory;

    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setContinuousLayout(true);
    splitPane.setLeftComponent(witnessPanel);
    splitPane.setRightComponent(variantGraphPanel = new VariantGraphPanel(variantGraph));
    add(splitPane, BorderLayout.CENTER);

    final JToolBar toolBar = new JToolBar();
    toolBar.setBorderPainted(true);
    toolBar.add(new AddWitnessAction());
    toolBar.add(new RemoveWitnessesAction());
    toolBar.add(new CollateAction());
    add(toolBar, BorderLayout.NORTH);

    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(800, 600);
  }

  public static void main(String[] args) throws Exception {
    new CollateXLaboratory(new VariantGraphFactory()).setVisible(true);
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
      final List<IWitness> witnesses = witnessPanel.getWitnesses();

      LOG.debug("Collating {}", Iterables.toString(witnesses));

      final Transaction transaction = variantGraphFactory.getDb().beginTx();
      try {
        final eu.interedition.collatex.implementation.graph.db.VariantGraph pvg = variantGraphFactory.create();
        new VariantGraphBuilder(pvg).add(witnesses.toArray(new IWitness[witnesses.size()]));

        variantGraph.update(pvg.join().rank());
        //variantGraph.update(pvg.rank());

        transaction.success();
      } finally {
        transaction.finish();
      }

      LOG.debug("Collated {}", Iterables.toString(witnesses));

      variantGraphPanel.setGraphLayout(new SugiyamaLayout<VariantGraphVertex, VariantGraphEdge>(variantGraph));
    }
  }
}
