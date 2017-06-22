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

package eu.interedition.collatex.dekker.legacy;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.dekker.island.Coordinate;
import eu.interedition.collatex.dekker.island.Island;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.matching.Matches;
import eu.interedition.collatex.util.StreamUtil;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/* @author: Ronald Haentjens Dekker
*
* This class represents a table of the matches.
* Since this table is sparse a Hashmap based implementation
* is used rather than a Arraylist based one.
* However the API of this class looks very much like an array based one
* since you can use tokenAt(row, column) or vertexAt(row, column).
* This class is read only.
* Selections of vectors from the table can be made using the
* IslandCollection class.
*/
public class MatchTableImpl implements MatchTable {
    private final MatchTableCell[][] table;
    private final Token[] witness;
    private final int[] ranks;

    // assumes default token comparator
    public static MatchTable create(VariantGraph graph, Iterable<Token> witness) {
        Comparator<Token> comparator = new EqualityTokenComparator();
        return MatchTableImpl.create(graph, witness, comparator);
    }

    public static MatchTableImpl create(VariantGraph graph, Iterable<Token> witness, Comparator<Token> comparator) {
        final VariantGraphRanking ranking = VariantGraphRanking.of(graph);
        // step 1: build the MatchTable
        MatchTableImpl table = createEmptyTable(ranking, graph, witness);
        // step 2: do the matching and fill the table
        table.fillTableWithMatches(ranking, graph, witness, comparator);
        return table;
    }

    private Optional<MatchTableCell> cell(int rowIndex, int columnIndex) {
        return Optional.ofNullable(table[rowIndex][columnIndex]);
    }

    @Override
    public VariantGraph.Vertex vertexAt(int rowIndex, int columnIndex) {
        return cell(rowIndex, columnIndex).map(c -> c.vertex).orElse(null);
    }

    @Override
    public Token tokenAt(int rowIndex, int columnIndex) {
        return cell(rowIndex, columnIndex).map(c -> c.token).orElse(null);
    }

    // Warning: this method reiterates the witness!
    // This method is only meant for the user interface and serialization classes!
    // Use the tokenAt method in all other cases.
    @Override
    public List<Token> rowList() {
        return Collections.unmodifiableList(Arrays.asList(witness));
    }

    @Override
    public List<Integer> columnList() {
        return Arrays.stream(ranks).boxed().collect(Collectors.toList());
    }

    // Since the coordinates in allMatches are ordered from upper left to lower right,
    // we don't need to check the lower right neighbor.
    @Override
    public Set<Island> getIslands() {
        Map<Coordinate, Island> coordinateMapper = new HashMap<>();
        List<Coordinate> allMatches = allMatches();
        for (Coordinate c : allMatches) {
            //      LOG.debug("coordinate {}", c);
            addToIslands(coordinateMapper, c);
        }
        Set<Coordinate> smallestIslandsCoordinates = new HashSet<>(allMatches);
        smallestIslandsCoordinates.removeAll(coordinateMapper.keySet());
        for (Coordinate coordinate : smallestIslandsCoordinates) {
            Island island = new Island();
            island.add(coordinate);
            coordinateMapper.put(coordinate, island);
        }
        return new HashSet<>(coordinateMapper.values());
    }


    private MatchTableImpl(Token[] tokens, int[] ranks) {
        this.table = new MatchTableCell[tokens.length][ranks.length];
        this.witness = tokens;
        this.ranks = ranks;
    }

    private static MatchTableImpl createEmptyTable(VariantGraphRanking ranking, VariantGraph graph, Iterable<Token> witness) {
        // -2 === ignore the start and the end vertex
        return new MatchTableImpl(
                StreamUtil.stream(witness).toArray(Token[]::new),
                IntStream.range(0, Math.max(0, ranking.apply(graph.getEnd()) - 1)).toArray()
        );
    }

    // move parameters into fields?
    private void fillTableWithMatches(VariantGraphRanking ranking, VariantGraph graph, Iterable<Token> witness, Comparator<Token> comparator) {
        Matches matches = Matches.between(graph.vertices(), witness, comparator);
        Set<Token> unique = matches.uniqueInWitness;
        Set<Token> ambiguous = matches.ambiguousInWitness;
        int rowIndex = 0;
        for (Token t : witness) {
            if (unique.contains(t) || ambiguous.contains(t)) {
                List<VariantGraph.Vertex> matchingVertices = matches.allMatches.getOrDefault(t, Collections.emptyList());
                for (VariantGraph.Vertex vgv : matchingVertices) {
                    set(rowIndex, ranking.apply(vgv) - 1, t, vgv);
                }
            }
            rowIndex++;
        }
    }

    private void set(int rowIndex, int columnIndex, Token token, VariantGraph.Vertex vertex) {
        //    LOG.debug("putting: {}<->{}<->{}", new Object[] { token, columnIndex, variantGraphVertex });
        table[rowIndex][columnIndex] = new MatchTableCell(token, vertex);
    }

    private void addToIslands(Map<Coordinate, Island> coordinateMapper, Coordinate c) {
        int diff = -1;
        VariantGraph.Vertex neighbor = null;
        try {
            neighbor = vertexAt(c.row + diff, c.column + diff);
        } catch (IndexOutOfBoundsException e) {
            // ignored
        }
        if (neighbor != null) {
            Coordinate neighborCoordinate = new Coordinate(c.row + diff, c.column + diff, new Match(neighbor, null));
            Island island = coordinateMapper.get(neighborCoordinate);
            if (island == null) {
                //        LOG.debug("new island");
                Island island0 = new Island();
                island0.add(neighborCoordinate);
                island0.add(c);
                coordinateMapper.put(neighborCoordinate, island0);
                coordinateMapper.put(c, island0);
            } else {
                //        LOG.debug("add to existing island");
                island.add(c);
                coordinateMapper.put(c, island);
            }
        }
    }

    // Note: code taken from MatchMatrix class
    // TODO: might be simpler to work from the valueSet
    // TODO: try remove the call to rowList / columnList
    List<Coordinate> allMatches() {
        List<Coordinate> pairs = new ArrayList<>();
        int rows = rowList().size();
        int cols = columnList().size();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                VariantGraph.Vertex vertex = vertexAt(i, j);
                if (vertex != null) pairs.add(new Coordinate(i, j, new Match(vertex, null)));
            }
        }
        return pairs;
    }

    private class MatchTableCell {
        public final Token token;
        public final VariantGraph.Vertex vertex;

        public MatchTableCell(Token token, VariantGraph.Vertex vertex) {
            this.token = token;
            this.vertex = vertex;
        }
    }
}
