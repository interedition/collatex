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

package eu.interedition.collatex.matching;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.util.StreamUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Matches {

    public final Map<Token, List<VariantGraph.Vertex>> allMatches;
    public final Set<Token> unmatchedInWitness;
    public final Set<Token> ambiguousInWitness;
    public final Set<Token> uniqueInWitness;

    public static Matches between(final Iterable<VariantGraph.Vertex> vertices, final Iterable<Token> witnessTokens, Comparator<Token> comparator) {

        final Map<Token, List<VariantGraph.Vertex>> allMatches = new HashMap<>();

        StreamUtil.stream(vertices).forEach(vertex ->
                vertex.tokens().stream().findFirst().ifPresent(baseToken ->
                        StreamUtil.stream(witnessTokens)
                                .filter(witnessToken -> comparator.compare(baseToken, witnessToken) == 0)
                                .forEach(matchingToken -> allMatches.computeIfAbsent(matchingToken, t -> new ArrayList<>()).add(vertex))));

        final Set<Token> unmatchedInWitness = StreamUtil.stream(witnessTokens)
                .filter(t -> !allMatches.containsKey(t))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        final Set<VariantGraph.Vertex> ambiguousInBase = allMatches.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(Function.identity(), v -> 1, (a, b) -> a + b))
                .entrySet()
                .stream()
                .filter(v -> v.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // (have to check: base -> witness, and witness -> base)
        final Set<Token> ambiguousInWitness = Stream.concat(
                StreamUtil.stream(witnessTokens)
                        .filter(t -> allMatches.getOrDefault(t, Collections.emptyList()).size() > 1),

                allMatches.entrySet().stream()
                        .filter(match -> match.getValue().stream().anyMatch(ambiguousInBase::contains))
                        .map(Map.Entry::getKey)
        ).collect(Collectors.toCollection(LinkedHashSet::new));

        // sure tokens
        // have to check unsure tokens because of (base -> witness && witness -> base)
        final Set<Token> uniqueInWitness = StreamUtil.stream(witnessTokens)
                .filter(t -> allMatches.getOrDefault(t, Collections.emptyList()).size() == 1 && !ambiguousInWitness.contains(t))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new Matches(allMatches, unmatchedInWitness, ambiguousInWitness, uniqueInWitness);
    }

    private Matches(Map<Token, List<VariantGraph.Vertex>> allMatches, Set<Token> unmatchedInWitness, Set<Token> ambiguousInWitness, Set<Token> uniqueInWitness) {
        this.allMatches = Collections.unmodifiableMap(allMatches);
        this.unmatchedInWitness = Collections.unmodifiableSet(unmatchedInWitness);
        this.ambiguousInWitness = Collections.unmodifiableSet(ambiguousInWitness);
        this.uniqueInWitness = Collections.unmodifiableSet(uniqueInWitness);
    }

}
