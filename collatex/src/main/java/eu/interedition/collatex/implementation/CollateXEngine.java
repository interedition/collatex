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

// TODO: normalizing spacing in project

package eu.interedition.collatex.implementation;

import eu.interedition.collatex.implementation.alignment.TokenLinker;
import eu.interedition.collatex.implementation.alignment.VariantGraphBuilder;
import eu.interedition.collatex.implementation.graph.db.VariantGraph;
import eu.interedition.collatex.implementation.graph.db.VariantGraphFactory;
import eu.interedition.collatex.implementation.input.DefaultTokenNormalizer;
import eu.interedition.collatex.implementation.input.WhitespaceTokenizer;
import eu.interedition.collatex.implementation.input.WitnessBuilder;
import eu.interedition.collatex.implementation.output.Apparatus;
import eu.interedition.collatex.interfaces.ITokenLinker;
import eu.interedition.collatex.interfaces.ITokenNormalizer;
import eu.interedition.collatex.interfaces.ITokenizer;
import eu.interedition.collatex.interfaces.IWitness;

import java.io.IOException;

/**
 * 
 * @author Interedition Dev Team
 * @author Ronald Haentjens Dekker
 *
 * CollateXEngine 
 * 
 * Public client factory class entry point into CollateX collation library
 * 
 */
public class CollateXEngine {
  private ITokenizer tokenizer = new WhitespaceTokenizer();
  // private ITokenizer tokenizer = new WhitespaceAndPunctuationTokenizer();
  private ITokenNormalizer tokenNormalizer = new DefaultTokenNormalizer();
  private ITokenLinker tokenLinker = new TokenLinker();

  private final VariantGraphFactory variantGraphFactory;

  public CollateXEngine() throws IOException {
    this(new VariantGraphFactory());
  }

  public CollateXEngine(VariantGraphFactory variantGraphFactory) {
    this.variantGraphFactory = variantGraphFactory;
  }
  
  public void setTokenLinker(ITokenLinker tokenLinker) {
    this.tokenLinker = tokenLinker;
  }

  public void setTokenizer(ITokenizer tokenizer) {
    this.tokenizer = tokenizer;
  }

  public void setTokenNormalizer(ITokenNormalizer tokenNormalizer) {
    this.tokenNormalizer = tokenNormalizer;
  }

  /**
   * Create an instance of an IWitness object
   * 
   * @param sigil - the unique id for this witness
   * @param text - the body of the witness
   * @return
   */
  public IWitness createWitness(final String sigil, final String text) {
    WitnessBuilder builder = new WitnessBuilder(tokenNormalizer);
    return builder.build(sigil, text, tokenizer);
  }

  /**
   * align the witnesses
   * 
   * @param witnesses - the witnesses
   * @return the alignment of the witnesses as a VariantGraph
   * 
   * @todo
   * We're not sure what we want to do with the name of this method: alignment vs. collation
   * Terminology check
   */
  public VariantGraph graph(IWitness... witnesses) {
    final VariantGraph graph = variantGraphFactory.create();
    new VariantGraphBuilder(graph).add(witnesses);
    return graph;
  }

  public Apparatus createApparatus(VariantGraph graph) {
    return graph.toApparatus();
  }
}
