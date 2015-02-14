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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

/*
 * @author Meindert Kroese
 * @author Bram Buitendijk
 * @author Ronald Haentjens Dekker
 */
public class Archipelago {
    Logger LOG = Logger.getLogger(Archipelago.class.getName());

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

    public Archipelago(Island isl) {
        this();
        islands.add(isl);
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
        String result = "";
        for (Island island : getIslands()) {
            if (result.isEmpty())
                result = "[ " + island;
            else
                result += ", " + island;
        }
        result += " ]";
        return result;
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

    private double distance(Island isl1, Island isl2) {
        double result = 0.0;
        int isl1_L_x = isl1.getLeftEnd().column;
        int isl1_L_y = isl1.getLeftEnd().row;
        int isl1_R_x = isl1.getRightEnd().column;
        int isl1_R_y = isl1.getRightEnd().row;
        int isl2_L_x = isl2.getLeftEnd().column;
        int isl2_L_y = isl2.getLeftEnd().row;
        int isl2_R_x = isl2.getRightEnd().column;
        int isl2_R_y = isl2.getRightEnd().row;
        result = distance(isl1_L_x, isl1_L_y, isl2_L_x, isl2_L_y);
        double d = distance(isl1_L_x, isl1_L_y, isl2_R_x, isl2_R_y);
        if (d < result) result = d;
        d = distance(isl1_R_x, isl1_R_y, isl2_L_x, isl2_L_y);
        if (d < result) result = d;
        d = distance(isl1_R_x, isl1_R_y, isl2_R_x, isl2_R_y);
        if (d < result) result = d;
        return result;
    }

    private double distance(int a_x, int a_y, int b_x, int b_y) {
        double result = 0.0;
        result = Math.sqrt((a_x - b_x) * (a_x - b_x) + (a_y - b_y) * (a_y - b_y));
        return result;
    }

    public Set<Integer> getIslandVectors() {
        return islandvectors;
    }

    public double smallestDistance(Island isl) {
        double minimum = 10000;
        for (Island fixedIsland : getIslands()) {
            minimum = Math.min(minimum, distance(isl, fixedIsland));
        }
        return minimum;
    }

    public double smallestDistanceToIdealLine(Island isl) {
        double minimum = 10000;
        Island closestIsland = null;
        for (Island fixedIsland : getIslands()) {
            double prev = minimum;
            minimum = Math.min(minimum, distance(isl, fixedIsland));
            if (prev > minimum) {
                closestIsland = fixedIsland;
            }
        }
        if (closestIsland == null) {
            return minimum;
        }
        Coordinate leftEnd = isl.getLeftEnd();
        int islandVector = leftEnd.row - leftEnd.column;
        Coordinate leftEnd0 = closestIsland.getLeftEnd();
        int closestIslandVector = leftEnd0.row - leftEnd0.column;
        return Math.abs(islandVector - closestIslandVector);
    }
}
