package eu.interedition.collatex;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.interedition.collatex.implementation.alignment.VariantGraphBuilder;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraph;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphEdge;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphVertex;
import eu.interedition.collatex.implementation.graph.db.VariantGraphFactory;
import eu.interedition.collatex.implementation.input.DefaultTokenNormalizer;
import eu.interedition.collatex.implementation.input.WhitespaceTokenizer;
import eu.interedition.collatex.implementation.input.WitnessBuilder;
import eu.interedition.collatex.implementation.output.AlignmentTable;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.ITokenizer;
import eu.interedition.collatex.interfaces.IWitness;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractTest {

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
    transaction = variantGraphFactory.newTransaction();
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

  protected VariantGraphBuilder merge(PersistentVariantGraph graph, IWitness... witnesses) {
    final VariantGraphBuilder builder = new VariantGraphBuilder(graph);
    builder.add(witnesses);
    return builder;
  }

  protected PersistentVariantGraph merge(IWitness... witnesses) {
    final PersistentVariantGraph graph = variantGraphFactory.create();
    merge(graph, witnesses);
    return graph;
  }

  protected PersistentVariantGraph merge(String... witnesses) {
    return merge(createWitnesses(witnesses));
  }

  protected AlignmentTable align(PersistentVariantGraph graph) {
    return new AlignmentTable(graph);
  }

  protected AlignmentTable align(IWitness... witnesses) {
    return new AlignmentTable(merge(witnesses));
  }

  protected AlignmentTable align(String... witnesses) {
    return new AlignmentTable(merge(witnesses));
  }

  protected static SortedSet<String> extractPhrases(PersistentVariantGraph graph, IWitness witness) {
    return extractPhrases(Sets.<String>newTreeSet(), graph, witness);
  }

  protected static SortedSet<String> extractPhrases(SortedSet<String> phrases, PersistentVariantGraph graph, IWitness witness) {
    for (PersistentVariantGraphVertex v : graph.traverseVertices(Sets.newTreeSet(Collections.singleton(witness)))) {
      phrases.add(toString(v, witness));
    }
    return phrases;
  }

  protected static String toString(PersistentVariantGraphVertex vertex, IWitness... witnesses) {
    final SortedSet<INormalizedToken> tokens = vertex.getTokens(Sets.newTreeSet(Arrays.asList(witnesses)));
    List<String> tokenContents = Lists.newArrayListWithExpectedSize(tokens.size());
    for (INormalizedToken token : tokens) {
      tokenContents.add(token.getNormalized());
    }
    return Joiner.on(' ').join(tokenContents);
  }

  protected static void assertHasWitnesses(PersistentVariantGraphEdge edge, IWitness... witnesses) {
    assertEquals(Sets.newTreeSet(Arrays.asList(witnesses)), edge.getWitnesses());
  }

  protected static PersistentVariantGraphEdge edgeBetween(PersistentVariantGraphVertex start, PersistentVariantGraphVertex end) {
    final PersistentVariantGraphEdge edge = start.getGraph().edgeBetween(start, end);
    Assert.assertNotNull(String.format("No edge between %s and %s", start, end), edge);
    return edge;
  }

  protected static PersistentVariantGraphVertex vertexWith(PersistentVariantGraph graph, String content, IWitness in) {
    for (PersistentVariantGraphVertex v : graph.traverseVertices(Sets.newTreeSet(Collections.singleton(in)))) {
      if (content.equals(toString(v, in))) {
        return v;
      }
    }
    fail(String.format("No vertex with content '%s' in witness %s", content, in));
    return null;
  }

  @Deprecated
  protected List<INormalizedToken> getTokens(PersistentVariantGraph graph, IWitness... witnesses) {
    final SortedSet<IWitness> witnessSet = Sets.newTreeSet(Arrays.asList(witnesses));
    final List<INormalizedToken> tokens = Lists.newArrayList();
    for (PersistentVariantGraphVertex v : graph.traverseVertices(witnessSet)) {
      tokens.addAll(v.getTokens(witnessSet));
    }
    return tokens;
  }
}
