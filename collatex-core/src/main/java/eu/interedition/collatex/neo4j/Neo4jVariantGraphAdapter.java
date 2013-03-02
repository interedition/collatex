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

package eu.interedition.collatex.neo4j;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface Neo4jVariantGraphAdapter {

  Set<Token> getTokens(Neo4jVariantGraphVertex vertex, Set<Witness> witnesses);

  void setTokens(Neo4jVariantGraphVertex vertex, Set<Token> tokens);

  Set<Witness> getWitnesses(Neo4jVariantGraphEdge edge);

  void setWitnesses(Neo4jVariantGraphEdge edge, Set<Witness> witnesses);

}
