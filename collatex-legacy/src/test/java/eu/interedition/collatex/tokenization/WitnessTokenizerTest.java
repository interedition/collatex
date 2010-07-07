/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
