package eu.interedition.collatex.tokenization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WitnessTokenizerTest {
  @Test
  public void test1() {
    String witness = "A black cat.";
    WitnessTokenizer tokenizer = new WitnessTokenizer(witness, true);
    assertTrue(tokenizer.hasNextToken());
    assertEquals("a", tokenizer.nextToken().getText());
    assertTrue(tokenizer.hasNextToken());
    assertEquals("black", tokenizer.nextToken().getText());
    assertTrue(tokenizer.hasNextToken());
    assertEquals("cat", tokenizer.nextToken().getText());
    assertFalse(tokenizer.hasNextToken());
  }

  @Test
  public void test2() {
    String witness = "Alas, Horatio, I knew him well.";
    WitnessTokenizer tokenizer = new WitnessTokenizer(witness, false);
    assertTrue(tokenizer.hasNextToken());
    assertEquals("Alas,", tokenizer.nextToken().getText());
    assertTrue(tokenizer.hasNextToken());
    assertEquals("Horatio,", tokenizer.nextToken().getText());
    assertTrue(tokenizer.hasNextToken());
    assertEquals("I", tokenizer.nextToken().getText());
  }

  @Test
  public void test3() {
    String witness = "ταυτα ειπων ο ιϲ̅ εξηλθεν ϲυν τοιϲ μαθηταιϲ αυτου περαν του χειμαρρου των κεδρων οπου ην κηποϲ ειϲ ον ειϲηλθεν αυτοϲ και οι μαθηται αυτου";
    WitnessTokenizer tokenizer = new WitnessTokenizer(witness, false);
    assertTrue(tokenizer.hasNextToken());
    assertEquals("ταυτα", tokenizer.nextToken().getText());
    assertTrue(tokenizer.hasNextToken());
    assertEquals("ειπων", tokenizer.nextToken().getText());
    assertTrue(tokenizer.hasNextToken());
    assertEquals("ο", tokenizer.nextToken().getText());
  }

  @Test
  // Note: add Don't you think? What to do with '?
  // TODO rename original to something else?
  public void testPunctuation() {
    WitnessTokenizer tokenizer = new WitnessTokenizer("Punctuation. is, important!", true);
    Token nextToken = tokenizer.nextToken();
    assertEquals("punctuation", nextToken.getText());
    assertEquals(".", nextToken.getPunctuation());
  }
}
