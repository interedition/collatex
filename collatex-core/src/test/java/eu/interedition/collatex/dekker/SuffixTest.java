package eu.interedition.collatex.dekker;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.suffix.Block;
import eu.interedition.collatex.dekker.suffix.BlockWitness;
import eu.interedition.collatex.dekker.suffix.Collation;
import eu.interedition.collatex.dekker.suffix.LCPArray;
import eu.interedition.collatex.dekker.suffix.MultipleWitnessSequence;
import eu.interedition.collatex.dekker.suffix.Sequence;
import eu.interedition.collatex.dekker.suffix.TokenSuffixArrayNaive;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;

public class SuffixTest {

  // helper method
  private List<Token> createSequenceFromString(String a) {
    String[] t = a.split("\\s+");
    List<String> tokens = Lists.newArrayList(t);
    // have to convert a list of strings into a sequence/list of tokens
    // I would like it better if token was just a generic type
    List<Token> stokens = Lists.newArrayList();
    for (String to : tokens) {
      stokens.add(new SimpleToken(new SimpleWitness("sigil"), to, to));
    }
    return stokens;
  }
  
  // first we find the suffixes for one witness
  @Test
  public void testSuffixArrayAndLCPArray() {
    String a = "a b c d a b";
    List<Token> tokens = createSequenceFromString(a);
    Sequence s = new Sequence(tokens, new EqualityTokenComparator());
    TokenSuffixArrayNaive sa = new TokenSuffixArrayNaive(s);
    assertTrue(sa.arrayEquals(new int[] { 0, 4, 1, 5, 2, 3 }));
    LCPArray lcp = new LCPArray(s, sa, new EqualityTokenComparator());
    assertTrue(lcp.arrayEquals(new int[] { -1, 2, 0, 1, 0, 0 }));
  }

    // find the suffixes and lcps for two witnesses
  @Test
  public void testHermansWitnessOrder() {
    String W1 = "a b c d F g h i ! K ! q r s t";
    String W2 = "a b c d F g h i ! q r s t";
    List<Token> tokensW1 = createSequenceFromString(W1);
    List<Token> tokensW2 = createSequenceFromString(W2);
    @SuppressWarnings("unchecked")
    Sequence s = MultipleWitnessSequence.createSequenceFromMultipleWitnesses(new EqualityTokenComparator(), Lists.newArrayList(tokensW1, tokensW2));
    TokenSuffixArrayNaive sa = new TokenSuffixArrayNaive(s);
    assertTrue(sa.arrayEquals(new int[] { 8,10,24,15,29,4,20,9,0,16,1,17,2,18,3,19,5,21,6,22,7,23,11,25,12,26,13,27,14,28 }));
    LCPArray lcp = new LCPArray(s, sa, new EqualityTokenComparator());
    assertTrue(lcp.arrayEquals(new int[] {-1,1,5,0,0,0,5,0,0,9,0,8,0,7,0,6,0,4,0,3,0,2,0,4,0,3,0,2,0,1,}));
  }

  @Test
  public void testSMR() {
    Collation collation = Collation.create().addWitness("a b c d F g h i ! K ! q r s t").addWitness("a b c d F g h i ! q r s t");
    List<Block> blocks = collation.getBlocks();
    blocks.get(0).assertBlockAsString("a b c d F g h i !");
    blocks.get(1).assertBlockAsString("q r s t");
    assertEquals(2, blocks.size());
  }
  
  // test the transformation of witnesses into block witnesses
  @Test
  public void testTransform() {
    Collation collation = Collation.create().addWitness("a b c d F g h i ! K ! q r s t").addWitness("a b c d F g h i ! q r s t");
    List<BlockWitness> witnesses = collation.getBlockWitnesses();
    BlockWitness bw1 = witnesses.get(0);
    bw1.assertTokens("a b c d F g h i ! :0-8", "q r s t:11-14");
  }
}
