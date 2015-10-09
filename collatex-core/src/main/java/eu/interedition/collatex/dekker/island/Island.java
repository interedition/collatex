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

import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.token_index.Block;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Island implements Iterable<Coordinate> {

    private final List<Coordinate> islandCoordinates = new ArrayList<>();
    private final int depth;
    private final Block.Instance blockInstance;

    public Island(int depth, Block.Instance blockInstance) {
        this.depth = depth;
        this.blockInstance = blockInstance;
    }

    // for legacy code
    public Island() {
        depth = 0;
        blockInstance = null;
    }

    // for legacy code
    public Island(Coordinate first, Coordinate last) {
        depth = 0;
        blockInstance = null;
        add(first);
        Coordinate newCoordinate = first;
        while (!newCoordinate.equals(last)) {
            newCoordinate = new Coordinate(newCoordinate.getRow() + 1, newCoordinate.getColumn() + 1);
            //        LOG.debug("{}", newCoordinate);
            add(newCoordinate);
        }
    }

    public void add(Coordinate coordinate) {
        islandCoordinates.add(coordinate);
    }

    public void removeCoordinate(Coordinate c) {
        islandCoordinates.remove(c);
    }

    /**
     * Two islands are competitors if there is a horizontal or
     * vertical line which goes through both islands
     */
    public boolean isCompetitor(Island isl) {
        for (Coordinate c : isl) {
            for (Coordinate d : islandCoordinates) {
                if (c.sameColumn(d) || c.sameRow(d)) return true;
            }
        }
        return false;
    }

    public boolean contains(Coordinate c) {
        return islandCoordinates.contains(c);
    }

    public Coordinate getLeftEnd() {
        Coordinate coor = islandCoordinates.get(0);
        for (Coordinate c : islandCoordinates) {
            if (c.column < coor.column) coor = c;
        }
        return coor;
    }

    public Coordinate getRightEnd() {
        Coordinate coor = islandCoordinates.get(0);
        for (Coordinate c : islandCoordinates) {
            if (c.column > coor.column) coor = c;
        }
        return coor;
    }

    public int size() {
        return islandCoordinates.size();
    }

    @Override
    public Iterator<Coordinate> iterator() {
        return Collections.unmodifiableList(islandCoordinates).iterator();
    }

    @Override
    public int hashCode() {
        return islandCoordinates.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (!obj.getClass().equals(Island.class)) return false;

        Island isl = (Island) obj;
        if (isl.size() != size()) return false;

        boolean result = true;
        for (Coordinate c : isl) {
            result &= this.contains(c);
        }
        return result;
    }

    public int getDepth() {
        if (depth == 0) {
            throw new RuntimeException("Depth value is not set on this island! It is probably constructed with legacy code!");
        }
        return depth;
    }

    @Override
    public String toString() {
        List<Token> instance = getBlockInstance().getTokens();
        String result = "";
        for (int i = 0; i < this.size(); i++) {
            result += instance.get(i)+" ";
        }
        return result;

//        if (islandCoordinates.isEmpty()) {
//            //throw new RuntimeException("Unexpected situation: island coordinates empty!");
//            return "Island has been modified after creation and has become empty!";
//        }
//        return MessageFormat.format("Island ({0}-{1}) size: {2}", islandCoordinates.get(0), islandCoordinates.get(islandCoordinates.size() - 1), size());
    }

    public Block.Instance getBlockInstance() {
        if (blockInstance == null) {
            throw new RuntimeException("Block instance is not set on this island! It is probably constructed with legacy code!");
        }
        return blockInstance;
    }
}
