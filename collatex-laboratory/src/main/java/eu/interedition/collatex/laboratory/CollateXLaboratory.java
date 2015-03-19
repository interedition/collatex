/*
 * Copyright (c) 2015 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.laboratory;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.matching.StrictEqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 * @author Bram Buitendijk
 * @author Ronald Haentjens Dekker
 */
@SuppressWarnings("serial")
public class CollateXLaboratory extends JFrame {
    private static final Logger LOG = Logger.getLogger(CollateXLaboratory.class.getName());
    public static final BasicStroke DASHED_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{5.0f}, 0.0f);
    public static final BasicStroke SOLID_STROKE = new BasicStroke(1.5f);

    private final WitnessPanel witnessPanel = new WitnessPanel();

    private final JTable matchMatrixTable = new JTable();

    private final JComboBox algorithm;
    private final JTabbedPane tabbedPane;

    public CollateXLaboratory() {
        super("CollateX Laboratory");

        this.algorithm = new JComboBox<>(new String[]{"Dekker", "Needleman-Wunsch", "Greedy String Tiling", "MEDITE"});
        this.algorithm.setEditable(false);
        this.algorithm.setFocusable(false);
        this.algorithm.setMaximumSize(new Dimension(200, this.algorithm.getMaximumSize().height));

        this.tabbedPane = new JTabbedPane();
        this.tabbedPane.addTab("Match Table", new JScrollPane(matchMatrixTable));
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
        toolBar.add(new MatchMatrixAction());
        add(toolBar, BorderLayout.NORTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        final Dimension screenSize = getToolkit().getScreenSize();
        setSize(Math.max(800, screenSize.width - 200), Math.max(600, screenSize.height - 100));

        final Dimension frameSize = getSize();
        setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

        splitPane.setDividerLocation(0.3f);

        // hello world example for JGraphX
        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();

        graph.getModel().beginUpdate();
        try
        {
            Object v1 = graph.insertVertex(parent, null, "Hello", 20, 20, 80, 30);
            Object v2 = graph.insertVertex(parent, null, "World!", 240, 150, 80, 30);
            graph.insertEdge(parent, null, "Edge", v1, v2);
        }
        finally
        {
            graph.getModel().endUpdate();
        }

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        //NOTE: wrap in JScrollPane?
        this.tabbedPane.addTab("Decision graph", graphComponent);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new CollateXLaboratory().setVisible(true);
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

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Collating {0}", w.toString());
            }


            final EqualityTokenComparator comparator = new EqualityTokenComparator();
            final VariantGraph variantGraph = new VariantGraph();

            final CollationAlgorithm collator;
            if ("Dekker".equals(algorithm.getSelectedItem())) {
                collator = CollationAlgorithmFactory.dekker(comparator);
            } else if ("Needleman-Wunsch".equals(algorithm.getSelectedItem())) {
                collator = CollationAlgorithmFactory.needlemanWunsch(comparator);
            } else if ("Greedy String Tiling".equals(algorithm.getSelectedItem())) {
                collator = CollationAlgorithmFactory.greedyStringTiling(comparator, 2);
            } else {
                collator = CollationAlgorithmFactory.medite(comparator, SimpleToken.TOKEN_MATCH_EVALUATOR);
            }

            collator.collate(variantGraph, w);

            VariantGraph.JOIN.apply(variantGraph);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Collated {0}", w.toString());
            }
        }
    }

    private class MatchMatrixAction extends AbstractAction {

        private MatchMatrixAction() {
            super("Match Table");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final List<SimpleWitness> w = witnessPanel.getWitnesses();

            if (w.size() < 2) {
                return;
            }

            final StrictEqualityTokenComparator comparator = new StrictEqualityTokenComparator();
            final VariantGraph vg = new VariantGraph();

            int outlierTranspositionsSizeLimit = 3;
            for (int i = 0; i <= w.size() - 2; i++) {
                SimpleWitness witness = w.get(i);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Collating: {0}", witness.getSigil());
                }
                CollationAlgorithmFactory.dekkerMatchMatrix(comparator, outlierTranspositionsSizeLimit).collate(vg, witness);
            }

            SimpleWitness lastWitness = w.get(w.size() - 1);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Creating MatchTable for: {0}", lastWitness.getSigil());
            }

            tabbedPane.setSelectedIndex(0);
        }
    }
}

