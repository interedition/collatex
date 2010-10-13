package eu.interedition.collatex2.implementation.vg_analysis;

import static junit.framework.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import eu.interedition.collatex2.experimental.vg_alignment.IAlignment2;
import eu.interedition.collatex2.experimental.vg_alignment.ITransposition2;
import eu.interedition.collatex2.experimental.vg_alignment.VariantGraphAligner;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraph2;
import eu.interedition.collatex2.implementation.vg_analysis.ISequence;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

//NOTE: these unit tests replace the tests in AlignmentTest!
//TODO: have to be rewritten to work with VariantGraph alignment!
public class VGAnalysisTest {
  private static final Logger LOG = LoggerFactory.getLogger(VGAnalysisTest.class);
  private CollateXEngine factory;

  @Before
  public void setup() {
    factory = new CollateXEngine();
  }

  @Test
  public void testSimple1() {
    final IWitness a = factory.createWitness("A", "a b");
    final IWitness b = factory.createWitness("B", "a c b");
    IVariantGraph graph = factory.graph(a);
    IAnalysis analysis = factory.analyse(graph, b);
    final List<ISequence> matches = analysis.getSequences();
    assertEquals(2, matches.size());
    assertEquals("a", matches.get(0).getNormalized());
    assertEquals("b", matches.get(1).getNormalized());
  }
  
  //Copied from TextAlignmentTest
  @Test
  public void testAlignment() {
    final IWitness a = factory.createWitness("A", "cat");
    final IWitness b = factory.createWitness("B", "cat");
    IVariantGraph graph = factory.graph(a);
    IAnalysis analysis = factory.analyse(graph, b);
    final List<ISequence> sequences = analysis.getSequences();
    assertEquals(1, sequences.size());
    assertEquals("cat", sequences.get(0).getNormalized());
  }

  @Test
  public void testAlignment2Sequences() {
    final IWitness a = factory.createWitness("A", "The black cat");
    final IWitness b = factory.createWitness("B", "The black and white cat");
    IVariantGraph graph = factory.graph(a);
    IAnalysis analysis = factory.analyse(graph, b);
    final List<ISequence> sequences = analysis.getSequences();
    assertEquals(2, sequences.size());
    assertEquals("the black", sequences.get(0).getNormalized());
    assertEquals("cat", sequences.get(1).getNormalized());
  }

  // Note: taken from TextAlignmentTest!
  @Test
  public void testAddition_AtTheStart() {
    final IWitness a = factory.createWitness("A", "to be");
    final IWitness b = factory.createWitness("B", "not to be");
    IVariantGraph graph = factory.graph(a);
    IAnalysis analysis = factory.analyse(graph, b);
    final List<ISequence> sequences = analysis.getSequences();
    assertEquals(1, sequences.size());
    assertEquals("to be", sequences.get(0).getNormalized());
  }

  @Test
  public void testAddition_AtTheEnd() {
    final IWitness a = factory.createWitness("A", "to be");
    final IWitness b = factory.createWitness("B", "to be or not");
    IVariantGraph graph = factory.graph(a);
    IAnalysis analysis = factory.analyse(graph, b);
    final List<ISequence> sequences = analysis.getSequences();
    assertEquals(1, sequences.size());
    assertEquals("to be", sequences.get(0).getNormalized());
  }

  @Test
  public void testAddition_InTheMiddle() {
    final IWitness a = factory.createWitness("A", "to be");
    final IWitness b = factory.createWitness("B", "to think, therefore be");
    IVariantGraph graph = factory.graph(a);
    IAnalysis analysis = factory.analyse(graph, b);
    final List<ISequence> sequences = analysis.getSequences();
    assertEquals(2, sequences.size());
    assertEquals("to", sequences.get(0).getNormalized());
    assertEquals("be", sequences.get(1).getNormalized());
  }

  @Test
  public void testTransposition1Sequences() {
    final IWitness a = factory.createWitness("A", "The black dog chases a red cat.");
    final IWitness b = factory.createWitness("B", "A red cat chases the black dog.");
    IVariantGraph graph = factory.graph(a);
    IAnalysis analysis = factory.analyse(graph, b);
    final List<ISequence> sequences = analysis.getSequences();
    assertEquals("a red cat", sequences.get(0).getNormalized());
    assertEquals("chases", sequences.get(1).getNormalized());
    assertEquals("the black dog", sequences.get(2).getNormalized());
  }

  @Test
  public void testTransposition2Sequences() {
    final IWitness a = factory.createWitness("A", "d a b");
    final IWitness b = factory.createWitness("B", "a c b d");
    IVariantGraph graph = factory.graph(a);
    IAnalysis analysis = factory.analyse(graph, b);
    final List<ISequence> sequences = analysis.getSequences();
    assertEquals(3, sequences.size());
    assertEquals("a", sequences.get(0).getNormalized());
    assertEquals("b", sequences.get(1).getNormalized());
    assertEquals("d", sequences.get(2).getNormalized());
  }

  @Test
  public void testTransposition1() {
    final IWitness a = factory.createWitness("A", "d a b");
    final IWitness b = factory.createWitness("B", "a b d");
    IVariantGraph graph = VariantGraph2.create(a);
    VariantGraphAligner aligner = new VariantGraphAligner(graph);
    IAlignment2 alignment = aligner.align(b);
    final List<ITransposition2> transpositions = alignment.getTranspositions();
    assertEquals(2, transpositions.size());
    assertEquals("a b", transpositions.get(0).getMatchB().getNormalized());
    assertEquals("d", transpositions.get(0).getMatchA().getNormalized());
    assertEquals("d", transpositions.get(1).getMatchB().getNormalized());
    assertEquals("a b", transpositions.get(1).getMatchA().getNormalized());
  }

  @Test
  public void testTransposition2() {
    final IWitness a = factory.createWitness("A", "d a b");
    final IWitness b = factory.createWitness("B", "a c b d");
    IVariantGraph graph = VariantGraph2.create(a);
    VariantGraphAligner aligner = new VariantGraphAligner(graph);
    IAlignment2 alignment = aligner.align(b);
    final List<ITransposition2> transpositions = alignment.getTranspositions();
    assertEquals(3, transpositions.size());
    assertEquals("d", transpositions.get(0).getMatchA().getNormalized());
    assertEquals("a", transpositions.get(0).getMatchB().getNormalized());
    assertEquals("a", transpositions.get(1).getMatchA().getNormalized());
    assertEquals("b", transpositions.get(1).getMatchB().getNormalized());
    assertEquals("b", transpositions.get(2).getMatchA().getNormalized());
    assertEquals("d", transpositions.get(2).getMatchB().getNormalized());
  }

  @Test
  public void testTransposition3() {
    final IWitness a = factory.createWitness("1", "a b x c d e");
    final IWitness b = factory.createWitness("2", "c e y a d b");
    IVariantGraph graph = VariantGraph2.create(a);
    VariantGraphAligner aligner = new VariantGraphAligner(graph);
    IAlignment2 alignment = aligner.align(b);
    final List<ITransposition2> transpositions = alignment.getTranspositions();
    LOG.debug("transpositions=[" + Joiner.on(", ").join(Iterables.transform(transpositions, new Function<ITransposition2, String>() {
      @Override
      public String apply(final ITransposition2 from) {
        return from.getMatchA().getNormalized() + "=>" + from.getMatchB().getNormalized();
      }
    })) + "]");
    assertEquals(4, transpositions.size());
    assertEquals("a", transpositions.get(0).getMatchA().getNormalized());
    assertEquals("c", transpositions.get(0).getMatchB().getNormalized());
    assertEquals("b", transpositions.get(1).getMatchA().getNormalized());
    assertEquals("e", transpositions.get(1).getMatchB().getNormalized());
    assertEquals("c", transpositions.get(2).getMatchA().getNormalized());
    assertEquals("a", transpositions.get(2).getMatchB().getNormalized());
    assertEquals("e", transpositions.get(3).getMatchA().getNormalized());
    assertEquals("b", transpositions.get(3).getMatchB().getNormalized());
  }


}
