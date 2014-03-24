package eu.interedition.collatex.dekker.suffix;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.google.common.collect.Lists;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;

public class DekkerOrderIndependentAlgorithm extends CollationAlgorithm.Base {
  private List<Block> blocks;
  private List<BlockWitness> blockWitnesses;

  @Override
  public void collate(VariantGraph against, List<? extends Iterable<Token>> witnesses) {
    MultipleWitnessSequence s = MultipleWitnessSequence.createSequenceFromMultipleWitnesses(new EqualityTokenComparator(), witnesses);
    TokenSuffixArrayNaive sa = new TokenSuffixArrayNaive(s);
    LCPArray lcp = new LCPArray(s, sa, new EqualityTokenComparator());
    SuperMaximumRepeats b = new SuperMaximumRepeats();
    blocks = b.calculateBlocks(sa, lcp, s);
//    for (Block block : blocks) {
//      block.debug();
//    }
    List<Occurence> allOccurences = Lists.newArrayList();
    for (Block block : blocks) {
      allOccurences.addAll(block.getOccurances());
    }
    Collections.sort(allOccurences);
    // System.out.println(allOccurences);
    Stack<Occurence> todo = new Stack<Occurence>();
    todo.addAll(allOccurences);
    while(!todo.isEmpty()) {
      Occurence pop = todo.remove(0);
      BlockWitness current = s.getBlockWitnessForStartPosition(pop.lowerEndpoint());
      current.addOccurence(pop);
    }
    blockWitnesses = s.getBlockWitnesses();
  }
  
  @Override
  public void collate(VariantGraph against, Iterable<Token> witness) {
    throw new UnsupportedOperationException("This is not supported; non progressive aligner!");
  }

  public List<Block> getBlocks() {
    return blocks;
  }

  public List<BlockWitness> getBlockWitnesses() {
    return blockWitnesses;
  }
}
