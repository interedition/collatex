package eu.interedition.server.collatex;

import eu.interedition.collatex.graph.VariantGraph;
import org.restlet.representation.Representation;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface VariantGraphRepresentation {

  Representation forGraph(VariantGraph graph);
}
