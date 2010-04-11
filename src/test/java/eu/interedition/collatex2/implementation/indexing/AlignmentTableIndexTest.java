package eu.interedition.collatex2.implementation.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.implementation.alignmenttable.AlignmentTableCreator3;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableIndex;
import eu.interedition.collatex2.interfaces.ICallback;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableIndexTest {
  protected static final Log LOG = LogFactory.getLog(AlignmentTableIndexTest.class);
  private static final ICallback CALLBACK = new ICallback() {
    @Override
    public void alignment(final IAlignment alignment) {
      LOG.info(alignment.getMatches());
    }
  };
  private Factory factory;

  @Before
  public void setup() {
    factory = new Factory();
  }

  @Test
  public void test() {
    final IWitness witnessA = factory.createWitness("A", "the big black cat and the big black rat");
    final IAlignmentTable table = AlignmentTableCreator3.createAlignmentTable(Lists.newArrayList(witnessA), CALLBACK);
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
    final List<IWitness> list = Lists.newArrayList(a);
    final IAlignmentTable table = factory.createAlignmentTable(list);
    final IAlignmentTableIndex index = AlignmentTableIndex.create(table, table.findRepeatingTokens());
    assertEquals("AlignmentTableIndex: (the, first, witness)", index.toString());
  }

  @Test
  public void testCreateAlignmentTableIndexWithVariation() {
    final IWitness a = factory.createWitness("A", "the first witness");
    final IWitness b = factory.createWitness("B", "the second witness");
    final List<IWitness> list = Lists.newArrayList(a, b);
    final IAlignmentTable table = factory.createAlignmentTable(list);
    final IAlignmentTableIndex index = AlignmentTableIndex.create(table, table.findRepeatingTokens());
    assertEquals("AlignmentTableIndex: (the, first, witness, second)", index.toString());
  }

  @Test
  public void testAlignmentTableIndex() {
    final IWitness a = factory.createWitness("A", "first");
    final IWitness b = factory.createWitness("B", "second");
    final IWitness c = factory.createWitness("C", "third");
    final List<IWitness> list = Lists.newArrayList(a, b, c);
    final IAlignmentTable table = factory.createAlignmentTable(list);
    final IAlignmentTableIndex index = AlignmentTableIndex.create(table, table.findRepeatingTokens());
    assertEquals("AlignmentTableIndex: (first, second, third)", index.toString());
  }

  @Test
  public void testAlignmentTableIndex2() {
    final IWitness a = factory.createWitness("A", "first");
    final IWitness b = factory.createWitness("B", "match");
    final IWitness c = factory.createWitness("C", "match");
    final List<IWitness> list = Lists.newArrayList(a, b, c);
    final IAlignmentTable table = factory.createAlignmentTable(list);
    final IAlignmentTableIndex index = AlignmentTableIndex.create(table, table.findRepeatingTokens());
    assertEquals("AlignmentTableIndex: (first, match)", index.toString());
  }

}
