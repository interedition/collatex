package eu.interedition.collatex.dekker.suffix;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.Token;

/*
 * A witness only containing occurrences of maximum repeated blocks
 * @author: Ronald Haentjens Dekker
 */
public class BlockWitness {
  private List<Occurrence> occurrences;
  private int witnessNumber;
  
  public BlockWitness(int witnessNumber) {
    this.witnessNumber = witnessNumber;
    this.occurrences = Lists.newArrayList();
  }

  public void addOccurence(Occurrence occurence) {
    occurrences.add(occurence);
  }

  public List<Occurrence> getOccurrences() {
    return occurrences;
  }
  
  public boolean assertTokens(String... expected) {
    boolean accomplished =  true;
    List<String> actual = Lists.newArrayList();
    for (Occurrence o : occurrences) {
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
  
  public Map<Token, Block> transformIntoTokenBlockMap() {
    Map<Token, Block> tokenToBlock = Maps.newLinkedHashMap();
    for (Occurrence oc : occurrences) {
      oc.putOccurrenceIntoMap(tokenToBlock);
    }
    return tokenToBlock;
  }
}
