package eu.interedition.collatex2.implementation.vg_alignment;

import java.util.*;

import com.google.common.base.Predicates;
import com.google.common.collect.*;
import eu.interedition.collatex2.implementation.matching.EqualityTokenComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.interedition.collatex2.implementation.input.NullToken;
import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.implementation.matching.IMatchResult;
import eu.interedition.collatex2.implementation.matching.MatchResultAnalyzer;
import eu.interedition.collatex2.implementation.matching.TokenMatcher;
import eu.interedition.collatex2.implementation.vg_analysis.Sequence;
import eu.interedition.collatex2.interfaces.ITokenLinker;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class TokenLinker implements ITokenLinker {
  static final Logger LOG = LoggerFactory.getLogger(TokenLinker.class);

  public Map<ITokenSequence, IPhrase> link(IWitness a, IWitness b) {
    // do the matching
    TokenMatcher matcher = new EqualityTokenComparator();
    Multimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b);
    // add start and end tokens as matches
    final INormalizedToken startNullToken = a.getTokens().get(0);
    matches.put(new StartToken(), startNullToken);
    final INormalizedToken endNullToken = a.getTokens().get(a.size()-1);
    matches.put(new EndToken(b.size()), endNullToken);
     // Calculate MatchResult
    MatchResultAnalyzer analyzer = new MatchResultAnalyzer();
    IMatchResult matchResult = analyzer.analyze(a, b);
    // Calculate witness sequences using indexer
    WitnessIndexer indexer = new WitnessIndexer();
    IWitnessIndex index = indexer.index(b, matchResult);
    // Calculate 'derivation': Ignore non matches from the base
    List<INormalizedToken> afgeleide = derive(a, matches);
    // try and find matches in the base for each sequence in the witness 
    Map<ITokenSequence, IPhrase> result = Maps.newLinkedHashMap();
    for (ITokenSequence sequence : index.getTokenSequences()) {
      // System.out.println("Trying to find token sequence: "+sequence.getNormalized());
      List<INormalizedToken> matchedBaseTokens;
      if (sequence.expandsToTheRight()) {
        matchedBaseTokens = findMatchingBaseTokensForSequenceToTheRight(sequence, matches, afgeleide);
      } else {
        matchedBaseTokens = findMatchingBaseTokensForSequenceToTheLeft(sequence, matches, afgeleide);
      }
      if (!matchedBaseTokens.isEmpty()) {
        // System.out.println("Matched!");
        result.put(sequence, new Phrase(matchedBaseTokens));
      }
    }
    return result;
  }
 
  //this method is just a bridge between the old and the new situation
  public Map<INormalizedToken, INormalizedToken> link2(IWitness a, IWitness b) {
    Map<ITokenSequence, IPhrase> linkedSequences = link(a, b);
    // order the sequences here (first the sequences that expand to the left, then all sequences that expand to the right!)
    List<ITokenSequence> sequences0 = orderSequences(linkedSequences.keySet());
    // need to convert map<tokenseq, phrase> to List<Sequence> here
    List<Sequence> sequences = Lists.newArrayList();
    for (ITokenSequence seq : sequences0) {
      IPhrase basePhrase = linkedSequences.get(seq);
      IPhrase witnessPhrase = new Phrase(seq.getTokens());
      Sequence sequence = new Sequence(basePhrase, witnessPhrase);
      sequences.add(sequence);
    }
    // run the old filter method 
    List<Sequence> sequences1 = filterAwaySecondChoicesMultipleColumnsOneToken(sequences);
    List<Sequence> sequences2 = filterAwaySecondChoicesMultipleTokensOneColumn(sequences1);
     // do the matching
    TokenMatcher matcher = new EqualityTokenComparator();
    Multimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b);
    // Calculate MatchResult
    MatchResultAnalyzer analyzer = new MatchResultAnalyzer();
    IMatchResult matchResult = analyzer.analyze(a, b);
    // result map
    Map<INormalizedToken, INormalizedToken> alignedTokens = Maps.newLinkedHashMap();
    // put sure matches in the result map
    for (INormalizedToken token: matchResult.getSureTokens()) {
      alignedTokens.put(token, Iterables.getFirst(matches.get(token), null));
    }
    // add matched sequences to the aligned tokens
    for (Sequence sequence : sequences2) {
      IPhrase matchedBasePhrase = sequence.getBasePhrase();
      Iterator<INormalizedToken> iterator = matchedBasePhrase.getTokens().iterator();
      for (INormalizedToken witnessToken : sequence.getWitnessPhrase().getTokens()) {
        INormalizedToken possibility = iterator.next();
        // skip start and end tokens
        if (!(witnessToken instanceof StartToken || witnessToken instanceof EndToken)) {
          alignedTokens.put(witnessToken, possibility);
        }  
      }  
    }
    return alignedTokens;
  }
  
  private List<ITokenSequence> orderSequences(Set<ITokenSequence> set) {
    Comparator<ITokenSequence> comp = new Comparator<ITokenSequence>() {
      @Override
      public int compare(ITokenSequence o1, ITokenSequence o2) {
        if (o1.expandsToTheRight() == o2.expandsToTheRight()) {
          return 0;
        }
        if (o1.expandsToTheRight() && !o2.expandsToTheRight()) {
          return -1;
        }
        return 1;
      }
    };
    List<ITokenSequence> sorted = Lists.newArrayList(set);
    Collections.sort(sorted, comp);
    return sorted;
  }

  // check whether this match has an alternative that is equal in weight
  // if so, then skip the alternative!
  // NOTE: multiple witness tokens match with the same table column!
  //EXPECT GRAPH -> Witness TOKEN HERE
  private List<Sequence> filterAwaySecondChoicesMultipleTokensOneColumn(List<Sequence> sequences) {
    List<Sequence> filteredMatches = Lists.newArrayList();
    final Map<INormalizedToken, INormalizedToken> tableToToken = Maps.newLinkedHashMap();
    for (final Sequence sequence : sequences) {
      // step 1. Gather data
      List<TokenPair> pairs = Lists.newArrayList();
      final IPhrase tablePhrase = sequence.getBasePhrase();
      final IPhrase witnessPhrase = sequence.getWitnessPhrase();
      final Iterator<INormalizedToken> tokens = witnessPhrase.getTokens().iterator();
      for (final INormalizedToken tableToken : tablePhrase.getTokens()) {
        final INormalizedToken token = tokens.next();
        // skip Start and End Token in variant graph... string equals is not very nice!
        if (!(tableToken.getNormalized().equals("#"))) {
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
        filteredMatches.add(sequence);
      } else {
//        LOG.debug("!Phrase '" + witnessPhrase + "' is an alternative! skipping...");
      }
    }
    return filteredMatches;
  }

  // check whether this match has an alternative that is equal in weight
  // if so, then skip the alternative!
  // NOTE: multiple columns match with the same token!
  // EXPECT WITNESS TOKEN TO GRAPH TOKEN HERE!
  private List<Sequence> filterAwaySecondChoicesMultipleColumnsOneToken(List<Sequence> sequences) {
    List<Sequence> filteredMatches = Lists.newArrayList();
    final Map<INormalizedToken, INormalizedToken> tokenToTable = Maps.newLinkedHashMap();
    for (final Sequence sequence : sequences) {
      // step 1. Gather data
      List<TokenPair> pairs = Lists.newArrayList();
      final IPhrase tablePhrase = sequence.getBasePhrase();
      final IPhrase witnessPhrase = sequence.getWitnessPhrase();
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
        filteredMatches.add(sequence);
      } else {
//        LOG.debug("Phrase '" + witnessPhrase + "' is an alternative! skipping...");
      }
    }
    return filteredMatches;
  }

  
  // This method should return the matching base tokens for a given sequence 
  // Note: this method works for sequences that have the fixed token on the left and expand to the right
  private List<INormalizedToken> findMatchingBaseTokensForSequenceToTheRight(ITokenSequence sequence, Multimap<INormalizedToken, INormalizedToken> matches, List<INormalizedToken> afgeleide) {
    final INormalizedToken fixedWitnessToken = sequence.getFirstToken();
    final Collection<INormalizedToken> matchesForFixedTokenWitness = matches.get(fixedWitnessToken);
    if (matchesForFixedTokenWitness.isEmpty()) {
      throw new RuntimeException("No match found in base for fixed witness token! token: "+fixedWitnessToken);
    }
    if (matchesForFixedTokenWitness.size()!=1) {
      throw new RuntimeException("Multiple matches found in base for fixed witness token! tokens: "+fixedWitnessToken);
    }
    INormalizedToken fixedBaseToken = Iterables.getFirst(matchesForFixedTokenWitness, null);
    // traverse here the rest of the token sequence
    List<INormalizedToken> restTokens = Lists.newArrayList(sequence.getTokens());
    restTokens.remove(fixedWitnessToken);
    return tryTheDifferentPossibilities(matches, afgeleide, fixedBaseToken, restTokens, 1);
  }

  // This method should return the matching base tokens for a given sequence 
  // Note: this method works for sequences that have the fixed token on the right and expand to the left
  private List<INormalizedToken> findMatchingBaseTokensForSequenceToTheLeft(ITokenSequence sequence, Multimap<INormalizedToken, INormalizedToken> matches, List<INormalizedToken> afgeleide) {
    final INormalizedToken fixedWitnessToken = sequence.getLastToken();
    final Collection<INormalizedToken> matchesForFixedTokenWitness = matches.get(fixedWitnessToken);
    if (matchesForFixedTokenWitness.isEmpty()) {
      throw new RuntimeException("No match found in base for fixed witness token! token: "+fixedWitnessToken);
    }
    if (matchesForFixedTokenWitness.size()!=1) {
      throw new RuntimeException("Multiple matches found in base for fixed witness token! tokens: "+fixedWitnessToken);
    }
    INormalizedToken fixedBaseToken = Iterables.getFirst(matchesForFixedTokenWitness, null);
    // traverse here the rest of the token sequence
    List<INormalizedToken> restTokens = Lists.newArrayList(sequence.getTokens());
    restTokens.remove(fixedWitnessToken);
    Collections.reverse(restTokens);
    final List<INormalizedToken> matchedBaseTokens = tryTheDifferentPossibilities(matches, afgeleide, fixedBaseToken, restTokens, -1);
    Collections.reverse(matchedBaseTokens);
    return matchedBaseTokens;
  }

  private List<INormalizedToken> tryTheDifferentPossibilities(Multimap<INormalizedToken, INormalizedToken> matches, List<INormalizedToken> afgeleide, INormalizedToken fixedBaseToken,
      List<INormalizedToken> restTokens, int expectedDirection) {
    boolean validWholeSequence = true;
    List<INormalizedToken> matchedBaseTokens = Lists.newArrayList(fixedBaseToken);
    INormalizedToken lastToken = fixedBaseToken;
    for (INormalizedToken token : restTokens) {
      Collection<INormalizedToken> possibilities = matches.get(token);
      boolean valid = false;
      for (INormalizedToken possibility : possibilities) {
        int direction = afgeleide.indexOf(possibility) - afgeleide.indexOf(lastToken);
        if (direction == expectedDirection) {
          matchedBaseTokens.add(possibility);
          lastToken = possibility;
          valid = true;
          break;
        }
      }
      validWholeSequence = valid;
      if (!valid) {
        break;
      }
    }  
    if (validWholeSequence) {
      return matchedBaseTokens;
    } 
    return Collections.emptyList();
  }

  @Override
  public Map<INormalizedToken, INormalizedToken> link(IVariantGraph graph, IWitness b) {
    IWitness superbase = new Superbase(graph);
    return link2(superbase, b);
  }

  public static List<INormalizedToken> derive(IWitness a, Multimap<INormalizedToken, INormalizedToken> matches) {
    return Lists.newArrayList(Iterables.filter(a.getTokens(), Predicates.in(matches.values())));
  }
}
