package eu.interedition.collatex.matching;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.neo4j.Neo4jVariantGraphVertex;
import eu.interedition.collatex.simple.SimpleToken;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

public class Matches {

  private final ListMultimap<Token, Neo4jVariantGraphVertex> all;
  private final Set<Token> unmatched;
  private final Set<Token> ambiguous;
  private final Set<Token> unique;

  public static Matches between(final Iterable<Neo4jVariantGraphVertex> vertices, final Iterable<Token> witnessTokens, Comparator<Token> comparator) {

    final ListMultimap<Token, Neo4jVariantGraphVertex> all = ArrayListMultimap.create();
    for (Neo4jVariantGraphVertex vertex : vertices) {
      final Set<Token> tokens = vertex.tokens();
      if (tokens.isEmpty()) {
        continue;
      }
      for (Token witnessToken : witnessTokens) {
        if (comparator.compare(Iterables.getFirst(tokens, null), witnessToken) == 0) {
          all.put(witnessToken, vertex);
        }
      }
    }

    // unmatched tokens
    Set<Token> unmatched = Sets.newLinkedHashSet();
    for (Token witnessToken : witnessTokens) {
      if (!all.containsKey(witnessToken)) {
        unmatched.add(witnessToken);
      }
    }
    // unsure tokens (have to check: base -> witness, and witness -> base)
    Set<Token> ambiguous = Sets.newLinkedHashSet();
    for (Token witnessToken : witnessTokens) {
      int count = all.keys().count(witnessToken);
      if (count > 1) {
        ambiguous.add(witnessToken);
      }
    }
    Multiset<Neo4jVariantGraphVertex> bag = ImmutableMultiset.copyOf(all.values());
    Set<Neo4jVariantGraphVertex> unsureBaseTokens = Sets.newLinkedHashSet();
    for (Neo4jVariantGraphVertex baseToken : vertices) {
      int count = bag.count(baseToken);
      if (count > 1) {
        unsureBaseTokens.add(baseToken);
      }
    }
    Collection<Map.Entry<Token, Neo4jVariantGraphVertex>> entries = all.entries();
    for (Map.Entry<Token, Neo4jVariantGraphVertex> entry : entries) {
      if (unsureBaseTokens.contains(entry.getValue())) {
        ambiguous.add(entry.getKey());
      }
    }
    // sure tokens
    // have to check unsure tokens because of (base -> witness && witness -> base)
    Set<Token> unique = Sets.newLinkedHashSet();
    for (Token witnessToken : witnessTokens) {
      if (all.keys().count(witnessToken) == 1 && !ambiguous.contains(witnessToken)) {
        unique.add(witnessToken);
      }
    }

    // add start and end tokens as matches
    all.put(SimpleToken.START, Iterables.getFirst(vertices, null));
    all.put(SimpleToken.END, Iterables.getLast(vertices));

    return new Matches(all, unmatched, ambiguous, unique);
  }

  private Matches(ListMultimap<Token, Neo4jVariantGraphVertex> all, Set<Token> unmatched, Set<Token> ambiguous, Set<Token> unique) {
    this.all = all;
    this.unmatched = unmatched;
    this.ambiguous = ambiguous;
    this.unique = unique;
  }

  public ListMultimap<Token, Neo4jVariantGraphVertex> getAll() {
    return all;
  }

  public Set<Token> getUnmatched() {
    return unmatched;
  }

  public Set<Token> getAmbiguous() {
    return ambiguous;
  }

  public Set<Token> getUnique() {
    return unique;
  }

}
