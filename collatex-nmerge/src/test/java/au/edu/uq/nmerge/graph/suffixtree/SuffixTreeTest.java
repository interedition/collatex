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

package au.edu.uq.nmerge.graph.suffixtree;

import au.edu.uq.nmerge.AbstractTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SuffixTreeTest extends AbstractTest {

  private static final String TEST_STRING = "Hello World";

  @Test
  public void simple() {
    final SuffixTree st = new SuffixTree(TEST_STRING.getBytes());
    LOG.debug(st.printTree());
    Assert.assertNotSame(st.errorValue, st.findSubstring("d".getBytes()));
  }

  /**
   * Self test of the tree: Search for all substrings of the main string
   */
  @Test
  public void selfTestTree() {
    final SuffixTree st = new SuffixTree(TEST_STRING.getBytes());

    // loop for all the prefixes of the tree source string
    for (int k = 1; k < st.length; k++) {
      // loop for each suffix of each prefix
      for (int j = 1; j <= k; j++) {
        // search for the current suffix in the tree
        int len = k - j + 1;
        byte[] test = new byte[len];
        for (int m = 0; m < len; m++) {
          test[m] = st.source[j + m];
        }
        Assert.assertNotSame("in string (" + j + "," + k + ")", st.errorValue, st.findSubstring(test));
      }
    }
  }
}
