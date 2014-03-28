package eu.interedition.collatex.dekker.suffix;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import eu.interedition.collatex.simple.SimpleToken;

/*
 * Class represents a repeating non overlapping sequence of tokens
 * 
 * @author: Ronald Haentjens Dekker
 */
public class Block {
  private Sequence sequence;
  private List<Occurrence> occurances;
  
  public Block(Sequence s) {
    this.occurances = Lists.newArrayList();
    this.sequence = s;
  }
  
  public void addOccurance(Occurrence occurance) {
    occurances.add(occurance);
  }
  
  public void debug() {
    Sequence subsequence = convertBlockToSubsequence();
    System.out.println(subsequence.toString());
  }

  private Sequence convertBlockToSubsequence() {
    Sequence subsequence = sequence.subsequence(occurances.get(0).lowerEndpoint(), occurances.get(0).upperEndpoint()+1);
    return subsequence;
  }

  public boolean assertBlock(int index, int length) {
    boolean ok = occurances.get(0).lowerEndpoint() == index;
    ok = ok && occurances.get(0).upperEndpoint() - occurances.get(0).lowerEndpoint() == length;
    return ok;
  }
  
  public boolean assertBlockAsString(String expected) {
    String actual = toString();
    return actual.equals(expected);
  }

  @Override
  public String toString() {
    Sequence subsequence = convertBlockToSubsequence();
    StringBuilder b = new StringBuilder();
    for (int i=0; i<subsequence.length(); i++) {
      SimpleToken t = (SimpleToken) subsequence.tokenAt(i);
      b.append(t.getContent());
    }
    String actual = b.toString();
    return actual;
  }

  // not happy with this method; it is ask, instead of tell
  protected List<Occurrence> getOccurances() {
    return occurances;
  }

  // not happy with this method; it is ask, instead of tell
  public Sequence getSubsequenceForRange(Range<Integer> range) {
    return sequence.subsequence(range.lowerEndpoint(), range.upperEndpoint());
  }
}
