package eu.interedition.collatex.dekker.suffix;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleToken;

/*
 * Class represents a sequence of tokens
 * 
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

  public Token tokenAt(int i) {
    return sequence.get(i);
  }

  /*
   * Converts multiple token steams into a single token stream
   * There are marker tokens in between the witness tokens.
   * 
   * Note: I don't like the marker tokens...
   * It would be hard to add them when a token is a generic object
   * This works for now..
   * 
   * Alternative would be to remember the positions where a token changes
   * between witnesses and let the subsequence limit the range to a
   * specific witness.
   */
  public static Sequence createSequenceFromMultipleWitnesses(Comparator<Token> tokenComparator, List<Token>... witnesses) {
    List<Token> tokens = Lists.newArrayList();
    int witnessNumber=1;
    for (Iterable<Token> witness : witnesses) {
      for (Token t : witness) {
        tokens.add(t);
      }
      tokens.add(new MarkerToken(witnessNumber));
      witnessNumber++;
    }
    return new Sequence(tokens, tokenComparator);
  }
  
  @Override
  public String toString() {
    String result = "";
    for (Token t : sequence) {
      SimpleToken st = (SimpleToken) t;
      result += st.getContent();
    }
    return result;
  }
}
