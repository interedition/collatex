package eu.interedition.collatex.dekker.suffix;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import eu.interedition.collatex.Token;

/*
 * Converts multiple token steams into a single token stream
 * There are marker tokens in between the witness tokens.
 * 
 * @author: Ronald Haentjens Dekker
 * 
 * Note: I don't like the marker tokens...
 * It would be hard to add them when a token is a generic object
 * This works for now..
 * 
 * Alternative would be to remember the positions where a token changes
 * between witnesses and let the subsequence limit the range to a
 * specific witness.
 */
public class MultipleWitnessSequence extends Sequence {
  // map the token ranges to a witness
  private RangeMap<Integer, BlockWitness> m;
  private List<BlockWitness> blockWitnesses;

  public MultipleWitnessSequence(List<Token> sequence, Comparator<Token> tokenComparator, RangeMap<Integer, BlockWitness> m, List<BlockWitness> blockWitnesses) {
    super(sequence, tokenComparator);
    this.m = m;
    this.blockWitnesses = blockWitnesses;
  }

  public static MultipleWitnessSequence createSequenceFromMultipleWitnesses(Comparator<Token> tokenComparator, List<? extends Iterable<Token>> witnesses) {
    RangeMap<Integer, BlockWitness> m = TreeRangeMap.create();
    List<BlockWitness> blockWitnesses = Lists.newArrayList();
    List<Token> tokens = Lists.newArrayList();
    int witnessNumber=1;
    for (Iterable<? extends Token> witness : witnesses) {
      int beginToken = tokens.size();
      for (Token t : witness) {
        tokens.add(t);
      }
      tokens.add(new MarkerToken(witnessNumber));
      witnessNumber++;
      int endToken = tokens.size()-1;
      // map ranges to witness
      Range<Integer> witnessRange = Range.closed(beginToken, endToken);
      BlockWitness b = new BlockWitness(witnessNumber);
      m.put(witnessRange, b);
      blockWitnesses.add(b);
    }
    return new MultipleWitnessSequence(tokens, tokenComparator, m, blockWitnesses);
  }

  public BlockWitness getBlockWitnessForStartPosition(Integer lowerEndpoint) {
    BlockWitness blockWitness = m.get(lowerEndpoint);
    return blockWitness;
  }

  public List<BlockWitness> getBlockWitnesses() {
    return blockWitnesses;
  }

}
