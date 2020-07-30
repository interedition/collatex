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
package eu.interedition.collatex.dekker.island

import eu.interedition.collatex.dekker.token_index.Block
import eu.interedition.collatex.simple.SimpleToken
import java.util.*

class Island : Iterable<Coordinate?> {
    private val islandCoordinates: MutableList<Coordinate> = ArrayList()
    private val blockInstance: Block.Instance?

    constructor(blockInstance: Block.Instance?) {
        this.blockInstance = blockInstance
    }

    // for legacy code
    constructor() {
        blockInstance = null
    }

    // for legacy code
    constructor(first: Coordinate, last: Coordinate) {
        blockInstance = null
        add(first)
        var newCoordinate = first
        while (newCoordinate != last) {
            newCoordinate = Coordinate(newCoordinate.getRow() + 1, newCoordinate.getColumn() + 1)
            //        LOG.debug("{}", newCoordinate);
            add(newCoordinate)
        }
    }

    fun add(coordinate: Coordinate) {
        islandCoordinates.add(coordinate)
    }

    fun removeCoordinate(c: Coordinate) {
        islandCoordinates.remove(c)
    }

    /**
     * Two islands are competitors if there is a horizontal or
     * vertical line which goes through both islands
     */
    fun isCompetitor(isl: Island): Boolean {
        for (c in isl) {
            for (d in islandCoordinates) {
                if (c.sameColumn(d) || c.sameRow(d)) return true
            }
        }
        return false
    }

    operator fun contains(c: Coordinate): Boolean {
        return islandCoordinates.contains(c)
    }

    val leftEnd: Coordinate
        get() {
            var coor = islandCoordinates[0]
            for (c in islandCoordinates) {
                if (c.column < coor.column) coor = c
            }
            return coor
        }
    val rightEnd: Coordinate
        get() {
            var coor = islandCoordinates[0]
            for (c in islandCoordinates) {
                if (c.column > coor.column) coor = c
            }
            return coor
        }

    fun size(): Int {
        return islandCoordinates.size
    }

    override fun iterator(): MutableIterator<Coordinate> {
        return Collections.unmodifiableList(islandCoordinates).iterator()
    }

    override fun hashCode(): Int {
        return islandCoordinates.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj.javaClass != Island::class.java) return false
        val isl = obj as Island
        if (isl.size() != size()) return false
        var result = true
        for (c in isl) {
            result = result and this.contains(c)
        }
        return result
    }

    fun getBlockInstance(): Block.Instance {
        if (blockInstance == null) {
            throw RuntimeException("Block instance is not set on this island! It is probably constructed with legacy code!")
        }
        return blockInstance
    }

    val depth: Int
        get() = getBlockInstance().block.getDepth()

    override fun toString(): String {
        val instance = getBlockInstance().tokens
        val normalized = StringBuilder()
        for (i in 0 until size()) {
            val t = instance[i]
            val st = t as SimpleToken
            if (normalized.length > 0) {
                normalized.append(" ")
            }
            normalized.append(st.normalized)
        }
        return normalized.toString()

//        if (islandCoordinates.isEmpty()) {
//            //throw new RuntimeException("Unexpected situation: island coordinates empty!");
//            return "Island has been modified after creation and has become empty!";
//        }
//        return MessageFormat.format("Island ({0}-{1}) size: {2}", islandCoordinates.get(0), islandCoordinates.get(islandCoordinates.size() - 1), size());
    }
}