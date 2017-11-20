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

package eu.interedition.collatex.lab;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.DekkerAlgorithm;
import eu.interedition.collatex.dekker.legacy.MatchTableImpl;
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

    private final JComboBox<String> algorithm;
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
                new DekkerAlgorithm(comparator).collate(vg, witness);
            }

            SimpleWitness lastWitness = w.get(w.size() - 1);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Creating MatchTable for: {0}", lastWitness.getSigil());
            }
            matchMatrixTable.setModel(new MatchMatrixTableModel(MatchTableImpl.create(vg, lastWitness, comparator)));

            final TableColumnModel columnModel = matchMatrixTable.getColumnModel();
            columnModel.getColumn(0).setCellRenderer(matchMatrixTable.getTableHeader().getDefaultRenderer());
            for (int col = 1; col < matchMatrixTable.getColumnCount(); col++) {
                columnModel.getColumn(col).setCellRenderer(MATCH_MATRIX_CELL_RENDERER);
            }

            tabbedPane.setSelectedIndex(0);
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
            MatchTableCell cell = (MatchTableCell) value;
            MatchMatrixCellStatus status = cell.getStatus();

            switch (status) {
                case PREFERRED_MATCH:
                    label.setBackground(isSelected ? Color.GREEN : Color.GREEN.darker());
                    label.setText(cell.getText());
                    break;

                case OPTIONAL_MATCH:
                    label.setBackground(isSelected ? Color.YELLOW : Color.YELLOW.darker());
                    label.setText(cell.getText());
                    break;

                case EMPTY:
                    label.setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
                    label.setText("");
                    break;

                default:
                    label.setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
                    break;
            }

            return label;
        }
    };

}
