package eu.interedition.collatex2.experimental.vgalignment;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.interedition.collatex2.alignment.AlignmentTest;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.PairwiseAlignmentHelper;
import eu.interedition.collatex2.interfaces.IAddition;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IWitness;

//TODO: these test have to be rewritten to work with the 
//variant graph based alignment
public class VGAlignmentGapsTest {
  private static final Logger LOG = LoggerFactory.getLogger(AlignmentTest.class);
  private CollateXEngine      factory;

  @Before
  public void setup() {
    factory = new CollateXEngine();
  }

  @Test
  public void testSimple1() {
    final IWitness a = factory.createWitness("A", "a b");
    final IWitness b = factory.createWitness("B", "a c b");
    final IAlignment ali = PairwiseAlignmentHelper.align(factory, a, b);
    final List<IGap> gaps = ali.getGaps();
    assertEquals(1, gaps.size());
    assertEquals("\"c\" added", gaps.get(0).toString());
  }

  @Test
  public void testAlignment2Gaps() {
    final IWitness a = factory.createWitness("A", "The black cat");
    final IWitness b = factory.createWitness("B", "The black and white cat");
    final IAlignment alignment = PairwiseAlignmentHelper.align(factory, a, b);
    final List<IGap> gaps = alignment.getGaps();
    assertEquals(1, gaps.size());
    final IGap gap = gaps.get(0);
    assertTrue(gap.isAddition());
    assertTrue("Phrase A is not empty!", gap.getColumns().isEmpty());
    assertEquals("and white", gap.getPhrase().getNormalized());
  }

  public void testAddition_AtTheStart() {
    final IWitness a = factory.createWitness("A", "to be");
    final IWitness b = factory.createWitness("B", "not to be");
    final IAlignment alignment = PairwiseAlignmentHelper.align(factory, a, b);
    final List<IGap> gaps = alignment.getGaps();
    assertEquals(1, gaps.size());
    final IGap gap = gaps.get(0);
    assertTrue(gap.isAddition());
    assertTrue("Phrase A is not empty!", gap.getColumns().isEmpty());
    assertEquals("not", gap.getPhrase().getNormalized());
    final List<IAddition> additions = alignment.getAdditions();
    assertEquals(1, additions.size());
    final IAddition addition = additions.get(0);
    assertEquals("not", addition.getAddedPhrase().getNormalized());
  }
  
  @Test
  public void testAddition_AtTheEnd() {
    final IWitness a = factory.createWitness("A", "to be");
    final IWitness b = factory.createWitness("B", "to be or not");
    final IAlignment alignment = PairwiseAlignmentHelper.align(factory, a, b);
    final List<IGap> gaps = alignment.getGaps();
    assertEquals(1, gaps.size());
    final IGap gap = gaps.get(0);
    assertTrue(gap.isAddition());
    assertTrue("Phrase A is not empty!", gap.getColumns().isEmpty());
    assertEquals("or not", gap.getPhrase().getNormalized());
    final List<IAddition> additions = alignment.getAdditions();
    assertEquals(1, additions.size());
    final IAddition addition = additions.get(0);
    assertEquals("or not", addition.getAddedPhrase().getNormalized());
  }

  @Test
  public void testAddition_InTheMiddle() {
    final IWitness a = factory.createWitness("A", "to be");
    final IWitness b = factory.createWitness("B", "to think, therefore be");
    final IAlignment alignment = PairwiseAlignmentHelper.align(factory, a, b);
    final List<IGap> gaps = alignment.getGaps();
    assertEquals(1, gaps.size());
    final IGap gap = gaps.get(0);
    assertTrue(gap.isAddition());
    assertTrue("Phrase A is not empty!", gap.getColumns().isEmpty());
    assertEquals("think therefore", gap.getPhrase().getNormalized());
    final List<IAddition> additions = alignment.getAdditions();
    assertEquals(1, additions.size());
    final IAddition addition = additions.get(0);
    assertEquals("think therefore", addition.getAddedPhrase().getNormalized());
  }

}
