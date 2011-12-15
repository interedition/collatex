package eu.interedition.collatex.implementation.graph;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.implementation.matching.Matches;
import eu.interedition.collatex.interfaces.ITokenLinker;
import eu.interedition.collatex.interfaces.IWitness;
import eu.interedition.collatex.interfaces.Token;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EditGraphLinker implements ITokenLinker {

  private final GraphFactory graphFactory;

  public EditGraphLinker(GraphFactory graphFactory) {
    this.graphFactory = graphFactory;
  }

  @Override
  public Map<Token, Token> link(IWitness superbase, IWitness b, Comparator<Token> comparator) {
    final EditGraph editGraph = graphFactory.newEditGraph();
    final Map<Token, Token> linkedTokens = editGraph.build(superbase, b, comparator).linkedTokens();
    graphFactory.delete(editGraph);
    return linkedTokens;
  }
}
