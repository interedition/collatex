package eu.interedition.collatex2.implementation.matching;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.indexing.AlignmentTableIndex;
import eu.interedition.collatex2.implementation.indexing.NullToken;
import eu.interedition.collatex2.implementation.indexing.WitnessIndex;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableIndex;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public class IndexMatcher {
  public static final Logger    LOG = LoggerFactory.getLogger(IndexMatcher.class);
  private final IAlignmentTable table;
  private final IWitness        witness;
  private IAlignmentTableIndex  alignmentTableIndex;

  public IndexMatcher(IAlignmentTable table, IWitness witness) {
    this.table = table;
    this.witness = witness;
  }

  public List<ITokenMatch> getMatches() {
    final List<String> repeatingTokens = IndexMatcher.combineRepeatingTokens(table, witness);
    alignmentTableIndex = AlignmentTableIndex.create(table, repeatingTokens);
    return IndexMatcher.findMatches(alignmentTableIndex, new WitnessIndex(witness, repeatingTokens));
  }

  public static List<String> combineRepeatingTokens(final IAlignmentTable table, final IWitness witness) {
    final Set<String> repeatingTokens = Sets.newHashSet();
    repeatingTokens.addAll(table.findRepeatingTokens());
    repeatingTokens.addAll(witness.findRepeatingTokens());
    return Lists.newArrayList(repeatingTokens);
  }

  public static List<ITokenMatch> findMatches(final IWitnessIndex tableIndex, final IWitnessIndex witnessIndex) {
    final List<PhraseMatch> matches = Lists.newArrayList();
    final Collection<IPhrase> phrases = witnessIndex.getPhrases();
    for (final IPhrase phrase : phrases) {
//      IndexMatcher.LOG.debug("Looking for phrase: " + phrase.getNormalized());
      if (tableIndex.contains(phrase.getNormalized())) {
//        IndexMatcher.LOG.debug("FOUND!");
        final IPhrase tablePhrase = tableIndex.getPhrase(phrase.getNormalized());
        matches.add(new PhraseMatch(tablePhrase, phrase));
      }
    }
    IndexMatcher.LOG.debug("unfiltered matches: " + matches);
    return IndexMatcher.joinOverlappingMatches(matches);
  }

  public static List<ITokenMatch> joinOverlappingMatches(final List<PhraseMatch> matches) {
    final List<ITokenMatch> newMatches = IndexMatcher.filterMatchesBasedOnPositionMatches(matches);
    IndexMatcher.LOG.debug("filtered matches: " + newMatches);
    return newMatches;
  }

  // TODO: make IColumns Iterable!
  // NOTE: There is a potential situation here where 1 column matches with
  // multiple phrases
  // NOTE: The other phrases are seen as additions, which causes too many empty
  // columns
  // NOTE: --> not the optimal alignment
  @SuppressWarnings("boxing")
  public static List<ITokenMatch> filterMatchesBasedOnPositionMatches(final List<PhraseMatch> matches) {
    final Map<Integer, INormalizedToken> tableTokenMap = Maps.newHashMap();
    final Map<Integer, INormalizedToken> witnessTokenMap = Maps.newHashMap();
    List<PhraseMatch> filteredMatches = filterAwaySecondChoicesMultipleTokensOneColumn(filterAwaySecondChoicesMultipleColumnsOneToken(matches));
    for (final PhraseMatch match : filteredMatches) {
      // step 1. Gather data
      List<TokenPair> pairs = Lists.newArrayList();
      final IPhrase tablePhrase = match.getTablePhrase();
      final IPhrase witnessPhrase = match.getPhrase();
      final Iterator<INormalizedToken> tokens = witnessPhrase.getTokens().iterator();
      for (final INormalizedToken tableToken : tablePhrase.getTokens()) {
        final INormalizedToken token = tokens.next();
        // skip NullColumn and NullToken
        if (!(tableToken instanceof NullToken)) {
          pairs.add(new TokenPair(tableToken, token));
        }
      }
      // step 2. Split phrase matches
      for (TokenPair pair : pairs) {
        final INormalizedToken column = pair.tableToken;
        final INormalizedToken token = pair.witnessToken;
        final int position = token.getPosition();
//        System.out.println(column.getContent() + ":" + column.getSigil() + ":" + position);
        tableTokenMap.put(position, column);
        witnessTokenMap.put(position, token);
      }
    }
    final List<ITokenMatch> newMatches = Lists.newArrayList();
    final List<Integer> positions = Lists.newArrayList(tableTokenMap.keySet());
    Collections.sort(positions); // TODO: remove sort here!
    for (final Integer position : positions) {
      final INormalizedToken tableToken = tableTokenMap.get(position);
      final INormalizedToken token = witnessTokenMap.get(position);
      final ITokenMatch newMatch = new TokenMatch(tableToken, token);
      newMatches.add(newMatch);
    }
    return newMatches;
  }

  public IAlignmentTableIndex getAlignmentTableIndex() {
    return alignmentTableIndex;
  }

  // check whether this match has an alternative that is equal in weight
  // if so, then skip the alternative!
  // NOTE: multiple columns match with the same token!
  public static List<PhraseMatch> filterAwaySecondChoicesMultipleColumnsOneToken(List<PhraseMatch> matches) {
    List<PhraseMatch> filteredMatches = Lists.newArrayList();
    final Map<INormalizedToken, INormalizedToken> tokenToTable = Maps.newLinkedHashMap();
    for (final PhraseMatch match : matches) {
      // step 1. Gather data
      List<TokenPair> pairs = Lists.newArrayList();
      final IPhrase tablePhrase = match.getTablePhrase();
      final IPhrase witnessPhrase = match.getPhrase();
      final Iterator<INormalizedToken> tokens = witnessPhrase.getTokens().iterator();
      for (final INormalizedToken tableToken : tablePhrase.getTokens()) {
        final INormalizedToken token = tokens.next();
        // skip NullColumn and NullToken
        if (!(tableToken instanceof NullToken)) {
          pairs.add(new TokenPair(tableToken, token));
        }
      }
      // step 2. Look for alternative
      boolean foundAlternative = false;
      for (TokenPair pair : pairs) {
        // check for alternative here!
        final INormalizedToken tableToken = pair.tableToken;
        final INormalizedToken token = pair.witnessToken;
        if (tokenToTable.containsKey(token)) {
          INormalizedToken existingTable = tokenToTable.get(token);
          if (existingTable != tableToken) {
            foundAlternative = true;
          }
        } else {
          tokenToTable.put(token, tableToken);
        }
      }
      // step 3. Decide what to do
      if (!foundAlternative) {
        filteredMatches.add(match);
      } else {
        IndexMatcher.LOG.debug("Phrase '" + witnessPhrase + "' is an alternative! skipping...");
      }
    }
    return filteredMatches;
  }
  
  // check whether this match has an alternative that is equal in weight
  // if so, then skip the alternative!
  // NOTE: multiple witness tokens match with the same table column!
  public static List<PhraseMatch> filterAwaySecondChoicesMultipleTokensOneColumn(List<PhraseMatch> matches) {
    List<PhraseMatch> filteredMatches = Lists.newArrayList();
    final Map<INormalizedToken, INormalizedToken> tableToToken = Maps.newLinkedHashMap();
    for (final PhraseMatch match : matches) {
      // step 1. Gather data
      List<TokenPair> pairs = Lists.newArrayList();
      final IPhrase tablePhrase = match.getTablePhrase();
      final IPhrase witnessPhrase = match.getPhrase();
      final Iterator<INormalizedToken> tokens = witnessPhrase.getTokens().iterator();
      for (final INormalizedToken tableToken : tablePhrase.getTokens()) {
        final INormalizedToken token = tokens.next();
        // skip NullColumn and NullToken
        if (!(tableToken instanceof NullToken)) {
          pairs.add(new TokenPair(tableToken, token));
        }
      }
      // step 2. Look for alternative
      boolean foundAlternative = false;
      for (TokenPair pair : pairs) {
        // check for alternative here!
        final INormalizedToken tableToken = pair.tableToken;
        final INormalizedToken witnessToken = pair.witnessToken;
        if (tableToToken.containsKey(tableToken)) {
          INormalizedToken existingWitnessToken = tableToToken.get(tableToken);
          if (existingWitnessToken != witnessToken) {
            foundAlternative = true;
          }
        } else {
          tableToToken.put(tableToken, witnessToken);
        }
      }
      // step 3. Decide what to do
      if (!foundAlternative) {
        filteredMatches.add(match);
      } else {
        IndexMatcher.LOG.debug("Phrase '" + witnessPhrase + "' is an alternative! skipping...");
      }
    }
    return filteredMatches;
  }

}
