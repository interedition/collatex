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

package eu.interedition.collatex.simple;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.VariantGraph;

import java.util.List;

public class SimpleCollation {

  private final List<SimpleWitness> witnesses;
  private final CollationAlgorithm algorithm;
  private final boolean joined;

  public SimpleCollation(List<SimpleWitness> witnesses, CollationAlgorithm algorithm, boolean joined) {
    this.witnesses = witnesses;
    this.algorithm = algorithm;
    this.joined = joined;
  }

  public List<SimpleWitness> getWitnesses() {
    return witnesses;
  }

  public CollationAlgorithm getAlgorithm() {
    return algorithm;
  }

  public boolean isJoined() {
    return joined;
  }

  public VariantGraph collate(VariantGraph graph) {
    for (SimpleWitness witness : witnesses) {
      algorithm.collate(graph, witness);
    }
    if (joined) {
      VariantGraph.JOIN.apply(graph);
    }
    return graph;
  }
}
