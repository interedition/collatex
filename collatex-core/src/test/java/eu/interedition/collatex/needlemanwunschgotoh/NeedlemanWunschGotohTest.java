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

package eu.interedition.collatex.needlemanwunschgotoh;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.needlemanwunschgotoh.*;
import eu.interedition.collatex.matching.*;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.util.VariantGraphTraversal;
import org.junit.Test;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.*;

import static org.junit.Assert.*;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class NeedlemanWunschGotohTest extends AbstractTest {

    final double delta = 0.000000001;
    NeedlemanWunschGotohAligner aligner;

    public class NeedlemanWunschScorerStringString implements NeedlemanWunschScorer<String, String> {
        private final StringMetricScorer matchScorer;

        public NeedlemanWunschScorerStringString(StringMetricScorer matchScorer) {
            this.matchScorer = matchScorer;
        }

        @Override
        public double score(String a, String b) {
            return matchScorer.score(a, b);
        }
    };

    private void testAlign(String a, String b) {
        StringBuilder debugMatrix = new StringBuilder();
        aligner.setDebugMatrix(debugMatrix);

        List<Pair<String, String>> list, expectedList;

        List<String> aa = Arrays.asList(a.split("\\s+"));
        List<String> bb = Arrays.asList(b.split("\\s+"));
        assertEquals(aa.size(), bb.size());

        expectedList = new ArrayList<Pair<String, String>>();
        for (int i = 0; i < aa.size(); i++) {
            expectedList.add(new Pair<String, String>(aa.get(i), bb.get(i)));
        }

        try {
            list = aligner.align(
                    aa.stream().filter(p -> !p.equals("-")).collect(Collectors.toList()),
                    bb.stream().filter(p -> !p.equals("-")).collect(Collectors.toList())
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(debugMatrix.toString());
            throw e;
        }

        list = list.stream()
            .map(p -> new Pair<String, String>(p.a == null ? "-" : p.a, p.b == null ? "-" : p.b))
            .collect(Collectors.toList());

        String aaa = list        .stream().map(Pair::toString).collect(Collectors.joining(" "));
        String bbb = expectedList.stream().map(Pair::toString).collect(Collectors.joining(" "));

        if (!aaa.equals(bbb)) {
            System.out.println(debugMatrix.toString());
        }

        assertEquals(bbb, aaa);
    }

    private void testCollate(String... witnesses) {
        StringBuilder debugMatrix = new StringBuilder();
        ((NeedlemanWunschGotohAlgorithm) collationAlgorithm).setDebugMatrix(debugMatrix);

        List<String> strings = new ArrayList<>();
        List<String> tables  = new ArrayList<>();
        for (String witness : witnesses) {
            List<String> l = Arrays.asList(witness.split("\\s+"));
            strings.add(l.stream().filter(p -> !p.equals("-")).collect(Collectors.joining(" ")));
            tables.add(l.stream().map(p -> p.equals("-") ? " " : p).collect(Collectors.joining("|", "|", "|")));
        }

        final SimpleWitness[] w = createWitnesses(strings.toArray(new String[0]));

        List<SortedMap<Witness, Set<Token>>> t;
        try {
            t = table(collate(w));
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(debugMatrix.toString());
            throw e;
        }

        int i = 0;
        for (String table : tables) {
            String result = toString(t, w[i]);
            if (!table.equals(result)) {
                System.out.println(table);
                System.out.println(result);
                System.out.println(debugMatrix.toString());
            }
            assertEquals(table, result);
            i++;
        }
    }

    @Test
    public void simple() {
        setCollationAlgorithm(CollationAlgorithmFactory.needlemanWunschGotoh());
        LOG.fine(toString(table(collate("a b a b a", "a b a"))));
    }

    @Test
    public void equalityScorerTest() {
        StringMetricScorer scorer = new EqualityScorer(0.0, 1.0);

        assertEquals(1.0, scorer.score("foobar", "foobar"),  delta);
        assertEquals(0.0, scorer.score("foobar", "fobar"),   delta);
        assertEquals(0.0, scorer.score("foobar", "fooobar"), delta);
        assertEquals(0.0, scorer.score("foobar", "foobaz"),  delta);
        assertEquals(0.0, scorer.score("foobar", "foobiz"),  delta);
    }

    @Test
    public void levenshteinDistanceScorerTest() {
        StringMetricScorer scorer = new LevenshteinDistanceScorer(0.0, 1.0, 1);

        assertEquals(1.0, scorer.score("foobar", "foobar"),  delta);
        assertEquals(1.0, scorer.score("foobar", "fobar"),   delta);
        assertEquals(1.0, scorer.score("foobar", "fooobar"), delta);
        assertEquals(1.0, scorer.score("foobar", "foobaz"),  delta);
        assertEquals(0.0, scorer.score("foobar", "foobiz"),  delta);
    }

    @Test
    public void levenshteinRatioScorerTest() {
        StringMetricScorer scorer = new LevenshteinRatioScorer(0.0, 1.0);

        assertEquals(1.0, scorer.score("foobar", "foobar"), delta);
        assertEquals(0.5, scorer.score("fooabc", "foodef"), delta);
        assertEquals(0.0, scorer.score("abcdef", "ghijkl"), delta);

        assertTrue(scorer.score("foobar", "fooobar") > scorer.score("fobar", "foobar"));
    }

    @Test
    public void trigramRatioScorerTest() {
        StringMetricScorer scorer = new TrigramRatioScorer(0.0, 1.0);

        assertEquals(1.0,      scorer.score("foobar", "foobar"), delta);
        assertEquals(6.0 / 16, scorer.score("fooabc", "foodef"), delta);
        assertEquals(6.0 / 16, scorer.score("abcfoo", "deffoo"), delta);
        assertEquals(0.0,      scorer.score("abcdef", "ghijkl"), delta);

        assertEquals(2.0 / 12, scorer.score("heti",    "ethi"),  delta);

        assertTrue(scorer.score("foobar", "fooobar") > scorer.score("fobar", "foobar"));
    }

    @Test
    public void equalityAlignerTest() {
        aligner = new NeedlemanWunschGotohAligner(
            new NeedlemanWunschScorerStringString(new EqualityScorer())
        );

        testAlign("a b c",
                  "a b c");

        testAlign("a b c",
                  "a b d");

        testAlign("a b c",
                  "a - c");

        testAlign("- - -",
                  "a b c");

        testAlign("a b c",
                  "- - -");

        // The equality scorer will match words only if they are equal.

        testAlign("foo foob foobar foob foo",
                  "-   -    foobar -    -  ");

        // It will not match a word that is similar.

        testAlign("foo    foob fooba foob foo",
                  "foobar -    -     -    -");  // expected wrong answer
    }

    @Test
    public void levenshteinDistanceAlignerTest() {
        aligner = new NeedlemanWunschGotohAligner(
            new NeedlemanWunschScorerStringString(new LevenshteinDistanceScorer(1))
        );

        // The Levenshtein distance scorer will match a word that is equal.

        testAlign("x foo foobar foo y",
                  "- -   foobar -   -");

        // But it will also match a word that is similar within Levenshtein
        // distance.

        testAlign("x foo fooba  foo y",
                  "- -   foobar -   -");

        // But if many words are within Levenshtein distance they will all look
        // the same to the scorer, so it picks the wrong one.

        testAlign("x foob fooba  foobar fooba  foob y",
                  "- -    -      -      foobar -    -"); // expected wrong answer
    }

    @Test
    public void levenshteinRatioAlignerTest() {
        aligner = new NeedlemanWunschGotohAligner(
            new NeedlemanWunschScorerStringString(new LevenshteinRatioScorer())
        );

        // The Levenshtein ratio scorer will match a word that is equal.

        testAlign("x foo foobar foo y",
                  "- -   foobar -   -");

        // But it will also match the most similar word.

        testAlign("x foo fooba  foo y",
                  "- -   foobar -   -");

        // But it will give the correct match where the Levenshtein distance
        // scorer failed.

        testAlign("x foob fooba foobar fooba foob y",
                  "- -    -     foobar -     -    -");

        // More tests

        testAlign("-  -    Sanguis   effusio    in ecclesiis facta",
                  "Si quis sanguinis effusionem in eclesia   fecerit");

        testAlign("Si quis sanguinis effusionem in eclesia   fecerit",
                  "-  -    Sanguis   effusio    in ecclesiis facta");

        testAlign("uolumus ut ea    dentur que",
                  "uolumus ut detur ea     que");


        testAlign("periurium committitur . Quando maioris pretii",
                  "perIurium committitur - quanto maIoris pretii");
    }

    @Test
    public void trigramRatioAlignerTest() {
        aligner = new NeedlemanWunschGotohAligner(
            new NeedlemanWunschScorerStringString(new TrigramRatioScorer())
        );

        // The trigram ratio scorer will match a word that is equal.

        testAlign("x foo foobar foo y",
                  "- -   foobar -   -");

        // But it will also match the most similar word.

        testAlign("x foo fooba  foo y",
                  "- -   foobar -   -");

        // But it will give the correct match where the Levenshtein distance
        // scorer failed.

        testAlign("x foob fooba foobar fooba foob y",
                  "- -    -     foobar -     -    -");

        // More tests

        testAlign("-  -    Sanguis   effusio    in ecclesiis facta",
                  "Si quis sanguinis effusionem in eclesia   fecerit");

        testAlign("Si quis sanguinis effusionem in eclesia   fecerit",
                  "-  -    Sanguis   effusio    in ecclesiis facta");

        testAlign("uolumus ut -     ea dentur que",
                  "uolumus ut detur ea -      que");

        testAlign("periurium committitur . quando maioris pretii",
                  "perIurium committitur - quanto maIoris pretii");
    }

    @Test
    public void collatorSanityTest() {
        setCollationAlgorithm(CollationAlgorithmFactory.needlemanWunschGotoh());

        // Test if the collator works on an empty graph.

        final SimpleWitness[] w = createWitnesses("a b c d");
        final VariantGraph graph = collate(w);
        final List<VariantGraph.Vertex> vertices =
            StreamSupport.stream(VariantGraphTraversal.of(graph).spliterator(), false).collect(Collectors.toList());
        assertEquals(6, vertices.size());
        assertEquals(graph.getStart(), vertices.get(0));
        assertEquals(1, vertices.get(1).tokens().size());
        assertEquals(1, vertices.get(2).tokens().size());
        assertEquals(1, vertices.get(3).tokens().size());
        assertEquals(1, vertices.get(4).tokens().size());
        assertVertexEquals("a", vertices.get(1));
        assertVertexEquals("b", vertices.get(2));
        assertVertexEquals("c", vertices.get(3));
        assertVertexEquals("d", vertices.get(4));
        assertEquals(graph.getEnd(), vertices.get(5));
    }

    @Test
    public void collatorSanityTest1() {
        setCollationAlgorithm(CollationAlgorithmFactory.needlemanWunschGotoh());

        testCollate("a b c");
    }

    @Test
    public void collatorSanityTest2() {
        setCollationAlgorithm(CollationAlgorithmFactory.needlemanWunschGotoh());

        testCollate("the cat is black",
                    "the dog is black");
    }

    @Test
    public void exactMatch() {
        setCollationAlgorithm(CollationAlgorithmFactory.needlemanWunschGotoh());

        testCollate("a b c",
                    "a b c");

        testCollate("a b c",
                    "a b d");

        testCollate("a b c",
                    "a d c");

        testCollate("a b c",
                    "d b c");

        testCollate("a b c",
                    "a - c");

        testCollate("a - c",
                    "a b c");

        testCollate("a b c",
                    "- b -");

        testCollate("- b -",
                    "a b c");
    }

    @Test
    public void exactMatch1() {
        setCollationAlgorithm(CollationAlgorithmFactory.needlemanWunschGotoh());

        testCollate("a foob foob foobar foob foob b",
                    "a -    -    foobar -    -    b");

        testCollate("a foob foob foobar foob foob b foob foob c",
                    "a -    -    foobar -    -    b -    -    c");
    }

    @Test
    public void multiMatch() {
        setCollationAlgorithm(CollationAlgorithmFactory.needlemanWunschGotoh());

        testCollate("a b c - - -",
                    "- b c d - -",
                    "- - c d e -",
                    "- - - d e f");

        testCollate("- - - d e f",
                    "- - c d e -",
                    "- b c d - -",
                    "a b c - - -");
    }

    @Test
    public void distanceMatch() {
        setCollationAlgorithm(CollationAlgorithmFactory.needlemanWunschGotoh());

        testCollate("x foo foo foobar foo foo y",
                    "- -   -   fooba  -   -   -");
    }

    @Test
    public void ratioMatch() {
        setCollationAlgorithm(CollationAlgorithmFactory.needlemanWunschGotoh());

        testCollate("x foo foob foobar foo foo y",
                    "- -   -    fooba  -   -   -");

        testCollate("hadebaldus -         heti bernoinus",
                    "hadebaldus bernuinus heti -",
                    "adebaldus  -         ethi bernoinus");
    }

    @Test
    public void preferOneLongGap() {
        setCollationAlgorithm(CollationAlgorithmFactory.needlemanWunschGotoh());

        // The aligner will prefer one long gap over many short ones.

        testCollate("a b b b b b c",
                    "a b b - - - c");

        testCollate("a b b b b b c",
                    "a b b - - - c");
    }
}
