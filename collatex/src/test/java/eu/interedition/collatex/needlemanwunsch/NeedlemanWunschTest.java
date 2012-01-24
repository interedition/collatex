package eu.interedition.collatex.needlemanwunsch;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import org.junit.Test;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NeedlemanWunschTest extends AbstractTest {

  @Test
  public void simple() {
    final CollationAlgorithm collator = CollationAlgorithmFactory.needlemanWunsch(new EqualityTokenComparator());
    collator.collate(graphFactory.newVariantGraph(), createWitnesses("a b a b a", "a b a"));
  }
}
