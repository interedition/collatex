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

package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.interedition.collatex.experimental.interfaces.WitnessF;
import eu.interedition.collatex.experimental.ngrams.data.Token;
import eu.interedition.collatex.general.NormalizedWitnessBuilder;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class NGramTest {

  // TODO why is this called NGramTest: NGram isn't even used!
  // probably because of the NormalizedWitness.getTokens(1,2) which is used
  // for the creation of NGrams
  @Test
  public void testNGram() {
    final IWitness aa = NormalizedWitnessBuilder.create("A", "The black and white cat");
    final List<INormalizedToken> tokens = aa.createPhrase(1, 2).getTokens();
    final Token token = new Token("A", "The", 1);
    final Token token2 = new Token("A", "black", 2);

    Assert.assertEquals(2, tokens.size());
    Assert.assertEquals(token, tokens.get(0));
    Assert.assertEquals(token2, tokens.get(1));
  }

  @Test
  public void testNGram2Trim() {
    final IWitness a = WitnessF.create("A", "The black and white cat");
    final List<INormalizedToken> tokens = a.getTokens();
    final NGram ngram = new NGram(tokens);
    final NGram result = ngram.trim();
    Assert.assertEquals("black and white", result.getNormalized());
    // TODO maybe assert size()?
  }

}
