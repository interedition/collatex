package eu.interedition.collatex2.implementation;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.alignment.Alignment;
import eu.interedition.collatex2.implementation.alignment.GapDetection;
import eu.interedition.collatex2.implementation.alignment.SequenceDetection;
import eu.interedition.collatex2.implementation.alignmenttable.AlignmentTable4;
import eu.interedition.collatex2.implementation.alignmenttable.AlignmentTableCreator3;
import eu.interedition.collatex2.implementation.alignmenttable.Columns;
import eu.interedition.collatex2.implementation.indexing.AlignmentTableIndex;
import eu.interedition.collatex2.implementation.indexing.NullColumn;
import eu.interedition.collatex2.implementation.indexing.WitnessIndex;
import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.implementation.matching.Match;
import eu.interedition.collatex2.implementation.matching.PhraseMatch;
import eu.interedition.collatex2.implementation.matching.worddistance.NormalizedLevenshtein;
import eu.interedition.collatex2.implementation.matching.worddistance.WordDistance;
import eu.interedition.collatex2.implementation.tokenization.NormalizedWitnessBuilder;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableIndex;
import eu.interedition.collatex2.interfaces.ICallback;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IPhraseMatch;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public class Factory {

  private static final Log LOG = LogFactory.getLog(Factory.class);

  public IWitness createWitness(final String sigil, final String words) {
    return NormalizedWitnessBuilder.create(sigil, words);
  }

  // NOTE: this method creates an alignmenttable, adds the first witness,
  // then calls the other createAlignmentMethod
  public IAlignment createAlignment(final IWitness a, final IWitness b) {
    final IAlignmentTable table = new AlignmentTable4();
    AlignmentTableCreator3.addWitness(table, a, NULLCALLBACK);
    final IAlignment alignment = createAlignmentUsingIndex(table, b);
    return alignment;
  }

  //  public IAlignment createAlignmentUsingSuperbase(final IAlignmentTable table, final IWitness b) {
  //    // make the superbase from the alignment table
  //    final ISuperbase superbase = Superbase4.create(table);
  //    final WordDistance distanceMeasure = new NormalizedLevenshtein();
  //    final Set<IPhraseMatch> phraseMatches = RealMatcher.findMatches(superbase, b, distanceMeasure);
  //    // now convert phrase matches to column matches
  //    final List<IMatch> matches = Lists.newArrayList();
  //    for (final IPhraseMatch phraseMatch : phraseMatches) {
  //      final IColumns columns = superbase.getColumnsFor(phraseMatch.getPhraseA());
  //      final IPhrase phraseB = phraseMatch.getPhraseB();
  //      matches.add(new Match(columns, phraseB));
  //    }
  //
  //    final List<IGap> gaps = GapDetection.detectGap(matches, table, b);
  //    final IAlignment alignment = SequenceDetection.improveAlignment(new Alignment(matches, gaps));
  //    return alignment;
  //  }

  public static IPhraseMatch createMatch(final INormalizedToken baseWord, final INormalizedToken witnessWord) {
    final Phrase a = Phrase.create(baseWord);
    final Phrase b = Phrase.create(witnessWord);
    return new PhraseMatch(a, b);
  }

  public static IPhraseMatch createMatch(final INormalizedToken baseWord, final INormalizedToken witnessWord, final float editDistance) {
    throw new RuntimeException("Near matches are not yet supported!");
  }

  public static IPhraseMatch createMatch(final IPhrase basePhrase, final IPhrase witnessPhrase, final float editDistance) {
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
    final List<IMatch> matches = getMatchesUsingWitnessIndex(table, witness, new NormalizedLevenshtein());
    LOG.info(matches);
    final List<IGap> gaps = GapDetection.detectGap(matches, table, witness);
    final IAlignment alignment = SequenceDetection.improveAlignment(new Alignment(matches, gaps));
    return alignment;
  }

  //TODO: move to another class!
  protected static List<IMatch> getMatchesUsingWitnessIndex(final IAlignmentTable table, final IWitness witness, final WordDistance distanceMeasure) {
    final List<String> repeatingTokens = combineRepeatingTokens(table, witness);
    return findMatches(AlignmentTableIndex.create(table, repeatingTokens), new WitnessIndex(witness, repeatingTokens));
  }

  //TODO: move to another class!
  private static List<String> combineRepeatingTokens(final IAlignmentTable table, final IWitness witness) {
    final Set<String> repeatingTokens = Sets.newHashSet();
    repeatingTokens.addAll(table.findRepeatingTokens());
    repeatingTokens.addAll(witness.findRepeatingTokens());
    return Lists.newArrayList(repeatingTokens);
  }

  //TODO: move to another class!
  private static List<IMatch> findMatches(final IAlignmentTableIndex tableIndex, final IWitnessIndex witnessIndex) {
    final List<IMatch> matches = Lists.newArrayList();
    final Collection<IPhrase> phrases = witnessIndex.getPhrases();
    for (final IPhrase phrase : phrases) {
      if (tableIndex.containsNormalizedPhrase(phrase.getNormalized())) {
        final IColumns matchingColumns = tableIndex.getColumns(phrase.getNormalized());
        matches.add(new Match(matchingColumns, phrase));
      }
    }
    LOG.info("unfiltered matches: " + matches);
    return joinOverlappingMatches(matches);
  }

  protected static List<IMatch> joinOverlappingMatches(final List<IMatch> matches) {
    final List<IMatch> newMatches = filterMatchesBasedOnPositionMatches(matches);
    LOG.info("filtered matches: " + newMatches);
    return newMatches;
  }

  //TODO: make IColumns Iterable!
  //TODO: check whether there is a wrong second token placed on the same position!
  @SuppressWarnings("boxing")
  private static List<IMatch> filterMatchesBasedOnPositionMatches(final List<IMatch> matches) {
    final Map<Integer, IColumn> columnsMap = Maps.newHashMap();
    final Map<Integer, INormalizedToken> tokenMap = Maps.newHashMap();
    for (final IMatch match : matches) {
      //TODO: rename match.getColumnsA to match.getColumns
      final IColumns columns = match.getColumns();
      final IPhrase phrase = match.getPhrase();
      final Iterator<INormalizedToken> tokens = phrase.getTokens().iterator();
      for (final IColumn column : columns.getColumns()) {
        if (!(column instanceof NullColumn)) {
          final int position = column.getPosition();
          columnsMap.put(position, column);
          tokenMap.put(position, tokens.next());
        } else {
          tokens.next();
        }
      }
    }
    final List<IMatch> newMatches = Lists.newArrayList();
    final List<Integer> positions = Lists.newArrayList(columnsMap.keySet());
    Collections.sort(positions);
    for (final Integer position : positions) {
      final IColumn column = columnsMap.get(position);
      final INormalizedToken token = tokenMap.get(position);
      //TODO: hide this in constructors!
      final IColumns columns = new Columns(Lists.newArrayList(column));
      final IPhrase phrase = new Phrase(Lists.newArrayList(token));
      final IMatch newMatch = new Match(columns, phrase);
      newMatches.add(newMatch);
    }
    return newMatches;
  }

  public static IWitnessIndex createWitnessIndex(final IWitness witness) {
    return new WitnessIndex(witness, witness.findRepeatingTokens());
  }

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
