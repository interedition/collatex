package eu.interedition.collatex.dekker.suffix;

import com.google.common.collect.Range;

public class Occurence implements Comparable<Occurence> {
  private Block block;
  private Range<Integer> range;
  
  public Occurence(Block block, Range<Integer> range) {
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
  public int compareTo(Occurence arg0) {
    return lowerEndpoint() - arg0.lowerEndpoint();
  }
  
  public String toString() {
    return block.toString() + ":"+lowerEndpoint()+"-"+upperEndpoint();
  }
}
