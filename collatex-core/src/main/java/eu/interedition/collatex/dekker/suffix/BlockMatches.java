package eu.interedition.collatex.dekker.suffix;

import java.util.Comparator;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.VariantGraph.Vertex;
import eu.interedition.collatex.matching.Matches;

/*
 * Block matches
 * 
 * Creates the matches based on blocks instead of individual tokens
 * @author: Ronald Haentjens Dekker
 * 
 */
public class BlockMatches {
  
  /*
   * Map<Token, Block> 
   * Map<Vertex, Block>
   * build the matches
   */
  public static Matches between(VariantGraph graph, Iterable<Token> witnessTokens, Map<Token, Block> tokenToBlock, Map<Vertex, Block> vertexToBlock, Comparator<Token> comparator) {
    final ListMultimap<Token, VariantGraph.Vertex> all = ArrayListMultimap.create();
    for (Token witnessToken : witnessTokens) {
      Block block = tokenToBlock.get(witnessToken);
      for (VariantGraph.Vertex vertex : vertexToBlock.keySet()) {
        //Note: otherBlock might be null
        Block otherBlock = vertexToBlock.get(vertex);
        if (block!=otherBlock) {
          continue;
        }
        // when the block is the same, do a token comparison
        // first I have to fetch a token from the vertex
        Token vertexToken = vertex.tokens().iterator().next();
        int v = comparator.compare(witnessToken, vertexToken);
        if (v==0) {
          all.put(witnessToken, vertex);
        }
      }
    }
    return Matches.calculateSetsFromAll(graph.vertices(), witnessTokens, all);
  }

// work in progress
// This method takes the occurrences as leading
// It would need a Block --> List<Vertex> map
// and the Block --> List<Token> mapping is gathered from the 
// occurrences from the BlockWitness
  
//  public static BlockMatches between(VariantGraph variantGraph, BlockWitness bw) {
//    Map<VariantGraph.Vertex, Block> blockMap = Maps.newHashMap();
//    // ik moet wel op de een of andere manier zorgen
//    // dat die map wordt gevoed..
//    // Ik zal het moeten externaliseren
//    
//    
//    
//    
//    Iterable<VariantGraph.Vertex> vertices = variantGraph.vertices();
//    // ik kan hier ook over de tokens lopen
//    // en dan het block uit een nog te creeren map halen
//    // en dan de twee vergelijken
//    
//    //    List<Occurrence> occurrences = bw.getOccurrences();
////    // here I step over the occurrences.
////    
////    for (Occurrence occurrence : occurrences) {
////      // ga alle vertices af en kijk of ze in hetzelfde block voorkomen
////      Block witnessBlock = occurrence.getBlock();
////      for (VariantGraph.Vertex v: variantGraph.vertices()) {
////        // haal nu uit de map met welk block een vertex is geassocieerd
////        Block graphBlock = blockMap.get(v);
////      }
////    }
//    return null;
//  }
}
