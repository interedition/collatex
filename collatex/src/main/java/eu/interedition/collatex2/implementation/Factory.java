package eu.interedition.collatex2.implementation;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.alignment.Alignment;
import eu.interedition.collatex2.implementation.alignment.GapDetection;
import eu.interedition.collatex2.implementation.alignment.SequenceDetection;
import eu.interedition.collatex2.implementation.alignmenttable.AlignmentTable4;
import eu.interedition.collatex2.implementation.alignmenttable.AlignmentTableCreator3;
import eu.interedition.collatex2.implementation.indexing.WitnessIndex;
import eu.interedition.collatex2.implementation.input.NormalizedWitness;
import eu.interedition.collatex2.implementation.matching.IndexMatcher;
import eu.interedition.collatex2.implementation.tokenization.DefaultTokenNormalizer;
import eu.interedition.collatex2.implementation.tokenization.WhitespaceTokenizer;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ICallback;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IToken;
import eu.interedition.collatex2.interfaces.ITokenNormalizer;
import eu.interedition.collatex2.interfaces.ITokenizer;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public class Factory {
  private ITokenizer tokenizer = new WhitespaceTokenizer();
  private ITokenNormalizer tokenNormalizer = new DefaultTokenNormalizer();
  
  public void setTokenizer(ITokenizer tokenizer) {
    this.tokenizer = tokenizer;
  }
  
  public void setTokenNormalizer(ITokenNormalizer tokenNormalizer) {
    this.tokenNormalizer = tokenNormalizer;
  }
  
  public IWitness createWitness(final String sigil, final String words) {
    final Iterable<IToken> tokens = tokenizer.tokenize(sigil, words);
    return new NormalizedWitness(sigil, Lists.newArrayList(Iterables.transform(tokens, tokenNormalizer)));
  }

  // NOTE: this method creates an alignmenttable, adds the first witness,
  // then calls the other createAlignmentMethod
  public IAlignment createAlignment(final IWitness a, final IWitness b) {
    final IAlignmentTable table = new AlignmentTable4();
    AlignmentTableCreator3.addWitness(table, a, NULLCALLBACK);
    final IAlignment alignment = createAlignmentUsingIndex(table, b);
    return alignment;
  }

  public static IMatch createMatch(final INormalizedToken baseWord, final INormalizedToken witnessWord, final float editDistance) {
    throw new RuntimeException("Near matches are not yet supported!");
  }

  public static IMatch createMatch(final IPhrase basePhrase, final IPhrase witnessPhrase, final float editDistance) {
    throw new RuntimeException("Near matches are not yet supported!");
  }

  public static ICallback NULLCALLBACK = new ICallback() {
    @Override
    public void alignment(final IAlignment alignment) {}
  };

  public IAlignmentTable createAlignmentTable(final List<IWitness> set) {
    return createAlignmentTable(set, NULLCALLBACK);
  }

  public IAlignmentTable createAlignmentTable(final List<IWitness> set, final ICallback callback) {
    return AlignmentTableCreator3.createAlignmentTable(set, callback);
  }

  public IAlignment createAlignmentUsingIndex(final IAlignmentTable table, final IWitness witness) {
    final List<IMatch> matches = IndexMatcher.getMatchesUsingWitnessIndex(table, witness);
    final List<IGap> gaps = GapDetection.detectGap(matches, table, witness);
    final IAlignment alignment = SequenceDetection.improveAlignment(new Alignment(matches, gaps));
    return alignment;
  }

  public static IWitnessIndex createWitnessIndex(final IWitness witness) {
    return new WitnessIndex(witness, witness.findRepeatingTokens());
  }

  //TODO: remove? seems only used in tests!
  protected static Set<String> getTokensWithMultiples(final Collection<IWitness> witnesses) {
    final Set<String> stringSet = Sets.newHashSet();
    for (final IWitness witness : witnesses) {
      final Multiset<String> tokenSet = Multisets.newHashMultiset();
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

  //TODO: remove? seems only used in tests!
  protected static Set<String> getPhrasesWithMultiples(final IWitness... witnesses) {
    final Set<String> stringSet = Sets.newHashSet();
    for (final IWitness witness : witnesses) {
      final Multiset<String> tokenSet = Multisets.newHashMultiset();
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
        // als er een dubbele gevonden is, kijk dan of deze uitgebreid kan worden naar rechts
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
