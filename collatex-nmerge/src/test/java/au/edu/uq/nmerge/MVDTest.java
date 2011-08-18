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

package au.edu.uq.nmerge;

import au.edu.uq.nmerge.graph.Converter;
import au.edu.uq.nmerge.graph.VariantGraph;
import au.edu.uq.nmerge.mvd.Collation;
import au.edu.uq.nmerge.mvd.Match;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.junit.Test;

import java.util.Comparator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MVDTest extends AbstractTest {

  @Test
  public void simple() throws Exception {
    final Collation<String> collation = new Collation<String>("Test", CASE_INSENSITIVE_ORDERING, "");

    collation.newVersion("test1", "test1", Lists.newArrayList("The", "quick", "brown", "fox", "died"));
    collation.newVersion("test2", "test2", Lists.newArrayList("the", "Quick", "red", "fox", "got", "rabies", "and", "died"));
    collation.newVersion("test3", "test3", Lists.newArrayList("the", "quick", "Blue", "fox", "lives"));

    for (Match<String> m : collation.getMatches()) {
      LOG.debug(m.toString());
    }

    final Converter<String> converter = new Converter<String>();
    final VariantGraph<String> graph = converter.create(collation.getMatches(), collation.getWitnesses());
    LOG.debug("\n" + graph.toString());
  }

  private static final Ordering<String> CASE_INSENSITIVE_ORDERING = Ordering.from(new Comparator<String>() {
    @Override
    public int compare(String o1, String o2) {
      return o1.toLowerCase().compareTo(o2.toLowerCase());
    }
  });
}
