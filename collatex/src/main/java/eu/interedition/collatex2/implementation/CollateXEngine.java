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
import eu.interedition.collatex2.implementation.vg_alignment.IAlignment;
import eu.interedition.collatex2.implementation.vg_alignment.VariantGraphAligner;
import eu.interedition.collatex2.implementation.vg_analysis.Analyzer;
import eu.interedition.collatex2.implementation.vg_analysis.IAnalysis;
import eu.interedition.collatex2.interfaces.IAligner;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IApparatus;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.ITokenNormalizer;
import eu.interedition.collatex2.interfaces.ITokenizer;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.nonpublic.modifications.IMatch;

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

  public IAligner createAligner() {
    VariantGraph graph = new VariantGraph();
    return createAligner(graph);
  }

  public IAligner createAligner(IVariantGraph graph) {
    // return new VariantGraphAligner(graph);
    return new VariantGraphAligner(graph);
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
    IAligner aligner = createAligner();
    aligner.add(witnesses);
    return aligner.getResult();
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

  public IAlignment align(IVariantGraph graph, IWitness witness) {
    IAligner aligner = createAligner(graph);
    IAlignment alignment = aligner.align(witness);
    return alignment;
  }

  public IAnalysis analyse(IVariantGraph graph, IWitness witness) {
    IAlignment alignment = align(graph, witness);
    Analyzer analyzer = new Analyzer();
    return analyzer.analyze(alignment);
  }

  public IApparatus createApparatus(final IVariantGraph variantGraph) {
    return ParallelSegmentationApparatus.build(variantGraph);
  }

  @Deprecated
  public IApparatus createApparatus(IAlignmentTable result) {
    throw new RuntimeException("Not allowed! --> use createApparatus(VG) instead.");
  }

  @Deprecated
  public static IMatch createMatch(final INormalizedToken baseWord, final INormalizedToken witnessWord, final float editDistance) {
    throw new RuntimeException("Near matches are not yet supported!");
  }

  @Deprecated
  public static IMatch createMatch(final IPhrase basePhrase, final IPhrase witnessPhrase, final float editDistance) {
    throw new RuntimeException("Near matches are not yet supported!");
  }



  

}
