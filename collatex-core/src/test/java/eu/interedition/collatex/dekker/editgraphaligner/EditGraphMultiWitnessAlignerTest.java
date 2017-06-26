package eu.interedition.collatex.dekker.editgraphaligner;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import static eu.interedition.collatex.dekker.token_index.VariantGraphMatcher.graph;
import eu.interedition.collatex.simple.SimpleWitness;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

import java.util.*;

/**
 * Created by Ronald Haentjens Dekker on 06/01/17.
 */
public class EditGraphMultiWitnessAlignerTest extends AbstractTest {


    @Test
    public void testMWADavidBirnbaum() {
        final SimpleWitness[] w = createWitnesses(
                "aaaa bbbb cccc dddd eeee ffff",
                "aaaa bbbb eeex ffff"
        );
        EditGraphAligner aligner = new EditGraphAligner();
        VariantGraph g = new VariantGraph();
        List<SimpleWitness> witnesses = new ArrayList<>();
        witnesses.addAll(Arrays.asList(w));
        aligner.collate(g, witnesses);
        assertThat(g, graph(w[0]).aligned("aaaa bbbb").non_aligned("cccc dddd eeee").aligned("ffff"));
        assertThat(g, graph(w[1]).aligned("aaaa bbbb").non_aligned("eeex").aligned("ffff"));
    }

    @Test
    public void testMWADavidBirnbaum3Witnesses() {
        final SimpleWitness[] w = createWitnesses(
                "aaaa bbbb cccc dddd eeee ffff",
                "aaaa bbbb eeex ffff",
                "aaaa bbbb cccc eeee ffff"
        );
        EditGraphAligner aligner = new EditGraphAligner();
        VariantGraph g = new VariantGraph();
        List<SimpleWitness> witnesses = new ArrayList<>();
        witnesses.addAll(Arrays.asList(w));
        aligner.collate(g, witnesses);
        assertThat(g, graph(w[0]).aligned("aaaa bbbb cccc").non_aligned("dddd").aligned("eeee ffff"));
        assertThat(g, graph(w[1]).aligned("aaaa bbbb").non_aligned("eeex").aligned("ffff"));
        assertThat(g, graph(w[2]).aligned("aaaa bbbb cccc eeee ffff"));
    }

    @Test
    public void testMWADavidBirnbaum4Witnesses() {
        final SimpleWitness[] w = createWitnesses(
                "aaaa bbbb cccc dddd eeee ffff",
                "aaaa bbbb eeex ffff",
                "aaaa bbbb cccc eeee ffff",
                "aaaa bbbb eeex dddd ffff"
        );
        EditGraphAligner aligner = new EditGraphAligner();
        VariantGraph g = new VariantGraph();
        List<SimpleWitness> witnesses = new ArrayList<>();
        witnesses.addAll(Arrays.asList(w));
        aligner.collate(g, witnesses);
        showAlignmentTable(w, g);
        assertThat(g, graph(w[0]).aligned("aaaa bbbb cccc dddd eeee ffff"));
        assertThat(g, graph(w[1]).aligned("aaaa bbbb eeex ffff"));
        assertThat(g, graph(w[2]).aligned("aaaa bbbb cccc eeee ffff"));
        assertThat(g, graph(w[3]).aligned("aaaa bbbb eeex dddd ffff"));
    }

    @Test
    public void testMWADavidBirnbaum5Witnesses() {
        final SimpleWitness[] w = createWitnesses(
                "aaaa bbbb cccc dddd eeee ffff",
                "aaaa bbbb eeex ffff",
                "aaaa bbbb cccc eeee ffff",
                "aaaa bbbb eeex dddd ffff",
                "aaa aaa aaa aaa aaa"
        );
        EditGraphAligner aligner = new EditGraphAligner();
        VariantGraph g = new VariantGraph();
        List<SimpleWitness> witnesses = new ArrayList<>();
        witnesses.addAll(Arrays.asList(w));
        aligner.collate(g, witnesses);
        showAlignmentTable(w, g);
        assertThat(g, graph(w[0]).aligned("aaaa bbbb cccc dddd eeee ffff"));
        assertThat(g, graph(w[1]).aligned("aaaa bbbb eeex ffff"));
        assertThat(g, graph(w[2]).aligned("aaaa bbbb cccc eeee ffff"));
        assertThat(g, graph(w[3]).aligned("aaaa bbbb eeex dddd ffff"));
        assertThat(g, graph(w[4]).non_aligned("aaa aaa aaa aaa aaa"));

//        # check that there are no doubles in the graph
//        # i have to be careful here
//        # The vertex is created twice, so that won't be the same object.
//        results = set()
//        for v in vg.vertices():
//        content = v.label
//        if content in results:
//        raise Exception("Content "+content+" already in graph!")
//            else:
//        results.add(content)
        Set<String> results = new HashSet<>();
        g.vertices().forEach(v -> {
            String content = v.toString();
//            LOG.info(content);
            assertThat(results.contains(content), is(false));
            if (!content.equals("[]")) {
                results.add(content);
            }
        });
    }

    @Test
    public void testRankAdjustment() {
        final SimpleWitness[] w = createWitnesses(
                "aaaa cccc dddd ffff",
                "bbbb cccc eeee ffff",
//                "aaaa cccc eeee ffff",
                "bbbb gggg dddd ffff"
        );
        EditGraphAligner aligner = new EditGraphAligner();
        VariantGraph g = new VariantGraph();
        List<SimpleWitness> witnesses = new ArrayList<>();
        witnesses.addAll(Arrays.asList(w));
        aligner.collate(g, witnesses);
        assertThat(g, graph(w[0]).non_aligned("aaaa").aligned("cccc dddd ffff"));
        assertThat(g, graph(w[1]).aligned("bbbb cccc").non_aligned("eeee").aligned("ffff"));
        assertThat(g, graph(w[2]).aligned("bbbb").non_aligned("gggg").aligned("dddd ffff"));
//        assertThat(g, graph(w[3]).aligned("bbbb cccc dddd ffff"));
        showAlignmentTable(w, g);
    }

    private void showAlignmentTable(SimpleWitness[] w, VariantGraph g) {
        List<SortedMap<Witness, Set<Token>>> table = table(g);
        for (SimpleWitness sw: w) {
            System.out.println(toString(table, sw));
        }
    }

}
