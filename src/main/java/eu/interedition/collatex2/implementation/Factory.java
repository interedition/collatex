package eu.interedition.collatex2.implementation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.alignment.Alignment;
import eu.interedition.collatex2.implementation.alignment.GapDetection;
import eu.interedition.collatex2.implementation.alignment.SequenceDetection;
import eu.interedition.collatex2.implementation.alignmenttable.AlignmentTable4;
import eu.interedition.collatex2.implementation.alignmenttable.AlignmentTableCreator3;
import eu.interedition.collatex2.implementation.alignmenttable.Superbase4;
import eu.interedition.collatex2.implementation.indexing.AlignmentTableIndex;
import eu.interedition.collatex2.implementation.indexing.NullToken;
import eu.interedition.collatex2.implementation.indexing.WitnessIndex;
import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.implementation.matching.Match;
import eu.interedition.collatex2.implementation.matching.PhraseMatch;
import eu.interedition.collatex2.implementation.matching.RealMatcher;
import eu.interedition.collatex2.implementation.matching.worddistance.NormalizedLevenshtein;
import eu.interedition.collatex2.implementation.matching.worddistance.WordDistance;
import eu.interedition.collatex2.implementation.tokenization.NormalizedWitnessBuilder;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableIndex;
import eu.interedition.collatex2.interfaces.ICallback;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IPhraseMatch;
import eu.interedition.collatex2.interfaces.ISuperbase;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public class Factory {

  //  private static final Log LOG = LogFactory.getLog(Factory.class);

  public IWitness createWitness(final String sigil, final String words) {
    return NormalizedWitnessBuilder.create(sigil, words);
  }

  // NOTE: this method creates an alignmenttable, adds the first witness,
  // then calls the other createAlignmentMethod
  public IAlignment createAlignment(final IWitness a, final IWitness b) {
    final IAlignmentTable table = new AlignmentTable4();
    AlignmentTableCreator3.addWitness(table, a, NULLCALLBACK);
    final IAlignment alignment = createAlignmentUsingSuperbase(table, b);
    return alignment;
  }

  public IAlignment createAlignmentUsingSuperbase(final IAlignmentTable table, final IWitness b) {
    // make the superbase from the alignment table
    final ISuperbase superbase = Superbase4.create(table);
    final WordDistance distanceMeasure = new NormalizedLevenshtein();
    final Set<IPhraseMatch> phraseMatches = RealMatcher.findMatches(superbase, b, distanceMeasure);
    // now convert phrase matches to column matches
    final List<IMatch> matches = Lists.newArrayList();
    for (final IPhraseMatch phraseMatch : phraseMatches) {
      final IColumns columns = superbase.getColumnsFor(phraseMatch.getPhraseA());
      final IPhrase phraseB = phraseMatch.getPhraseB();
      matches.add(new Match(columns, phraseB));
    }

    final List<IGap> gaps = GapDetection.detectGap(matches, table, b);
    final IAlignment alignment = SequenceDetection.improveAlignment(new Alignment(matches, gaps));
    return alignment;
  }

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
    final List<IGap> gaps = GapDetection.detectGap(matches, table, witness);
    final IAlignment alignment = SequenceDetection.improveAlignment(new Alignment(matches, gaps));
    return alignment;
  }

  protected static List<IMatch> getMatchesUsingWitnessIndex(final IAlignmentTable table, final IWitness witness, final WordDistance distanceMeasure) {
    final Set<String> repeatingTokens = Sets.newHashSet();
    repeatingTokens.addAll(table.findRepeatingTokens());
    repeatingTokens.addAll(witness.findRepeatingTokens());
    return findMatches(AlignmentTableIndex.create(table, Lists.newArrayList(repeatingTokens)), new WitnessIndex(witness, repeatingTokens));
  }

  private static List<IMatch> findMatches(final IAlignmentTableIndex tableIndex, final IWitnessIndex witnessIndex) {
    final List<IMatch> matches = Lists.newArrayList();
    final Collection<IPhrase> phrases = witnessIndex.getPhrases();
    for (final IPhrase phrase : phrases) {
      if (tableIndex.containsNormalizedPhrase(phrase.getNormalized())) {
        final IColumns matchingColumns = tableIndex.getColumns(phrase.getNormalized());
        matches.add(new Match(matchingColumns, phrase));
      }
    }
    return joinOverlappingMatches(matches);
  }

  protected static List<IMatch> joinOverlappingMatches(final List<IMatch> matches) {
    final List<IMatch> newMatches = matches;
    // TODO implement
    return newMatches;
  }

  /* use or throw away everything after this */

  //  public IAlignment createAlignment0(final IAlignmentTable table, final IWitness b) {
  //    final WordDistance distanceMeasure = new NormalizedLevenshtein();
  //
  //    // tokenid = normalized name
  //
  //    final Multimap<String, IColumn> columnsForTokenId = Multimaps.newArrayListMultimap();
  //    for (final IColumn column : table.getColumns()) {
  //      for (final INormalizedToken normalizedToken : column.getVariants()) {
  //        columnsForTokenId.put(normalizedToken.getNormalized(), column);
  //      }
  //    }
  //
  //    final List<String> tokensFoundInMultipleColums = Lists.newArrayList();
  //    for (final String tokenId : columnsForTokenId.keySet()) {
  //      if (columnsForTokenId.get(tokenId).size() > 1) {
  //        tokensFoundInMultipleColums.add(tokenId);
  //      }
  //    }
  //
  //    for (final String tokenId : tokensFoundInMultipleColums) {
  //
  //    }
  //
  //    for (final INormalizedToken normalizedToken : b.getTokens()) {
  //      final String normalized = normalizedToken.getNormalized();
  //      final Collection<IColumn> columns = columnsForTokenId.get(normalized);
  //
  //    }
  //
  //    // van de table: verzamel die normalizedtokens die in meerdere columns voorkomen
  //    // van de witness: kijk of er normalizedtokens voorkomen die nog niet dubbel voorkomen in de table, maar wel in de witness
  //
  //    // bereken unieke phrases van de table => komen maar in 1 set columns voor
  //    // bereken unieke phrases van de witness => komen maar i keer voor in de witness
  //
  //    // per normalized token, in welke phrases komen ze voor?
  //    // per phrase: in welke columns komen ze voor?
  //
  //    // table.calculateUniquePhrases
  //    
  //    // 2: loop de tokens van de witness af
  //    //   is het token uniek in witness en table? -> vraag de column aan de alignmenttable, voeg column toe
  //    //   is het token niet uniek in witness of table? -> breid de phrase uit tot het uniek is.
  //    
  //
  //    final Set<IPhraseMatch> phraseMatches = findPhraseMatches(table, b, distanceMeasure);
  //
  //    // we hebben een alignmentable, kent alleen columns
  //    // we moeten daar uitzoeken welke tokens dubbel voorkomen
  //    // : loop 
  //
  //    final List<IMatch> matches = Lists.newArrayList();
  //
  //    final List<IGap> gaps = GapDetection.detectGap(matches, table, b);
  //    final IAlignment alignment = SequenceDetection.improveAlignment(new Alignment(matches, gaps));
  //    return alignment;
  //  }
  private Set<IPhraseMatch> findPhraseMatches(final IAlignmentTable table, final IWitness witness, final WordDistance distanceMeasure) {
    final Set<IPhraseMatch> matchSet = Sets.newLinkedHashSet();
    //    final Map<String, IWitnessIndex> witnessIndexMap = Factory.createWitnessIndexMap(Lists.newArrayList(base, witness));
    //    final IWitnessIndex baseIndex = witnessIndexMap.get(table.getSigil());
    //    final IWitnessIndex witnessIndex = witnessIndexMap.get(witness.getSigil());
    //    for (final IPhrase basePhrase : baseIndex.getPhrases()) {
    //      for (final IPhrase witnessPhrase : witnessIndex.getPhrases()) {
    //        if (basePhrase.getNormalized().equals(witnessPhrase.getNormalized())) {
    //          matchSet.add(new PhraseMatch(basePhrase, witnessPhrase));
    //        } else {
    //          // skip the near matches for now
    //          //          final float editDistance = distanceMeasure.distance(baseWord.getNormalized(), witnessWord.getNormalized());
    //          //          if (editDistance < 0.5) matchSet.add(Factory.createMatch(baseWord, witnessWord, editDistance));
    //        }
    //      }
    //    }
    //    // en nu opschonen
    return matchSet;
  }

  public static IWitnessIndex createWitnessIndex(final IWitness witness) {
    return new WitnessIndex(witness, witness.findRepeatingTokens());
  }

  public static Map<String, IWitnessIndex> createWitnessIndexMap(final Collection<IWitness> witnesses) {
    final Map<String, IWitnessIndex> map = Maps.newHashMap();
    final Set<String> tokensWithMultiples = getTokensWithMultiples(witnesses);

    for (final IWitness witness : witnesses) {
      final Multiset<IPhrase> phraseBag = Multisets.newTreeMultiset();
      Multimap<String, IPhrase> phraseMap = Multimaps.newTreeMultimap();
      final List<INormalizedToken> tokens = witness.getTokens();
      for (final INormalizedToken token : tokens) {
        phraseMap.put(token.getNormalized(), new Phrase(Lists.newArrayList(token)));
      }
      do {
        final Multimap<String, IPhrase> newPhraseMap = Multimaps.newHashMultimap();
        for (final String phraseId : phraseMap.keySet()) {
          final Collection<IPhrase> phrases = phraseMap.get(phraseId);
          if (tokensWithMultiples.contains(phraseId)) {
            addExpandedPhrases(newPhraseMap, phrases, tokens, tokensWithMultiples/*, phraseMap*/);
          } else {
            final IPhrase phrase = phrases.iterator().next();
            if (phrase.size() == 1) {
              newPhraseMap.put(phraseId, phrase);
            }
          }
        }
        phraseMap = newPhraseMap;
      } while (hasMultiples(phraseMap));
      final List<IPhrase> values = Lists.newArrayList(phraseMap.values());
      Collections.sort(values, Phrase.PHRASECOMPARATOR);
      phraseBag.addAll(values);
      map.put(witness.getSigil(), new WitnessIndex(phraseBag));
    }

    return map;
  }

  private static final Predicate<IPhrase> TWO_OR_MORE_WORDS = new Predicate<IPhrase>() {
    @Override
    public boolean apply(final IPhrase phrase) {
      return phrase.size() > 1;
    }
  };

  private static boolean hasMultiples(final Multimap<String, IPhrase> phraseMap) {
    return phraseMap.entries().size() > phraseMap.keySet().size();
  }

  private static void addExpandedPhrases(final Multimap<String, IPhrase> newPhraseMap, final Collection<IPhrase> phrases, final List<INormalizedToken> tokens, final Set<String> tokensWithMultiples) {
    for (final IPhrase phrase : phrases) {
      final int beforePosition = phrase.getBeginPosition() - 1;
      final int afterPosition = phrase.getEndPosition();

      final INormalizedToken beforeToken = (beforePosition > 0) ? tokens.get(beforePosition - 1) : new NullToken(phrase.getBeginPosition(), phrase.getSigil());
      final INormalizedToken afterToken = (afterPosition < tokens.size()) ? tokens.get(afterPosition) : new NullToken(phrase.getEndPosition(), phrase.getSigil());

      final ArrayList<INormalizedToken> leftExpandedTokenList = Lists.newArrayList(beforeToken);
      leftExpandedTokenList.addAll(phrase.getTokens());
      final IPhrase leftExpandedPhrase = new Phrase(leftExpandedTokenList);

      final ArrayList<INormalizedToken> rightExpandedTokenList = Lists.newArrayList(phrase.getTokens());
      rightExpandedTokenList.add(afterToken);
      final IPhrase rightExpandedPhrase = new Phrase(rightExpandedTokenList);

      final String leftPhraseId = leftExpandedPhrase.getNormalized();
      newPhraseMap.put(leftPhraseId, leftExpandedPhrase);

      final String rightPhraseId = rightExpandedPhrase.getNormalized();
      newPhraseMap.put(rightPhraseId, rightExpandedPhrase);
    }
  }

  //  public static Map<String, IWitnessIndex> createWitnessIndexMap(final IWitness... witnesses) {
  //    final Map<String, IWitnessIndex> map = Maps.newHashMap();
  //    final List<String> uniquePhrases = Lists.newArrayList();
  //    for (final IWitness witness : witnesses) {
  //      final IWitnessIndex index = createWitnessIndex(witness);
  //      map.put(witness.getSigil(), index);
  //      uniquePhrases.addAll(Lists.newArrayList(transform(filter(index.getPhrases(), TWO_OR_MORE_WORDS), WitnessIndex.PHRASE_TO_NORMALIZED)));
  //    }
  //    final Set<Entry<String, IWitnessIndex>> entrySet = map.entrySet();
  //    for (final Entry<String, IWitnessIndex> entry : entrySet) {
  //      final String sigil = entry.getKey();
  //      final IWitnessIndex index = entry.getValue();
  //      index.use(uniquePhrases);
  //    }
  //    return map;
  //  }

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
