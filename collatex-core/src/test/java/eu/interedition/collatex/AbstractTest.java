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

package eu.interedition.collatex;

import eu.interedition.collatex.dekker.DekkerAlgorithm;
import eu.interedition.collatex.dekker.InspectableCollationAlgorithm;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.util.StreamUtil;
import eu.interedition.collatex.util.VariantGraphRanking;
import eu.interedition.collatex.util.VariantGraphTraversal;
import org.junit.Assert;
import org.junit.Before;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.interedition.collatex.dekker.Match.PHRASE_MATCH_TO_TOKENS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public abstract class AbstractTest {
    public static final char[] SIGLA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    protected final Logger LOG = Logger.getLogger(getClass().getName());

    protected CollationAlgorithm collationAlgorithm;

    @Before
    public void initAlgorithm() {
        collationAlgorithm = new DekkerAlgorithm(new EqualityTokenComparator());
    }

    protected SimpleWitness[] createWitnesses(String... contents) {
        Assert.assertTrue("Not enough sigla", contents.length <= SIGLA.length);
        final SimpleWitness[] witnesses = new SimpleWitness[contents.length];
        for (int wc = 0; wc < contents.length; wc++) {
            witnesses[wc] = new SimpleWitness(Character.toString(SIGLA[wc]), contents[wc]);
        }
        return witnesses;
    }

    protected VariantGraph collate(SimpleWitness... witnesses) {
        final VariantGraph graph = new VariantGraph();
        collate(graph, witnesses);
        return graph;
    }

    protected void collate(VariantGraph graph, SimpleWitness... witnesses) {
        collationAlgorithm.collate(graph, witnesses);
    }

    protected VariantGraph collate(String... witnesses) {
        return collate(createWitnesses(witnesses));
    }

    protected static List<SortedMap<Witness, Set<Token>>> table(VariantGraph graph) {
        return VariantGraphRanking.of(graph).asTable();
    }

    protected static SortedSet<String> extractPhrases(SortedSet<String> phrases, VariantGraph graph, Witness witness) {
        for (VariantGraph.Vertex v : VariantGraphTraversal.of(graph, Collections.singleton(witness))) {
            phrases.add(toString(v, witness));
        }
        return phrases;
    }

    protected static String toString(VariantGraph.Vertex vertex, Witness... witnesses) {
        final Set<Witness> witnessSet = new HashSet<>(Arrays.asList(witnesses));
        return vertex.tokens().stream()
                .filter(t -> witnessSet.contains(t.getWitness()))
                .collect(Collectors.groupingBy(Token::getWitness)).entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getSigil()))
                .map(Map.Entry::getValue)
                .flatMap(tokens -> tokens.stream()
                        .filter(t -> t instanceof SimpleToken)
                        .map(t -> (SimpleToken) t)
                        .sorted()
                        .map(SimpleToken::getNormalized)
                )
                .collect(Collectors.joining(" "));
    }

    protected static void assertGraphVertices(VariantGraph graph, int vertices) {
        assertEquals(vertices, StreamUtil.stream(graph.vertices()).count());
    }

    protected static void assertGraphEdges(VariantGraph graph, int edges) {
        assertEquals(edges,
                StreamUtil.stream(graph.vertices())//
                        .map(VariantGraph.Vertex::outgoing)//
                        .map(Map::keySet)//
                        .mapToLong(Set::size)//
                        .sum());
    }

    protected static void assetGraphSize(VariantGraph graph, int vertices, int edges) {
        assertGraphVertices(graph, vertices);
        assertGraphEdges(graph, edges);
    }

    protected static void assertHasWitnesses(Set<Witness> edge, Witness... witnesses) {
        assertEquals(new HashSet<>(Arrays.asList(witnesses)), edge);
    }

    protected static Set<Witness> edgeBetween(VariantGraph.Vertex start, VariantGraph.Vertex end) {
        final Optional<Set<Witness>> edge = Optional.ofNullable(start.outgoing().get(end));
        Assert.assertTrue(String.format("No edge between %s and %s", start, end), edge.isPresent());
        return edge.get();
    }

    protected static void assertVertexEquals(String expected, VariantGraph.Vertex vertex) {
        assertEquals(expected, vertex.tokens().stream().findFirst().map(t -> (SimpleToken) t).map(SimpleToken::getNormalized).get());
    }

    protected static void assertTokenEquals(String expected, Token token) {
        assertEquals(expected, ((SimpleToken) token).getContent());
    }

    protected static void assertVertexHasContent(VariantGraph.Vertex vertex, String content, Witness in) {
        Assert.assertEquals(String.format("%s does not has expected content for %s", vertex, in), content, toString(vertex, in));
    }

    protected static VariantGraph.Vertex vertexWith(VariantGraph graph, String content, Witness in) {
        for (VariantGraph.Vertex v : VariantGraphTraversal.of(graph, Collections.singleton(in))) {
            if (content.equals(toString(v, in))) {
                return v;
            }
        }
        fail(String.format("No vertex with content '%s' in witness %s", content, in));
        return null;
    }

    protected static Stream<Witness> witnesses(List<SortedMap<Witness, Set<Token>>> table) {
        return table.stream()
                .map(SortedMap::keySet)
                .flatMap(Set::stream)
                .distinct();
    }

    protected static String toString(List<SortedMap<Witness, Set<Token>>> table) {
        return witnesses(table)
                .sorted(Witness.SIGIL_COMPARATOR)
                .map(witness -> String.format("%s: %s\n", witness.getSigil(), toString(table, witness)))
                .collect(Collectors.joining());
    }

    protected static String toString(List<SortedMap<Witness, Set<Token>>> table, Witness witness) {
        return String.format("|%s|", table.stream()
                .map(r -> r.getOrDefault(witness, Collections.emptySet()))
                .map(tokens -> tokens.stream()
                        .filter(t -> SimpleToken.class.isAssignableFrom(t.getClass()))
                        .map(t -> (SimpleToken) t)
                        .sorted()
                        .map(SimpleToken::getNormalized)
                        .collect(Collectors.joining(" "))
                )
                .map(cell -> cell.isEmpty() ? " " : cell)
                .collect(Collectors.joining("|")));
    }

    protected void assertPhraseMatches(String... expectedPhrases) {
        List<List<Match>> phraseMatches = ((InspectableCollationAlgorithm) collationAlgorithm).getPhraseMatches();
        int i = 0;
        for (List<Match> phraseMatch : phraseMatches) {
            Assert.assertEquals(expectedPhrases[i], SimpleToken.toString(PHRASE_MATCH_TO_TOKENS.apply(phraseMatch)));
            i++;
        }
    }

    protected void setCollationAlgorithm(CollationAlgorithm collationAlgorithm) {
        this.collationAlgorithm = collationAlgorithm;
    }

}
