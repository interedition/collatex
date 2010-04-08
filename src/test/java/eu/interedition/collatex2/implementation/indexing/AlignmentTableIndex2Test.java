package eu.interedition.collatex2.implementation.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.implementation.alignmenttable.AlignmentTableCreator3;
import eu.interedition.collatex2.interfaces.IAligmentTableIndex;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ICallback;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableIndex2Test {
  private static final ICallback CALLBACK = new ICallback() {
    @Override
    public void alignment(final IAlignment alignment) {
      System.out.println(alignment.getMatches());
    }
  };
  private Factory factory;

  @Before
  public void setup() {
    factory = new Factory();
  }

  @Test
  public void test1() {
    final IWitness witnessA = factory.createWitness("A", "the big black cat and the big black rat");
    final IAlignmentTable table = AlignmentTableCreator3.createAlignmentTable(Lists.newArrayList(witnessA), CALLBACK);
    final IAligmentTableIndex index = AlignmentTableIndex2.create(table);
    assertTrue(index.containsNormalizedPhrase("cat"));
    assertTrue(index.containsNormalizedPhrase("and"));
    assertTrue(index.containsNormalizedPhrase("rat"));
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
  }
}
