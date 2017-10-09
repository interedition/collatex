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

package eu.interedition.collatex.medite;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.util.StreamUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class SuffixTreeTest extends AbstractTest {

    @Test
    public void suffixTree() {
        final SuffixTree<String> st = SuffixTree.build(Comparator.comparing(String::toLowerCase), "S", "P", "O", "a", "s", "p", "o");

        LOG.fine(st::toString);
        LOG.fine(() -> StreamUtil.stream(st.match(Arrays.asList("s", "p", "o", "a")))//
                .map(Object::toString)//
                .collect(Collectors.joining(", ")));
    }

}
