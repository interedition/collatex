package eu.interedition.collatex.dekker.suffix;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.jung.JungVariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;
/*
 * Class representing a fluent interface for order independent collation.
 * 
 * @author: Ronald Haentjens Dekker
 */
public class Collation {
  private final List<SimpleWitness> witnesses;
  private final Comparator<Token> comparator;
  
  private Collation() {
    witnesses = Lists.newArrayList();
    comparator = new EqualityTokenComparator();
  }
  
  public static Collation create() {
    return new Collation();
  }

  public Collation addWitness(String witness) {
    SimpleWitness w = new SimpleWitness("id", witness);
    witnesses.add(w);
    return this;
  }

  public List<Block> getBlocks() {
    DekkerOrderIndependentAlgorithm algorithm = new DekkerOrderIndependentAlgorithm(comparator);
    JungVariantGraph graph = new JungVariantGraph();
    algorithm.collate(graph, witnesses);
    return algorithm.getBlocks();
  }

  public List<BlockWitness> getBlockWitnesses() {
    DekkerOrderIndependentAlgorithm algorithm = new DekkerOrderIndependentAlgorithm(comparator);
    JungVariantGraph graph = new JungVariantGraph();
    algorithm.collate(graph, witnesses);
    return algorithm.getBlockWitnesses();
  }
}
