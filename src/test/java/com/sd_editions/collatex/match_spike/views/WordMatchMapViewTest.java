package com.sd_editions.collatex.match_spike.views;

import java.util.List;

import junit.framework.TestCase;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.Util;
import com.sd_editions.collatex.match_spike.WordMatchMap;

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
