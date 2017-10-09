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

import eu.interedition.collatex.dekker.Match;

import java.util.Objects;

public class Coordinate implements Comparable<Coordinate> {
    public int row;
    public int column;
    public Match match;

    public Coordinate(int row, int column) {
        this.column = column;
        this.row = row;
        this.match = null; //TODO: this constructor should be removed
    }

    // row -> position in witness (0+)
    // column -> rank in variant graph (0+)
    // match -> combination of witness token and vertex
    public Coordinate(int row, int column, Match match) {
        this.column = column;
        this.row = row;
        this.match = match;
    }

    Coordinate(Coordinate other) {
        this(other.row, other.column);
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public boolean sameColumn(Coordinate c) {
        return c.column == column;
    }

    public boolean sameRow(Coordinate c) {
        return c.row == row;
    }

    public boolean bordersOn(Coordinate c) {
        return (Math.abs(this.row - c.getRow()) == 1) && (Math.abs(this.column - c.getColumn()) == 1);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null & o instanceof Coordinate) {
            final Coordinate c = (Coordinate) o;
            return (this.row == c.getRow() && this.column == c.getColumn());
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public int compareTo(Coordinate o) {
        final int result = column - o.column;
        return (result == 0 ? row - o.row : result);
    }

    @Override
    public String toString() {
        return "(" + row + "," + column + ")";
    }
}
