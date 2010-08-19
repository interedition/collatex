package eu.interedition.collatex2.experimental.tokenmatching;

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

import eu.interedition.collatex2.implementation.indexing.NullToken;
import eu.interedition.collatex2.implementation.indexing.WitnessIndex;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.ITokenContainer;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.ITokenMatcher;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.ITokenIndex;

//TODO: remove explicit dependency on NullToken
//TODO: remove explicit dependency on WitnessIndex
public class TokenIndexMatcher implements ITokenMatcher {
  private static final Logger LOG = LoggerFactory.getLogger(TokenIndexMatcher.class);
  private final ITokenContainer base;

  public TokenIndexMatcher(ITokenContainer base) {
    this.base = base;
  }

  public List<ITokenMatch> getMatches(IWitness witness) {
    final List<String> repeatedTokens = combineRepeatedTokens(witness);
    ITokenIndex baseIndex = base.getTokenIndex(repeatedTokens);
    return findMatches(baseIndex, new WitnessIndex(witness, repeatedTokens));
  }
  
  //TODO: change return type from List into Set?
  private List<String> combineRepeatedTokens(final IWitness witness) {
    final Set<String> repeatedTokens = Sets.newHashSet();
    repeatedTokens.addAll(base.getRepeatedTokens());
    repeatedTokens.addAll(witness.findRepeatingTokens());
    return Lists.newArrayList(repeatedTokens);
  }


  private List<ITokenMatch> findMatches(final ITokenIndex tableIndex, final ITokenIndex tokenIndex) {
    final List<PhraseMatch> matches = Lists.newArrayList();
    final Set<String> keys = tokenIndex.keys();
    for (final String key : keys) {
      // IndexMatcher.LOG.debug("Looking for phrase: " + key);
      if (tableIndex.contains(key)) {
        // IndexMatcher.LOG.debug("FOUND!");
        final IPhrase phrase = tokenIndex.getPhrase(key);
        final IPhrase tablePhrase = tableIndex.getPhrase(key);
        matches.add(new PhraseMatch(tablePhrase, phrase));
      }
    }
    TokenIndexMatcher.LOG.debug("unfiltered matches: " + matches);
    return joinOverlappingMatches(matches);
  }

  private List<ITokenMatch> joinOverlappingMatches(final List<PhraseMatch> matches) {
    final List<ITokenMatch> newMatches = filterMatchesBasedOnPositionMatches(matches);
    TokenIndexMatcher.LOG.debug("filtered matches: " + newMatches);
    return newMatches;
  }

  // TODO: make IColumns Iterable!
  // NOTE: There is a potential situation here where 1 column matches with
  // multiple phrases
  // NOTE: The other phrases are seen as additions, which causes too many empty
  // columns
  // NOTE: --> not the optimal alignment
  @SuppressWarnings("boxing")
  private List<ITokenMatch> filterMatchesBasedOnPositionMatches(final List<PhraseMatch> matches) {
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
        // System.out.println(column.getContent() + ":" + column.getSigil() +
        // ":" + position);
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

  // check whether this match has an alternative that is equal in weight
  // if so, then skip the alternative!
  // NOTE: multiple columns match with the same token!
  private List<PhraseMatch> filterAwaySecondChoicesMultipleColumnsOneToken(List<PhraseMatch> matches) {
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
        TokenIndexMatcher.LOG.debug("Phrase '" + witnessPhrase + "' is an alternative! skipping...");
      }
    }
    return filteredMatches;
  }

  // check whether this match has an alternative that is equal in weight
  // if so, then skip the alternative!
  // NOTE: multiple witness tokens match with the same table column!
  private List<PhraseMatch> filterAwaySecondChoicesMultipleTokensOneColumn(List<PhraseMatch> matches) {
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
        TokenIndexMatcher.LOG.debug("Phrase '" + witnessPhrase + "' is an alternative! skipping...");
      }
    }
    return filteredMatches;
  }
}
