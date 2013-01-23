package eu.interedition.collatex.medite;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.util.VariantGraphRanking;
import org.junit.Test;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MediteTest extends AbstractTest {

  @Override
  public void initAlgorithm() {
    collationAlgorithm = CollationAlgorithmFactory.medite(new EqualityTokenComparator());
  }

  @Test
  public void medite() {
    LOG.fine(toString(VariantGraphRanking.of(collate(
            "This Carpenter hadde wedded newe a wyf",
            "This Carpenter hadde wedded a newe wyf",
            "This Carpenter hadde newe wedded a wyf",
            "This Carpenter hadde wedded newly a wyf",
            "This Carpenter hadde E wedded newe a wyf",
            "This Carpenter hadde newli wedded a wyf",
            "This Carpenter hadde wedded a wyf"
    )).asTable()));
  }
}
