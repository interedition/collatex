package eu.interedition.collatex.dekker.editgraphaligner;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static eu.interedition.collatex.dekker.token_index.VariantGraphMatcher.graph;
import static org.junit.Assert.assertThat;

/**
 * Created by Ronald Haentjens Dekker on 06/01/17.
 */
public class EditGraphMultiWitnessAlignerTest extends AbstractTest {


    @Test
    public void testMWADavidBirnbaum() {
        final SimpleWitness[] w = createWitnesses("aaaa bbbb cccc dddd eeee ffff", "aaaa bbbb eeex ffff");
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
        final SimpleWitness[] w = createWitnesses("aaaa bbbb cccc dddd eeee ffff", "aaaa bbbb eeex ffff",
            "aaaa bbbb cccc eeee ffff");
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
        final SimpleWitness[] w = createWitnesses("aaaa bbbb cccc dddd eeee ffff", "aaaa bbbb eeex ffff",
            "aaaa bbbb cccc eeee ffff", "aaaa bbbb eeex dddd ffff");
        EditGraphAligner aligner = new EditGraphAligner();
        VariantGraph g = new VariantGraph();
        List<SimpleWitness> witnesses = new ArrayList<>();
        witnesses.addAll(Arrays.asList(w));
        aligner.collate(g, witnesses);
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
        assertThat(g, graph(w[0]).aligned("aaaa bbbb cccc dddd eeee ffff"));
        assertThat(g, graph(w[1]).aligned("aaaa bbbb eeex ffff"));
        assertThat(g, graph(w[2]).aligned("aaaa bbbb cccc eeee ffff"));
        assertThat(g, graph(w[3]).aligned("aaaa bbbb eeex dddd ffff"));
        assertThat(g, graph(w[4]).non_aligned("aaa aaa aaa aaa aaa"));
    }

//    collation = Collation()
//        collation.add_plain_witness("A", "aaaa bbbb cccc dddd eeee ffff")
//            collation.add_plain_witness("B", "aaaa bbbb eeex ffff") # Near-match gap
//        collation.add_plain_witness("C", "aaaa bbbb cccc eeee ffff")
//            collation.add_plain_witness("D", "aaaa bbbb eeex dddd ffff") # Transposition
//        # collation.add_plain_witness("E", "aaa aaa aaa aaa aaa")
//            # table = collate(collation, segmentation=False, near_match=True)
//        # table = collate(collation, segmentation=False, near_match=False)
//        # print(table)
//    vg = collate(collation, output="graph")
//
//        # check that there are no doubles in the graph
//        # i have to be careful here
//        # The vertex is created twice, so that won't be the same object.
//    result = set()
//        for v in vg.vertices():
//    content = v.label
//            if content in result:
//    raise Exception("Content "+content+" already in graph!")
//            else:
//                result.add(content)

}
