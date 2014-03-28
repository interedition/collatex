package eu.interedition.collatex.dekker.suffix;

import java.util.Map;

import com.google.common.collect.Range;

import eu.interedition.collatex.Token;

public class Occurrence implements Comparable<Occurrence> {
  private Block block;
  private Range<Integer> range;
  
  public Occurrence(Block block, Range<Integer> range) {
    this.block = block;
    this.range = range;
  }

  public Integer lowerEndpoint() {
    return range.lowerEndpoint();
  }

  public Integer upperEndpoint() {
    return range.upperEndpoint();
  }

  @Override
  public int compareTo(Occurrence arg0) {
    return lowerEndpoint() - arg0.lowerEndpoint();
  }
  
  public String toString() {
    return block.toString() + ":"+lowerEndpoint()+"-"+upperEndpoint();
  }

  public Block getBlock() {
    return block;
  }

  public void putOccurrenceIntoMap(Map<Token, Block> tokenToBlock) {
    // voor de hele range moet ik de tokens uit de sequence halen
    Sequence seq = block.getSubsequenceForRange(range);
    seq.mapTokensToBlock(block, tokenToBlock);
  }
}
