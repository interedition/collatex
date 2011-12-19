package eu.interedition.collatex.graph;

import eu.interedition.collatex.ITokenLinker;
import eu.interedition.collatex.Token;

import java.util.Comparator;
import java.util.Map;

public class EditGraphLinker implements ITokenLinker {

  private final GraphFactory graphFactory;

  public EditGraphLinker(GraphFactory graphFactory) {
    this.graphFactory = graphFactory;
  }

  @Override
  public Map<Token, VariantGraphVertex> link(VariantGraph base, Iterable<Token> witness, Comparator<Token> comparator) {
    final EditGraph editGraph = graphFactory.newEditGraph(base);
    final Map<Token, VariantGraphVertex> linkedTokens = editGraph.build(base, witness, comparator).linkedTokens();
    graphFactory.delete(editGraph);
    return linkedTokens;
  }
}
