package eu.interedition.collatex.dekker.suffix;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.dekker.PhraseMatchDetector;
import eu.interedition.collatex.dekker.TranspositionDetector;
import eu.interedition.collatex.dekker.matrix.IslandConflictResolver;
import eu.interedition.collatex.dekker.matrix.MatchTable;
import eu.interedition.collatex.dekker.matrix.MatchTableSelection;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.matching.Matches;
import eu.interedition.collatex.util.VariantGraphRanking;

public class DekkerOrderIndependentAlgorithm extends CollationAlgorithm.Base {
  // new
  private List<Block> blocks;
  private List<BlockWitness> blockWitnesses;
  private Map<Token, Block> tokenToBlock;
  private Map<VariantGraph.Vertex, Block> vertexToBlock;

  // shared with DekkerAlgorithm class.
  private final Comparator<Token> comparator;
  private final PhraseMatchDetector phraseMatchDetector;
  private final TranspositionDetector transpositionDetector;

  // shared with DekkerAlgorithm class.
  private Map<Token, VariantGraph.Vertex> tokenLinks;
  private List<List<Match>> phraseMatches;
  private List<List<Match>> transpositions;
  private Map<Token, VariantGraph.Vertex> alignments;
  private boolean mergeTranspositions = false;

  public DekkerOrderIndependentAlgorithm(Comparator<Token> comparator) {
    this.comparator = comparator;
    this.transpositionDetector = new TranspositionDetector();
    this.phraseMatchDetector = new PhraseMatchDetector();
  }
  
  @Override
  public void collate(VariantGraph graph, List<? extends Iterable<Token>> witnesses) {
    createBlockWitnesses(witnesses);
    
    
    // add vertices for the first witness to the graph
    Iterable<Token> first = witnesses.get(0);
    
    merge(graph, first, Collections.<Token, VariantGraph.Vertex>emptyMap());
    //VariantGraphBuilder.addFirstWitnessToGraph(graph, first);

    // get first block witness
    BlockWitness firstBw = blockWitnesses.get(0);
    tokenToBlock = firstBw.transformIntoTokenBlockMap();
    
    vertexToBlock = Maps.newHashMap();
    
    for (Token t : first) {
      VariantGraph.Vertex nVertex = witnessTokenVertices.get(t);
      Block block = tokenToBlock.get(t);
      vertexToBlock.put(nVertex, block);
    }
    
    // now the first witness is done
    // for the other witnesses we have to do the matching..
    
    for (int i=1; i< witnesses.size(); i++) {
      Iterable<Token> witnessTokens = witnesses.get(i);
      collate(graph, witnessTokens);
    }
    
  }

  private void createBlockWitnesses(List<? extends Iterable<Token>> witnesses) {
    MultipleWitnessSequence s = MultipleWitnessSequence.createSequenceFromMultipleWitnesses(new EqualityTokenComparator(), witnesses);
    TokenSuffixArrayNaive sa = new TokenSuffixArrayNaive(s);
    LCPArray lcp = new LCPArray(s, sa, new EqualityTokenComparator());
    SuperMaximumRepeats b = new SuperMaximumRepeats();
    blocks = b.calculateBlocks(sa, lcp, s);
//    for (Block block : blocks) {
//      block.debug();
//    }
    List<Occurrence> allOccurences = Lists.newArrayList();
    for (Block block : blocks) {
      allOccurences.addAll(block.getOccurances());
    }
    Collections.sort(allOccurences);
    // System.out.println(allOccurences);
    Stack<Occurrence> todo = new Stack<Occurrence>();
    todo.addAll(allOccurences);
    while(!todo.isEmpty()) {
      Occurrence pop = todo.remove(0);
      BlockWitness current = s.getBlockWitnessForStartPosition(pop.lowerEndpoint());
      current.addOccurence(pop);
    }
    blockWitnesses = s.getBlockWitnesses();
  }
  
  @Override
  public void collate(VariantGraph graph, Iterable<Token> tokens) {
    if (blockWitnesses==null) {
      throw new UnsupportedOperationException("This is not supported; non progressive aligner!");
    }
 
    // here we build the block matches
    LOG.fine("calculating potential matches");
    Matches matches = BlockMatches.between(graph, tokens, tokenToBlock, vertexToBlock, comparator);
    
    // create MatchTable and fill it with matches
    LOG.fine("create MatchTable and fill it with matches");
    MatchTable table = MatchTable.create(graph, tokens, matches);

    // create IslandConflictResolver
    LOG.fine("create island conflict resolver");
    // Do not care about the outlier transposition limit for now
    int outlierTranspositionsSizeLimit = -1;
    IslandConflictResolver resolver = new IslandConflictResolver(table, outlierTranspositionsSizeLimit);
  
    // The IslandConflictResolver createNonConflictingVersion() method
    // selects the optimal islands
    LOG.fine("select the optimal islands");
    MatchTableSelection preferredIslands = resolver.createNonConflictingVersion();
    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "Number of preferred Islands: {0}", preferredIslands.size());
    }

    tokenLinks = preferredIslands.getMatches();

    
    // default stuff follows...
    
//    if (LOG.isLoggable(Level.FINE)) {
//      LOG.log(Level.FINE, "{0} + {1}: Detect phrase matches", new Object[] { graph, witness });
//    }
    phraseMatches = phraseMatchDetector.detect(tokenLinks, graph, tokens);
//    if (LOG.isLoggable(Level.FINER)) {
//      for (List<Match> phraseMatch : phraseMatches) {
//        LOG.log(Level.FINER, "{0} + {1}: Phrase match: {2}", new Object[] { graph, witness, Iterables.toString(phraseMatch) });
//      }
//    }

//    if (LOG.isLoggable(Level.FINE)) {
//      LOG.log(Level.FINE, "{0} + {1}: Detect transpositions", new Object[] { graph, witness });
//    }
    transpositions = transpositionDetector.detect(phraseMatches, graph);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "transpositions:{0}", transpositions);
    }

//    if (LOG.isLoggable(Level.FINER)) {
//      for (List<Match> transposition : transpositions) {
//        LOG.log(Level.FINER, "{0} + {1}: Transposition: {2}", new Object[] { graph, witness, Iterables.toString(transposition) });
//      }
//    }

//    if (LOG.isLoggable(Level.FINE)) {
//      LOG.log(Level.FINE, "{0} + {1}: Determine aligned tokens by filtering transpositions", new Object[] { graph, witness });
//    }
    alignments = Maps.newHashMap();
    for (List<Match> phrase : phraseMatches) {
      for (Match match : phrase) {
        alignments.put(match.token, match.vertex);
      }
    }

    for (List<Match> transposedPhrase : transpositions) {
      for (Match match : transposedPhrase) {
        alignments.remove(match.token);
      }
    }
//    if (LOG.isLoggable(Level.FINER)) {
//      for (Map.Entry<Token, VariantGraph.Vertex> alignment : alignments.entrySet()) {
//        LOG.log(Level.FINER, "{0} + {1}: Alignment: {2} = {3}", new Object[] { graph, witness, alignment.getValue(), alignment.getKey() });
//      }
//    }

    merge(graph, tokens, alignments);

    // we filter out small transposed phrases over large distances
    List<List<Match>> falseTranspositions = Lists.newArrayList();
    
    VariantGraphRanking ranking = VariantGraphRanking.of(graph);
    
    for (List<Match> transposedPhrase : transpositions) {
      Match match = transposedPhrase.get(0);
      VariantGraph.Vertex v1 = witnessTokenVertices.get(match.token);
      VariantGraph.Vertex v2 = match.vertex;
      int distance = Math.abs(ranking.apply(v1)-ranking.apply(v2))-1;
      if (distance > transposedPhrase.size()*3) {
        falseTranspositions.add(transposedPhrase);
      }
    }

    for (List<Match> transposition : falseTranspositions) {
      transpositions.remove(transposition);
    }

    if (mergeTranspositions) {
      mergeTranspositions(graph, transpositions);
    }
    
    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "!{0}: {1}", new Object[] {graph, Iterables.toString(graph.vertices())});
    }

  }

  public List<Block> getBlocks() {
    return blocks;
  }

  public List<BlockWitness> getBlockWitnesses() {
    return blockWitnesses;
  }
}
