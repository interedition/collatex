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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.nmerge.graph.Converter;
import eu.interedition.collatex.nmerge.graph.VariantGraph;
import eu.interedition.collatex.nmerge.mvd.Chunk;
import eu.interedition.collatex.nmerge.mvd.ChunkState;
import eu.interedition.collatex.nmerge.mvd.Collation;
import eu.interedition.collatex.nmerge.mvd.Match;
import eu.interedition.collatex.nmerge.mvd.Variant;
import eu.interedition.collatex.nmerge.mvd.Witness;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MVDTest extends AbstractTest {

  @Test
  public void simple() throws Exception {
    nmerge(
            "the quick brown fox has died",
            "the quick fox got blue rabies and has died",
            "the quick blue fox got lives"
    );
  }

  @Test
  public void transposition() throws Exception {
    nmerge(
            "T R A N S A A A B B B",
            "A A A B B B T R A N S "
    );

  }

  protected void nmerge(String... witnessContents) throws Exception {
    final Collation<String> collation = new Collation<String>("Test", Ordering.<String>natural());

    final List<Witness> witnesses = Lists.newArrayListWithExpectedSize(witnessContents.length);
    int sigil = 0;
    for (String witness : witnessContents) {
      witnesses.add(collation.add(new Witness("W" + Integer.toString(++sigil)), tokenize(witness)));
    }

    for (Match<String> m : collation.getMatches()) {
      LOG.debug(m.toString());
    }

    final Converter<String> converter = new Converter<String>();
    final VariantGraph<String> graph = converter.create(collation.getMatches(), collation.getWitnesses());
    LOG.debug("\n" + graph.toString());

    for (Witness w : collation.getWitnesses()) {
      LOG.debug("{}: {}", w, Iterables.toString(collation.getVersion(w)));
    }

    final Witness base = witnesses.get(0);
    for (Chunk<String> ch : collation.compare(base, witnesses.get(1), ChunkState.ADDED)) {
      LOG.debug(ch.toString());
    }

    for (Variant<String> v : collation.getApparatus(base, 0, collation.getVersion(base).size())) {
      LOG.debug(v.toString());
    }
  }

  protected List<String> tokenize(String str) {
    return Arrays.asList(str.split("\\s+"));
  }
}
