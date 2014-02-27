package eu.interedition.collatex.dekker.suffix;

import java.util.Comparator;
import java.util.List;

import eu.interedition.collatex.Token;
/*
 * @author: Ronald Haentjens Dekker
 */
public class Sequence {

  private final List<Token> sequence;
  private final Comparator<Token> tokenComparator;
  
  public Sequence(List<Token> sequence, Comparator<Token> tokenComparator) {
    this.sequence = sequence;
    this.tokenComparator = tokenComparator;
  }
  
  public int length() {
    return sequence.size();
  }
  
  // create a subsequence of this sequence starting at the
  // specified index till the end of this sequence
  public Sequence subsequence(int index) {
    return new Sequence(sequence.subList(index, length()), tokenComparator);
  }

  public int compareTo(Sequence other) {
    for (int i=0; i < Math.min(length(), other.length()); i++) {
      Token token1 = sequence.get(i);
      Token token2 = other.sequence.get(i);
      int result = tokenComparator.compare(token1, token2);
      if (result!=0) {
        return result;
      }
    }
    return other.length() - length();
  }

}
