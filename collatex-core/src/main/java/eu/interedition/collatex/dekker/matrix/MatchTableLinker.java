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

package eu.interedition.collatex.dekker.matrix;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.TokenLinker;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MatchTableLinker implements TokenLinker {
    static Logger LOG = Logger.getLogger(MatchTableLinker.class.getName());

    public MatchTableLinker() {
        super();
    }

    @Override
    public Map<Token, VariantGraph.Vertex> link(VariantGraph base, Iterable<Token> witness, Comparator<Token> comparator) {
        // create MatchTable and fill it with matches
        LOG.fine("create MatchTable and fill it with matches");
        MatchTable table = MatchTable.create(base, witness, comparator);

        // create IslandConflictResolver
        LOG.fine("create island conflict resolver");
        IslandConflictResolver resolver = new IslandConflictResolver(table);

        // The IslandConflictResolver createNonConflictingVersion() method
        // selects the optimal islands
        LOG.fine("select the optimal islands");
        MatchTableSelection preferredIslands = resolver.createNonConflictingVersion();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Number of preferred Islands: {0}", preferredIslands.size());
        }

        // Here the result is put in a map
        Map<Token, VariantGraph.Vertex> map = new HashMap<>();
        for (Island island : preferredIslands.getIslands()) {
            for (Coordinate c : island) {
                map.put(table.tokenAt(c.row, c.column), table.vertexAt(c.row, c.column));
            }
        }
        return map;
    }
}
