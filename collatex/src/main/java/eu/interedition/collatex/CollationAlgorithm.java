package eu.interedition.collatex;

import eu.interedition.collatex.graph.VariantGraph;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface CollationAlgorithm {
  
  void collate(VariantGraph against, Iterable<Token> witness);
  
  void collate(VariantGraph against, Iterable<Token>... witnesses);

  void collate(VariantGraph against, List<Iterable<Token>> witnesses);

}
