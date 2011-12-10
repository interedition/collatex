package eu.interedition.collatex;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.Sets;
import eu.interedition.collatex.implementation.alignment.VariantGraphBuilder;
import eu.interedition.collatex.implementation.alignment.VariantGraphWitnessAdapter;
import eu.interedition.collatex.implementation.graph.db.VariantGraph;
import eu.interedition.collatex.implementation.graph.db.VariantGraphEdge;
import eu.interedition.collatex.implementation.graph.db.VariantGraphVertex;
import eu.interedition.collatex.implementation.graph.db.VariantGraphFactory;
import eu.interedition.collatex.implementation.input.DefaultTokenNormalizer;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.implementation.input.WhitespaceTokenizer;
import eu.interedition.collatex.implementation.input.WitnessBuilder;
import eu.interedition.collatex.interfaces.Token;
import eu.interedition.collatex.interfaces.ITokenizer;
import eu.interedition.collatex.interfaces.IWitness;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractTest {
  protected final Logger LOG = LoggerFactory.getLogger(getClass());
  public static final char[] SIGLA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

  protected WitnessBuilder witnessBuilder = new WitnessBuilder(new DefaultTokenNormalizer());
  protected ITokenizer tokenizer = new WhitespaceTokenizer();
  protected static VariantGraphFactory variantGraphFactory;
  private Transaction transaction;

  @BeforeClass
  public static void createVariantGraphFactory() throws IOException {
    variantGraphFactory = new VariantGraphFactory();
  }

  @Before
  public void startGraphTransaction() {
    transaction = variantGraphFactory.getDb().beginTx();
  }

  @After
  public void finishGraphTransaction() {
    if (transaction != null) {
      transaction.finish();
      transaction = null;
    }
  }
  protected IWitness[] createWitnesses(WitnessBuilder witnessBuilder, ITokenizer tokenizer, String... contents) {
    Assert.assertTrue("Not enough sigla", contents.length <= SIGLA.length);
    final IWitness[] witnesses = new IWitness[contents.length];
    for (int wc = 0; wc < contents.length; wc++) {
      witnesses[wc] = witnessBuilder.build(Character.toString(SIGLA[wc]), contents[wc], tokenizer);
    }
    return witnesses;
  }

  protected IWitness[] createWitnesses(String... contents) {
    return createWitnesses(witnessBuilder, tokenizer, contents);
  }

  protected VariantGraphBuilder merge(VariantGraph graph, IWitness... witnesses) {
    final VariantGraphBuilder builder = new VariantGraphBuilder(graph);
    builder.add(witnesses);
    return builder;
  }

  protected VariantGraph merge(IWitness... witnesses) {
    final VariantGraph graph = variantGraphFactory.create();
    merge(graph, witnesses);
    return graph;
  }

  protected VariantGraph merge(String... witnesses) {
    return merge(createWitnesses(witnesses));
  }

  protected static SortedSet<String> extractPhrases(VariantGraph graph, IWitness witness) {
    return extractPhrases(Sets.<String>newTreeSet(), graph, witness);
  }

  protected static SortedSet<String> extractPhrases(SortedSet<String> phrases, VariantGraph graph, IWitness witness) {
    for (VariantGraphVertex v : graph.vertices(Sets.newTreeSet(Collections.singleton(witness)))) {
      phrases.add(toString(v, witness));
    }
    return phrases;
  }

  protected static String toString(VariantGraphVertex vertex, IWitness... witnesses) {
    final SortedSet<Token> tokens = vertex.tokens(Sets.newTreeSet(Arrays.asList(witnesses)));
    List<String> tokenContents = Lists.newArrayListWithExpectedSize(tokens.size());
    for (Token token : tokens) {
      tokenContents.add(((SimpleToken) token).getNormalized());
    }
    return Joiner.on(' ').join(tokenContents);
  }

  protected static void assertHasWitnesses(VariantGraphEdge edge, IWitness... witnesses) {
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

  protected static VariantGraphVertex vertexWith(VariantGraph graph, String content, IWitness in) {
    for (VariantGraphVertex v : graph.vertices(Sets.newTreeSet(Collections.singleton(in)))) {
      if (content.equals(toString(v, in))) {
        return v;
      }
    }
    fail(String.format("No vertex with content '%s' in witness %s", content, in));
    return null;
  }

  protected static String toString(RowSortedTable<Integer, IWitness, SortedSet<Token>> table) {
    final StringBuilder tableStr = new StringBuilder();
    for (IWitness witness : table.columnKeySet()) {
      tableStr.append(witness.getSigil()).append(": ").append(toString(table, witness)).append("\n");
    }
    return tableStr.toString();
  }

  protected static String toString(RowSortedTable<Integer, IWitness, SortedSet<Token>> table, IWitness witness) {
    final StringBuilder tableRowStr = new StringBuilder("|");
    for (Integer row : table.rowKeySet()) {
      final SortedSet<Token> tokens = table.get(row, witness);
      tableRowStr.append(tokens == null ? ' ' : Joiner.on(" ").join(Iterables.transform(tokens, new Function<Token, String>() {
        @Override
        public String apply(Token input) {
          return input.getContent();
        }
      }))).append("|");
    }
    return tableRowStr.toString();
  }

  @Deprecated
  protected List<SimpleToken> getTokens(VariantGraph graph, IWitness... witnesses) {
    final SortedSet<IWitness> witnessSet = Sets.newTreeSet(Arrays.asList(witnesses));
    final List<SimpleToken> tokens = Lists.newArrayList();
    for (VariantGraphVertex v : graph.vertices(witnessSet)) {
      Iterables.addAll(tokens, Iterables.filter(v.tokens(witnessSet), SimpleToken.class));
    }
    return tokens;
  }
}
