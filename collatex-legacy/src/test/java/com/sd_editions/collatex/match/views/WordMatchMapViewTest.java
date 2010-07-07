/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
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

package com.sd_editions.collatex.match.views;

import java.util.List;

import junit.framework.TestCase;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.Util;
import com.sd_editions.collatex.match.WordMatchMap;

public class WordMatchMapViewTest extends TestCase {
  private WordMatchMapView testView;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    List<BlockStructure> testcase = Lists.newArrayList();
    testcase.add(Util.string2BlockStructure("a black cat"));
    testcase.add(Util.string2BlockStructure("two white dogs"));
    WordMatchMap testMap = new WordMatchMap(testcase);
    testView = new WordMatchMapView(testMap);
  }

  public void testToHtml() {
    String html = testView.toHtml();
    assertTrue(html.contains("&quot;white&quot;"));
    assertTrue(html.contains("[B,2]"));
  }

}
