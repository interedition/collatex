package eu.interedition.collatex.schmidt;

import com.google.common.collect.Iterables;
import eu.interedition.collatex.AbstractTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SuffixTreeTest extends AbstractTest {

  @Test
  public void suffixTree() {
    final SuffixTree<String> st = SuffixTree.build(new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        return o1.toLowerCase().compareTo(o2.toLowerCase());
      }
    }, "S", "P", "O", "a", "s", "p", "o");

    LOG.fine(st.toString());
    LOG.fine(Iterables.toString(st.match(Arrays.asList("s", "p", "o", "a"))));

  }

}
