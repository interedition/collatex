package eu.interedition.collatex.dekker.suffix;

import java.util.List;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;

public class DekkerOrderIndependentAlgorithm extends CollationAlgorithm.Base {
  private List<Block> blocks;

  @Override
  public void collate(VariantGraph against, List<? extends Iterable<Token>> witnesses) {
    Sequence s = Sequence.createSequenceFromMultipleWitnesses(new EqualityTokenComparator(), witnesses);
    TokenSuffixArrayNaive sa = new TokenSuffixArrayNaive(s);
    LCPArray lcp = new LCPArray(s, sa, new EqualityTokenComparator());
    SuperMaximumRepeats b = new SuperMaximumRepeats();
    blocks = b.calculateBlocks(sa, lcp, s);
    for (Block block : blocks) {
      block.debug();
    }
  }
  
  @Override
  public void collate(VariantGraph against, Iterable<Token> witness) {
    throw new UnsupportedOperationException("This is not supported; non progressive aligner!");
  }

  public List<Block> getBlocks() {
    return blocks;
  }

}
