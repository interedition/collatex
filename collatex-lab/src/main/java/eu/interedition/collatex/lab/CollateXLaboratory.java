package eu.interedition.collatex.lab;

import com.google.common.collect.Sets;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import eu.interedition.collatex.implementation.input.NormalizedToken;
import eu.interedition.collatex.implementation.input.Witness;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IWitness;

import javax.swing.*;
import java.util.Collections;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CollateXLaboratory extends JFrame {

  public CollateXLaboratory() {
    super("CollateX Laboratory");

    final VariantGraph g = new VariantGraph();

    final VariantGraphVertex hello = new VariantGraphVertex(Collections.<INormalizedToken>singletonList(new NormalizedToken(null, 0, "Hello World", "hello world")));
    g.addVertex(hello);

    final Witness w = new Witness("A");
    g.addEdge(new VariantGraphEdge(Sets.<IWitness>newTreeSet(Collections.singleton(w))), g.getStart(), hello);
    g.addEdge(new VariantGraphEdge(Sets.<IWitness>newTreeSet(Collections.singleton(w))), hello, g.getEnd());
    add(new VariantGraphPanel(g));

    setDefaultCloseOperation(EXIT_ON_CLOSE);
    pack();
  }

  public static void main(String[] args) {
    final CollateXLaboratory lab = new CollateXLaboratory();
    lab.setVisible(true);
  }
}
