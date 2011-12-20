package eu.interedition.collatex;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.Sets;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.dekker.VariantGraphBuilder;
import eu.interedition.collatex.graph.GraphFactory;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphEdge;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.input.SimpleToken;
import eu.interedition.collatex.input.SimpleWitness;
import eu.interedition.collatex.input.WhitespaceTokenizer;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import static eu.interedition.collatex.dekker.Match.PHRASE_MATCH_TO_TOKENS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractTest {
  protected final Logger LOG = LoggerFactory.getLogger(getClass());
  public static final char[] SIGLA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

  protected static GraphFactory graphFactory;
  private Transaction transaction;

  @BeforeClass
  public static void createVariantGraphFactory() throws IOException {
    graphFactory = GraphFactory.create();
  }

  @Before
  public void startGraphTransaction() {
    transaction = graphFactory.getDatabase().beginTx();
  }

  @After
  public void finishGraphTransaction() {
    if (transaction != null) {
      transaction.finish();
      transaction = null;
    }
  }
  protected SimpleWitness[] createWitnesses(Function<String, List<String>> tokenizer, String... contents) {
    Assert.assertTrue("Not enough sigla", contents.length <= SIGLA.length);
    final SimpleWitness[] witnesses = new SimpleWitness[contents.length];
    for (int wc = 0; wc < contents.length; wc++) {
      witnesses[wc] = new SimpleWitness(Character.toString(SIGLA[wc]), contents[wc], tokenizer);
    }
    return witnesses;
  }

  protected SimpleWitness[] createWitnesses(String... contents) {
    return createWitnesses(new WhitespaceTokenizer(), contents);
  }

  protected CollationAlgorithm merge(VariantGraph graph, SimpleWitness... witnesses) {
    final CollationAlgorithm algorithm = CollationAlgorithmFactory.dekker(new EqualityTokenComparator());
    algorithm.collate(graph, witnesses);
    return algorithm;
  }

  protected VariantGraph merge(SimpleWitness... witnesses) {
    final VariantGraph graph = graphFactory.newVariantGraph();
    merge(graph, witnesses);
    return graph;
  }

  protected VariantGraph merge(String... witnesses) {
    return merge(createWitnesses(witnesses));
  }

  protected static SortedSet<String> extractPhrases(VariantGraph graph, Witness witness) {
    return extractPhrases(Sets.<String>newTreeSet(), graph, witness);
  }

  protected static SortedSet<String> extractPhrases(SortedSet<String> phrases, VariantGraph graph, Witness witness) {
    for (VariantGraphVertex v : graph.vertices(Sets.newTreeSet(Collections.singleton(witness)))) {
      phrases.add(toString(v, witness));
    }
    return phrases;
  }

  protected static String toString(VariantGraphVertex vertex, Witness... witnesses) {
    final SortedSet<Token> tokens = vertex.tokens(Sets.newTreeSet(Arrays.asList(witnesses)));
    List<String> tokenContents = Lists.newArrayListWithExpectedSize(tokens.size());
    for (Token token : tokens) {
      tokenContents.add(((SimpleToken) token).getNormalized());
    }
    return Joiner.on(' ').join(tokenContents);
  }

  protected static void assertHasWitnesses(VariantGraphEdge edge, Witness... witnesses) {
    assertEquals(Sets.newTreeSet(Arrays.asList(witnesses)), edge.getWitnesses());
  }

  protected static VariantGraphEdge edgeBetween(VariantGraphVertex start, VariantGraphVertex end) {
    final VariantGraphEdge edge = start.getGraph().edgeBetween(start, end);
    Assert.assertNotNull(String.format("No edge between %s and %s", start, end), edge);
    return edge;
  }

  protected static void assertVertexEquals(String expected, VariantGraphVertex vertex) {
    assertEquals(expected, ((SimpleToken) vertex.tokens().first()).getNormalized());
  }

  protected static void assertVertexHasContent(VariantGraphVertex vertex, String content, Witness in) {
    Assert.assertEquals(String.format("%s does not has expected content for %s", vertex, in), content, toString(vertex, in));
  }

  protected static VariantGraphVertex vertexWith(VariantGraph graph, String content, Witness in) {
    for (VariantGraphVertex v : graph.vertices(Sets.newTreeSet(Collections.singleton(in)))) {
      if (content.equals(toString(v, in))) {
        return v;
      }
    }
    fail(String.format("No vertex with content '%s' in witness %s", content, in));
    return null;
  }

  protected static String toString(RowSortedTable<Integer, Witness, SortedSet<Token>> table) {
    final StringBuilder tableStr = new StringBuilder();
    for (Witness witness : table.columnKeySet()) {
      tableStr.append(witness.getSigil()).append(": ").append(toString(table, witness)).append("\n");
    }
    return tableStr.toString();
  }

  protected static String toString(RowSortedTable<Integer, Witness, SortedSet<Token>> table, Witness witness) {
    final StringBuilder tableRowStr = new StringBuilder("|");
    for (Integer row : table.rowKeySet()) {
      final SortedSet<Token> tokens = table.get(row, witness);
      tableRowStr.append(tokens == null ? ' ' : Joiner.on(" ").join(Iterables.transform(tokens, new Function<Token, String>() {
        @Override
        public String apply(Token input) {
          return ((SimpleToken) input).getContent();
        }
      }))).append("|");
    }
    return tableRowStr.toString();
  }

  @Deprecated
  protected List<SimpleToken> getTokens(VariantGraph graph, Witness... witnesses) {
    final SortedSet<Witness> witnessSet = Sets.newTreeSet(Arrays.asList(witnesses));
    final List<SimpleToken> tokens = Lists.newArrayList();
    for (VariantGraphVertex v : graph.vertices(witnessSet)) {
      Iterables.addAll(tokens, Iterables.filter(v.tokens(witnessSet), SimpleToken.class));
    }
    return tokens;
  }

  protected void assertPhraseMatches(VariantGraphBuilder builder, String... expectedPhrases) {
    List<List<Match>> phraseMatches = builder.getPhraseMatches();
    int i = 0;
    for (List<Match> phraseMatch : phraseMatches) {
      Assert.assertEquals(expectedPhrases[i], SimpleToken.toString(PHRASE_MATCH_TO_TOKENS.apply(phraseMatch)));
      i++;
    }
  }

}
