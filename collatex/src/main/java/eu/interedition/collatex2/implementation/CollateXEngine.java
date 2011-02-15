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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.containers.graph.VariantGraph2;
import eu.interedition.collatex2.implementation.containers.witness.AlternativeWitnessIndex;
import eu.interedition.collatex2.implementation.containers.witness.NormalizedWitness;
import eu.interedition.collatex2.implementation.input.tokenization.DefaultTokenNormalizer;
import eu.interedition.collatex2.implementation.input.tokenization.WhitespaceTokenizer;
import eu.interedition.collatex2.implementation.output.apparatus.ParallelSegmentationApparatus;
import eu.interedition.collatex2.implementation.output.table.RankedGraphBasedAlignmentTable;
import eu.interedition.collatex2.implementation.vg_alignment.IAlignment2;
import eu.interedition.collatex2.implementation.vg_alignment.VariantGraphAligner;
import eu.interedition.collatex2.implementation.vg_analysis.Analyzer;
import eu.interedition.collatex2.implementation.vg_analysis.IAnalysis;
import eu.interedition.collatex2.interfaces.IAligner;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IToken;
import eu.interedition.collatex2.interfaces.ITokenIndex;
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
    final Iterable<IToken> tokens = tokenizer.tokenize(sigil, text);
    return new NormalizedWitness(sigil, Lists.newArrayList(Iterables.transform(tokens, tokenNormalizer)));
  }

  public IAligner createAligner() {
    VariantGraph2 graph = new VariantGraph2(); 
    return createAligner(graph);
  }

  public VariantGraphAligner createAligner(IVariantGraph graph) {
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

  public IAlignment2 align(IVariantGraph graph, IWitness witness) {
    VariantGraphAligner aligner = createAligner(graph);
    IAlignment2 alignment = aligner.align(witness);
    return alignment;
  }

  public IAnalysis analyse(IVariantGraph graph, IWitness witness) {
    IAlignment2 alignment = align(graph, witness);
    Analyzer analyzer = new Analyzer();
    return analyzer.analyze(alignment);
  }

  public ParallelSegmentationApparatus createApparatus(final IVariantGraph variantGraph) {
    return ParallelSegmentationApparatus.build(variantGraph);
  }

  public ParallelSegmentationApparatus createApparatus(IAlignmentTable result) {
    throw new RuntimeException("Not allowed! --> use createApparatus(VG) instead.");
  }

  public static IMatch createMatch(final INormalizedToken baseWord, final INormalizedToken witnessWord, final float editDistance) {
    throw new RuntimeException("Near matches are not yet supported!");
  }

  public static IMatch createMatch(final IPhrase basePhrase, final IPhrase witnessPhrase, final float editDistance) {
    throw new RuntimeException("Near matches are not yet supported!");
  }

  public static ITokenIndex createWitnessIndex(final IWitness witness) {
    return new AlternativeWitnessIndex(witness, witness.getRepeatedTokens());
  }

  // TODO: remove? seems only used in tests!
  protected static Set<String> getTokensWithMultiples(final Collection<IWitness> witnesses) {
    final Set<String> stringSet = Sets.newHashSet();
    for (final IWitness witness : witnesses) {
      final Multiset<String> tokenSet = HashMultiset.create();
      final List<INormalizedToken> tokens = witness.getTokens();
      for (final INormalizedToken token : tokens) {
        tokenSet.add(token.getNormalized());
      }
      final Set<String> elementSet = tokenSet.elementSet();
      for (final String tokenString : elementSet) {
        if (tokenSet.count(tokenString) > 1) {
          stringSet.add(tokenString);
        }
      }
    }
    return stringSet;
  }

  // TODO: remove? seems only used in tests!
  protected static Set<String> getPhrasesWithMultiples(final IWitness... witnesses) {
    final Set<String> stringSet = Sets.newHashSet();
    for (final IWitness witness : witnesses) {
      final Multiset<String> tokenSet = HashMultiset.create();
      final List<INormalizedToken> tokens = witness.getTokens();
      for (final INormalizedToken token : tokens) {
        tokenSet.add(token.getNormalized());
      }
      boolean duplicationFound = false;
      for (final String tokenString : tokenSet.elementSet()) {
        if (tokenSet.count(tokenString) > 1) {
          duplicationFound = true;
          stringSet.add(tokenString);
        }
      }
      if (duplicationFound) {
        // als er een dubbele gevonden is, kijk dan of deze uitgebreid kan
        // worden naar rechts
        for (int i = 0; i < tokens.size() - 1; i++) {
          final String currentNormalized = tokens.get(i).getNormalized();
          final String nextNormalized = tokens.get(i + 1).getNormalized();
          if (stringSet.contains(currentNormalized) && stringSet.contains(nextNormalized)) {
            tokenSet.add(currentNormalized + " " + nextNormalized);
          }
        }
      }
      for (final String tokenString : tokenSet.elementSet()) {
        if (tokenSet.count(tokenString) > 1) {
          duplicationFound = true;
          stringSet.add(tokenString);
        }
      }
    }
    return stringSet;
  }


  

}
