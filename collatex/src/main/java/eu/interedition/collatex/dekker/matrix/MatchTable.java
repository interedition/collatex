package eu.interedition.collatex.dekker.matrix;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Sets;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.matching.Matches;
import eu.interedition.collatex.simple.SimpleWitness;

//Note: this class is intended to replace the current MatchMatrix class
//The current class is limited to pairwise collation
//This one should not be
public class MatchTable {
  static Logger LOG = LoggerFactory.getLogger(MatchTable.class);
  private final ArrayTable<Token, Integer, VariantGraphVertex> table;
  
  public MatchTable(Iterable<Token> tokens, Iterable<Integer> ranks) {
    this.table = ArrayTable.create(tokens, ranks);
  }
  
  // assumes default comparator
  public static MatchTable create(VariantGraph graph, SimpleWitness witness) {
    // step 1: build the MatchMatrix2
    MatchTable table = createEmptyTable(graph, witness);
    // step 2: do the matching and fill the table
    fillTableWithMatches(graph, witness, table);
    return table;
  }

  private static MatchTable createEmptyTable(VariantGraph graph, SimpleWitness simpleWitness) {
    // ik heb een Integer range nodig..
    // dit is best een stupid way om het te doen
    // ik moet een georderde set hebben
    graph.rank();
    Set<Integer> ranks = Sets.newLinkedHashSet();
    Iterator<VariantGraphVertex> vertices = graph.vertices().iterator();
    while(vertices.hasNext()) {
      ranks.add(vertices.next().getRank());
    }
    return new MatchTable(simpleWitness.getTokens(), ranks);
  }

  // remove static; move parameters into fields
  private static void fillTableWithMatches(VariantGraph graph, SimpleWitness witness, MatchTable table) {
    Comparator<Token> comparator = new EqualityTokenComparator();
    Matches matches = Matches.between(graph.vertices(), witness, comparator);
    Set<Token> unique = matches.getUnique();
    Set<Token> ambiguous = matches.getAmbiguous();
    for (Token t : witness) {
      List<VariantGraphVertex> matchingVertices = matches.getAll().get(t);
      //TODO: dit kan simpeler! zie de duplicatie
      if (unique.contains(t)) {
        table.set(t, matchingVertices.get(0).getRank()-1, matchingVertices.get(0));
      } else {
        if (ambiguous.contains(t)) {
          for (VariantGraphVertex vgv : matchingVertices) {
            table.set(t, vgv.getRank()-1, vgv);
          }
        }
      }
    }
  }

  private void set(Token token, int rank, VariantGraphVertex variantGraphVertex) {
    LOG.info("putting: {}<->{}<->{}", new Object[] {token, rank, variantGraphVertex});
    table.put(token, rank, variantGraphVertex);
  }


  public ArrayTable<Token, Integer, VariantGraphVertex> getTable() {
    return table;
  }

}
