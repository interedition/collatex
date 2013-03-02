/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

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
