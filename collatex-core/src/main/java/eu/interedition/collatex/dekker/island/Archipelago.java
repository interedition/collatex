/*
 * Copyright (c) 2015 The Interedition Development Group.
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

package eu.interedition.collatex.dekker.island;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/*
 * @author Meindert Kroese
 * @author Bram Buitendijk
 * @author Ronald Haentjens Dekker
 */
public class Archipelago {

    private final List<Island> islands;
    private final Set<Integer> islandvectors;

    public Archipelago() {
        islands = new ArrayList<>();
        this.islandvectors = new HashSet<>(); // row - column, all islands should have direction 1, so this diff should be the same for all coordinates on the island.
    }

    //copy constructor
    public Archipelago(Archipelago orig) {
        this.islands = new ArrayList<>(orig.islands);
        this.islandvectors = new HashSet<>(orig.islandvectors);
    }

    public void add(Island island) {
        islands.add(island);
        Coordinate leftEnd = island.getLeftEnd();
        islandvectors.add(leftEnd.row - leftEnd.column);
    }

    public int size() {
        return islands.size();
    }

    public Island get(int i) {
        return islands.get(i);
    }

    public boolean containsCoordinate(int row, int column) {
        return Objects.equals(getCoordinatesMap().get(row), column);
    }

    public List<Island> getIslands() {
        return islands;
    }

    protected void remove(int i) {
        islands.remove(i);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Island island : getIslands()) {
            if (result.length() == 0)
                result = new StringBuilder("[ " + island);
            else
                result.append(", ").append(island);
        }
        result.append(" ]");
        return result.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(islands);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (object.getClass() != this.getClass()) return false;
        if (((Archipelago) object).size() != this.size()) return false;
        for (int i = 0; i < size(); i++) {
            if (!((Archipelago) object).get(i).equals(get(i))) return false;
        }
        return true;
    }

    private Map<Integer, Integer> getCoordinatesMap() {
        final Map<Integer, Integer> map = new HashMap<>();
        for (final Island isl : islands) {
            for (final Coordinate c : isl) {
                map.put(c.getRow(), c.getColumn());
            }
        }
        return map;
    }

    public Set<Integer> getIslandVectors() {
        return islandvectors;
    }

}
