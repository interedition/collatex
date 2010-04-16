package eu.interedition.collatex2.implementation.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAligner;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableIndex;
import eu.interedition.collatex2.interfaces.ICallback;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableIndex0Test {
  static Logger logger = LoggerFactory.getLogger(AlignmentTableIndex0Test.class);
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
        logger.debug(alignment.getMatches().toString());
      }
    });
    return aligner;
  }

  @Ignore
  @Test
  public void test1() {
    final IWitness witnessA = factory.createWitness("A", "the big black cat and the big black rat");
    final IAlignmentTable table = createAligner().add(witnessA).getResult();
    final IAlignmentTableIndex index = new AlignmentTableIndex0(table);
    assertTrue(index.containsNormalizedPhrase("cat"));
    assertTrue(index.containsNormalizedPhrase("and"));
    assertTrue(index.containsNormalizedPhrase("rat"));
    assertEquals(15, index.size());
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
