package eu.interedition.collatex.dekker.editgraphaligner;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static eu.interedition.collatex.dekker.token_index.VariantGraphMatcher.graph;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
    VariantGraph g = new VariantGraph();
    align(g, w);
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
    VariantGraph g = new VariantGraph();
    align(g, w);
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
    VariantGraph g = new VariantGraph();
    align(g, w);
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
    VariantGraph g = new VariantGraph();
    align(g, w);
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
    VariantGraph g = new VariantGraph();
    align(g, w);
    assertThat(g, graph(w[0]).non_aligned("aaaa").aligned("cccc dddd ffff"));
    assertThat(g, graph(w[1]).aligned("bbbb cccc").non_aligned("eeee").aligned("ffff"));
    assertThat(g, graph(w[2]).aligned("bbbb").non_aligned("gggg").aligned("dddd ffff"));
//        assertThat(g, graph(w[3]).aligned("bbbb cccc dddd ffff"));
    showAlignmentTable(w, g);
  }

  @Test
  public void testDuplicatedTokenInWitness() {
    final SimpleWitness[] w = createWitnesses(
        "a",
        "b",
        "c",
        "a a"
    );
    VariantGraph g = new VariantGraph();
    align(g, w);
    assertThat(g, graph(w[0]).aligned("a"));
    assertThat(g, graph(w[1]).non_aligned("b"));
    assertThat(g, graph(w[2]).non_aligned("c"));
    assertThat(g, graph(w[3]).non_aligned("a").aligned("a"));
    showAlignmentTable(w, g);
  }

  @Test
  public void test1() {
    final SimpleWitness[] w = createWitnesses(
        "a",
        "b",
        "a b"
    );
    VariantGraph g = new VariantGraph();
    align(g, w);
    assertThat(g, graph(w[0]).aligned("a"));
    assertThat(g, graph(w[1]).aligned("b"));
    assertThat(g, graph(w[2]).aligned("a b"));
  }

  @Test
  public void test2() {
    final SimpleWitness[] w = createWitnesses(
        "a",
        "a b",
        "b"
    );
    VariantGraph g = new VariantGraph();
    align(g, w);
    assertThat(g, graph(w[0]).aligned("a"));
    assertThat(g, graph(w[1]).aligned("a b"));
    assertThat(g, graph(w[2]).aligned("b"));
  }

  @Test
  public void test3() {
    final SimpleWitness[] w = createWitnesses(
        "a b",
        "b",
        "a"
    );
    VariantGraph g = new VariantGraph();
    align(g, w);
    assertThat(g, graph(w[0]).aligned("a b"));
    assertThat(g, graph(w[1]).aligned("b"));
    assertThat(g, graph(w[2]).aligned("a"));
  }

  @Test
  public void test4() {
    final SimpleWitness[] w = createWitnesses(
        "a x",
        "y b",
        "a b"
    );
    VariantGraph g = new VariantGraph();
    align(g, w);
    assertThat(g, graph(w[0]).aligned("a").non_aligned("x"));
    assertThat(g, graph(w[1]).non_aligned("y").aligned("b"));
    assertThat(g, graph(w[2]).aligned("a b"));
  }

  @Test
  public void test5() {
    final SimpleWitness[] w = createWitnesses(
        "a x",
        "b",
        "a b"
    );
    VariantGraph g = new VariantGraph();
    align(g, w);
    assertThat(g, graph(w[0]).aligned("a").non_aligned("x"));
    assertThat(g, graph(w[1]).aligned("b"));
    assertThat(g, graph(w[2]).aligned("a b"));
  }

  @Test
  public void test6() {
    final SimpleWitness[] w = createWitnesses(
        "a",
        "y b",
        "a b"
    );
    VariantGraph g = new VariantGraph();
    align(g, w);
    assertThat(g, graph(w[0]).aligned("a"));
    assertThat(g, graph(w[1]).non_aligned("y").aligned("b"));
    assertThat(g, graph(w[2]).aligned("a b"));
  }

    @Test
    public void test7() {
        final SimpleWitness[] w = createWitnesses(
            "a x",
            "b y",
            "a b"
        );
        VariantGraph g = new VariantGraph();
        align(g, w);
        assertThat(g, graph(w[0]).aligned("a").non_aligned("x"));
        assertThat(g, graph(w[1]).aligned("b").non_aligned("y"));
        assertThat(g, graph(w[2]).aligned("a b"));
    }

    @Test
    public void test8() {
        final SimpleWitness[] w = createWitnesses(
            "a",
            "b",
            "c",
            "a b c"
        );
        VariantGraph g = new VariantGraph();
        align(g, w);
        assertThat(g, graph(w[0]).aligned("a"));
        assertThat(g, graph(w[1]).aligned("b"));
        assertThat(g, graph(w[2]).aligned("c"));
        assertThat(g, graph(w[3]).aligned("a b c"));
    }

    @Test
    public void testDuplicatedTokenInWitness2() {
        final SimpleWitness[] w = createWitnesses(
            "a",
            "b",
            "c",
            "a b c a b c"
        );
        VariantGraph g = new VariantGraph();
        align(g, w);
        assertThat(g, graph(w[0]).aligned("a"));
        assertThat(g, graph(w[1]).aligned("b"));
        assertThat(g, graph(w[2]).aligned("c"));
        assertThat(g, graph(w[3]).non_aligned("a b c").aligned("a b c"));
    }

  @Ignore @Test
  public void testAlignWithLongestMatch() {
    final SimpleWitness[] w = createWitnesses(
        "a g a g c t a g t",
        "a g c t"
    );
    VariantGraph g = new VariantGraph();
    align(g, w);

    final SimpleWitness[] w2 = createWitnesses(
        "a g c t",
        "a g a g c t a g t"
    );
    VariantGraph g2 = new VariantGraph();
    align(g2, w2);
    assertThat(g2, graph(w2[0]).aligned("a g c t"));
    assertThat(g2, graph(w2[1]).non_aligned("a g").aligned("a g c t").non_aligned("a g t"));
    assertThat(g, graph(w[0]).non_aligned("a g").aligned("a g c t").non_aligned("a g t"));
    assertThat(g, graph(w[1]).aligned("a g c t"));
  }

  private void align(VariantGraph g, SimpleWitness[] w) {
    EditGraphAligner aligner = new EditGraphAligner();
    List<SimpleWitness> witnesses = new ArrayList<>();
    witnesses.addAll(Arrays.asList(w));
    aligner.collate(g, witnesses);
    showAlignmentTable(w, g);
  }

  private void showAlignmentTable(SimpleWitness[] w, VariantGraph g) {
    List<SortedMap<Witness, Set<Token>>> table = table(g);
    for (SimpleWitness sw : w) {
      System.out.println(toString(table, sw));
    }
  }

}
