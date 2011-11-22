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

package eu.interedition.collatex2.implementation;

import eu.interedition.collatex2.implementation.containers.graph.VariantGraph;
import eu.interedition.collatex2.implementation.input.builders.WitnessBuilder;
import eu.interedition.collatex2.implementation.input.tokenization.DefaultTokenNormalizer;
import eu.interedition.collatex2.implementation.input.tokenization.WhitespaceTokenizer;
import eu.interedition.collatex2.implementation.output.apparatus.ParallelSegmentationApparatus;
import eu.interedition.collatex2.implementation.output.table.RankedGraphBasedAlignmentTable;
import eu.interedition.collatex2.implementation.vg_alignment.VariantGraphBuilder;
import eu.interedition.collatex2.interfaces.*;

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

  public VariantGraphBuilder createVariantGraphBuilder(IVariantGraph graph) {
    return new VariantGraphBuilder(graph);
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
  public IVariantGraph graph(IWitness... witnesses) {
    VariantGraphBuilder builder = createVariantGraphBuilder(new VariantGraph());
    builder.add(witnesses);
    return builder.getResult();
  }

  /**
   * align the witnesses
   * 
   * @param witnesses - the witnesses
   * @return the alignment of the witnesses
   * 
   * @todo
   * We're not sure what we want to do with the name of this method: alignment vs. collation
   * Terminology check
   */
  public IAlignmentTable align(IWitness... witnesses) {
    IVariantGraph vg = graph(witnesses);
    RankedGraphBasedAlignmentTable table = new RankedGraphBasedAlignmentTable(vg);
    return table;
  }

  public IApparatus createApparatus(final IVariantGraph variantGraph) {
    return ParallelSegmentationApparatus.build(variantGraph);
  }
}
