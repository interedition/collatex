package eu.interedition.collatex2.implementation.vg_alignment;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.containers.graph.VariantGraphIndex;
import eu.interedition.collatex2.implementation.containers.witness.WitnessIndex;
import eu.interedition.collatex2.implementation.input.NullToken;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.ITokenIndex;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.ITokenMatcher;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

//TODO: Use AlternativeTokenIndexMatcher when
//TODO: AlternativeVariantGraphIndex is ready!
//TODO: remove explicit dependency on NullToken
public class TokenIndexMatcher implements ITokenMatcher {
  static final Logger LOG = LoggerFactory.getLogger(TokenIndexMatcher.class);
  private final IVariantGraph base;

  public TokenIndexMatcher(IVariantGraph base) {
    this.base = base;
  }

  @Override
  public List<ITokenMatch> getMatches(IWitness witness) {
    final List<String> repeatedTokens = combineRepeatedTokens(witness);
    ITokenIndex baseIndex = new VariantGraphIndex(base, repeatedTokens);
    ITokenIndex witnessIndex = new WitnessIndex(witness, repeatedTokens);
    return findMatches(baseIndex, witnessIndex, witness);
  }
  
  //TODO: change return type from List into Set?
  private List<String> combineRepeatedTokens(final IWitness witness) {
    final Set<String> repeatedTokens = Sets.newHashSet();
    repeatedTokens.addAll(base.getRepeatedTokens());
    repeatedTokens.addAll(witness.getRepeatedTokens());
    return Lists.newArrayList(repeatedTokens);
  }


  private List<ITokenMatch> findMatches(final ITokenIndex tableIndex, final ITokenIndex tokenIndex, IWitness witness) {
    final List<Sequence> matches = Lists.newArrayList();
    final Set<String> keys = tokenIndex.keys();
    for (final String key : keys) {
      // IndexMatcher.LOG.debug("Looking for phrase: " + key);
      if (tableIndex.contains(key)) {
        // IndexMatcher.LOG.debug("FOUND!");
        final IPhrase phrase = tokenIndex.getPhrase(key);
        final IPhrase tablePhrase = tableIndex.getPhrase(key);
        matches.add(new Sequence(tablePhrase, phrase));
      }
    }
    LOG.debug("unfiltered matches: " + matches);
    return joinOverlappingMatches(matches, witness);
  }

  private List<ITokenMatch> joinOverlappingMatches(final List<Sequence> matches, IWitness witness) {
    final List<ITokenMatch> newMatches = filterMatchesBasedOnPositionMatches(matches, witness);
    LOG.debug("filtered matches: " + newMatches);
    return newMatches;
  }

  // TODO: make IColumns Iterable!
  // NOTE: There is a potential situation here where 1 column matches with
  // multiple phrases
  // NOTE: The other phrases are seen as additions, which causes too many empty
  // columns
  // NOTE: --> not the optimal alignment
  @SuppressWarnings("boxing")
  private List<ITokenMatch> filterMatchesBasedOnPositionMatches(final List<Sequence> matches, IWitness witness) {
    Map<INormalizedToken, INormalizedToken> witnessToTable;
    witnessToTable = Maps.newLinkedHashMap();
    List<Sequence> filteredMatches = filterAwaySecondChoicesMultipleTokensOneColumn(filterAwaySecondChoicesMultipleColumnsOneToken(matches));
    for (final Sequence match : filteredMatches) {
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
        witnessToTable.put(token, column);
      }
    }

    final List<ITokenMatch> newMatches = Lists.newArrayList();
    for (final INormalizedToken token : witness.getTokens()) {
      final INormalizedToken tableToken = witnessToTable.get(token);
      if (tableToken!=null) {
        final ITokenMatch newMatch = new TokenMatch(tableToken, token);
        newMatches.add(newMatch);
      }
    }
    return newMatches;
 }

  // check whether this match has an alternative that is equal in weight
  // if so, then skip the alternative!
  // NOTE: multiple columns match with the same token!
  private List<Sequence> filterAwaySecondChoicesMultipleColumnsOneToken(List<Sequence> matches) {
    List<Sequence> filteredMatches = Lists.newArrayList();
    final Map<INormalizedToken, INormalizedToken> tokenToTable = Maps.newLinkedHashMap();
    for (final Sequence match : matches) {
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
        LOG.debug("Phrase '" + witnessPhrase + "' is an alternative! skipping...");
      }
    }
    return filteredMatches;
  }

  // check whether this match has an alternative that is equal in weight
  // if so, then skip the alternative!
  // NOTE: multiple witness tokens match with the same table column!
  private List<Sequence> filterAwaySecondChoicesMultipleTokensOneColumn(List<Sequence> matches) {
    List<Sequence> filteredMatches = Lists.newArrayList();
    final Map<INormalizedToken, INormalizedToken> tableToToken = Maps.newLinkedHashMap();
    for (final Sequence match : matches) {
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
        LOG.debug("Phrase '" + witnessPhrase + "' is an alternative! skipping...");
      }
    }
    return filteredMatches;
  }
  
}
