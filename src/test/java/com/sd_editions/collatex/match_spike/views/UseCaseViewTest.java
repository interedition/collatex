package com.sd_editions.collatex.match_spike.views;

import junit.framework.TestCase;

import com.sd_editions.collatex.match_spike.views.UseCaseView;

public class UseCaseViewTest extends TestCase {

  private UseCaseView testUseCaseView;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    String[] testCase = new String[] { "The big dog", "the little dog", "the big cat" };
    testUseCaseView = new UseCaseView(testCase);
  }

  public void testToHtml() {
    String html = testUseCaseView.toHtml();
    assertTrue(html.contains("<span class=\"color1\">The</span> <span class=\"color2\">big</span> <span class=\"color3\">dog</span>"));
    assertTrue(html.contains("<span class=\"color1\">the</span> <span class=\"color4\">little</span> <span class=\"color3\">dog</span>"));
    assertTrue(html.contains("<span class=\"color1\">the</span> <span class=\"color2\">big</span> <span class=\"color5\">cat</span>"));
  }

}
