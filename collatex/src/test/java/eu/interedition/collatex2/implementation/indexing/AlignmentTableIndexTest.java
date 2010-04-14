package eu.interedition.collatex2.implementation.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAligner;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableIndex;
import eu.interedition.collatex2.interfaces.ICallback;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableIndexTest {
  protected static final Logger LOG = LoggerFactory.getLogger(AlignmentTableIndexTest.class);
  private CollateXEngine factory;

  @Before
  public void setup() {
    factory = new CollateXEngine();
  }

  private IAligner createAligner() {
    IAligner aligner = factory.createAligner();
    aligner.setCallback(new ICallback() {
      @Override
      public void alignment(final IAlignment alignment) {
        System.out.println(alignment.getMatches());
      }
    });
    return aligner;
  }

  @Test
  public void test() {
    final IWitness witnessA = factory.createWitness("A", "the big black cat and the big black rat");
    final IAlignmentTable table = createAligner().add(witnessA).getResult();
    final IAlignmentTableIndex index = AlignmentTableIndex.create(table, table.findRepeatingTokens());
    assertTrue(index.containsNormalizedPhrase("# the"));
    assertTrue(index.containsNormalizedPhrase("# the big"));
    assertTrue(index.containsNormalizedPhrase("# the big black"));
    assertTrue(index.containsNormalizedPhrase("the big black cat"));
    assertTrue(index.containsNormalizedPhrase("big black cat"));
    assertTrue(index.containsNormalizedPhrase("black cat"));
    assertTrue(index.containsNormalizedPhrase("cat"));
    assertTrue(index.containsNormalizedPhrase("and"));
    assertTrue(index.containsNormalizedPhrase("and the"));
    assertTrue(index.containsNormalizedPhrase("and the big"));
    assertTrue(index.containsNormalizedPhrase("and the big black"));
    assertTrue(index.containsNormalizedPhrase("the big black rat"));
    assertTrue(index.containsNormalizedPhrase("big black rat"));
    assertTrue(index.containsNormalizedPhrase("black rat"));
    assertTrue(index.containsNormalizedPhrase("rat"));
    assertEquals(15, index.size());

    final IColumns columns = index.getColumns("the big black cat");
    assertEquals(1, columns.getBeginPosition());
    assertEquals(4, columns.getEndPosition());
  }

  @Test
  public void testCreateAlignmentTableIndex() {
    final IWitness a = factory.createWitness("A", "the first witness");
    final IAlignmentTable table = factory.align(a);
    final IAlignmentTableIndex index = AlignmentTableIndex.create(table, table.findRepeatingTokens());
    assertEquals("AlignmentTableIndex: (the, first, witness)", index.toString());
  }

  @Test
  public void testCreateAlignmentTableIndexWithVariation() {
    final IWitness a = factory.createWitness("A", "the first witness");
    final IWitness b = factory.createWitness("B", "the second witness");
    final IAlignmentTable table = factory.align(a, b);
    final IAlignmentTableIndex index = AlignmentTableIndex.create(table, table.findRepeatingTokens());
    assertEquals("AlignmentTableIndex: (the, first, witness, second)", index.toString());
  }

  @Test
  public void testAlignmentTableIndex() {
    final IWitness a = factory.createWitness("A", "first");
    final IWitness b = factory.createWitness("B", "second");
    final IWitness c = factory.createWitness("C", "third");
    final IAlignmentTable table = factory.align(a, b, c);
    final IAlignmentTableIndex index = AlignmentTableIndex.create(table, table.findRepeatingTokens());
    assertEquals("AlignmentTableIndex: (first, second, third)", index.toString());
  }

  @Test
  public void testAlignmentTableIndex2() {
    final IWitness a = factory.createWitness("A", "first");
    final IWitness b = factory.createWitness("B", "match");
    final IWitness c = factory.createWitness("C", "match");
    final IAlignmentTable table = factory.align(a, b, c);
    final IAlignmentTableIndex index = AlignmentTableIndex.create(table, table.findRepeatingTokens());
    assertEquals("AlignmentTableIndex: (first, match)", index.toString());
  }

}
