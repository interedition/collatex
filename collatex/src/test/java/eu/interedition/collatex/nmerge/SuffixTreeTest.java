/*
 * NMerge is Copyright 2009-2011 Desmond Schmidt
 *
 * This file is part of NMerge. NMerge is a Java library for merging
 * multiple versions into multi-version documents (MVDs), and for
 * reading, searching and comparing them.
 *
 * NMerge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.nmerge;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.nmerge.graph.suffixtree.SuffixTree;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SuffixTreeTest extends AbstractTest {

  private static final List<String> TEST_STRING = Lists.newArrayList("Hello ", "world ", "world ", "World ", "!");

  @Test
  public void simple() {
    final SuffixTree<String> st = new SuffixTree<String>(TEST_STRING, Ordering.<String>natural(), "");
    LOG.debug(st.printTree());
    Assert.assertNotNull(st.findSubstring(TEST_STRING.subList(0, 3)));
  }

  /**
   * All the words stored in a string are findable
   */
  @Test
  public void verifyAllSuffixes() {
    final SuffixTree<String> st = new SuffixTree<String>(TEST_STRING, Ordering.<String>natural(), "");
    final int end = TEST_STRING.size();
    for (int start = 0; start < end; start++) {
      Assert.assertNotNull("Couldn't find word [" + start + ", " + end + "]", st.findSubstring(TEST_STRING.subList(start, end)));
    }
  }
}
