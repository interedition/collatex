package eu.interedition.collatex2;

import eu.interedition.collatex2.implementation.alignment.VariantGraphBuilder;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraph;
import eu.interedition.collatex2.implementation.input.builders.WitnessBuilder;
import eu.interedition.collatex2.implementation.input.tokenization.DefaultTokenNormalizer;
import eu.interedition.collatex2.implementation.input.tokenization.WhitespaceTokenizer;
import eu.interedition.collatex2.implementation.output.table.RankedGraphBasedAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ITokenizer;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;
import org.junit.Assert;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractTest {

  public static final char[] SIGLA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

  protected WitnessBuilder witnessBuilder = new WitnessBuilder(new DefaultTokenNormalizer());
  protected ITokenizer tokenizer = new WhitespaceTokenizer();

  protected IWitness[] createWitnesses(String... contents) {
    Assert.assertTrue("Not enough sigla", contents.length <= SIGLA.length);
    final IWitness[] witnesses = new IWitness[contents.length];
    for (int wc = 0; wc < contents.length; wc++) {
      witnesses[wc] = witnessBuilder.build(Character.toString(SIGLA[wc]), contents[wc], tokenizer);
    }
    return witnesses;
  }

  protected IVariantGraph collate(IWitness... witnesses) {
    final VariantGraph graph = new VariantGraph();
    new VariantGraphBuilder(graph).add(witnesses);
    return graph;
  }

  protected IVariantGraph collate(String... witnesses) {
    return collate(createWitnesses(witnesses));
  }

  protected IAlignmentTable toAlignmentTable(IVariantGraph graph) {
    return new RankedGraphBasedAlignmentTable(graph);
  }

  protected IAlignmentTable toAlignmentTable(IWitness... witnesses) {
    return new RankedGraphBasedAlignmentTable(collate(witnesses));
  }

  protected IAlignmentTable toAlignmentTable(String... witnesses) {
    return new RankedGraphBasedAlignmentTable(collate(witnesses));
  }

}
