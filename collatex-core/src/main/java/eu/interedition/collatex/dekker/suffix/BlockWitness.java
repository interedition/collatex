package eu.interedition.collatex.dekker.suffix;

import java.util.List;

import com.google.common.collect.Lists;

/*
 * A witness only containing occurrences of maximum repeated blocks
 * @author: Ronald Haentjens Dekker
 */
public class BlockWitness {
  private List<Occurence> occurences;
  private int witnessNumber;
  
  public BlockWitness(int witnessNumber) {
    this.witnessNumber = witnessNumber;
    this.occurences = Lists.newArrayList();
  }

  public void addOccurence(Occurence occurence) {
    occurences.add(occurence);
  }

  public boolean assertTokens(String... expected) {
    boolean accomplished =  true;
    List<String> actual = Lists.newArrayList();
    for (Occurence o : occurences) {
      actual.add(o.toString());
    }
    for (int i=0; i< actual.size(); i++) {
      boolean result = actual.get(i).equals(expected[i]);
      if (!result) {
        accomplished = false;
        System.out.println("Expected: "+expected[i]+", but was: "+actual.get(i));
      }
    }
    return accomplished;
  }
}
