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

package eu.interedition.collatex.match;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.sd_editions.collatex.match.SubsegmentExtractor;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.UnfixedAlignment;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.WitnessSegmentPhrases;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class IntegrationTest {
  private WitnessBuilder builder;

  @Before
  public void setup() {
    builder = new WitnessBuilder();
  }

  @Test
  public void testPairWiseAlignment() {
    // Segmentation
    final SubsegmentExtractor sse = theSameExtractor();
    sse.go();
    final WitnessSegmentPhrases pa = sse.getWitnessSegmentPhrases("06-1");
    final WitnessSegmentPhrases pb = sse.getWitnessSegmentPhrases("06-2");
    System.out.println(pa.toString());
    System.out.println(pb.toString());
    // Matching
    final UnfixedAlignment<Phrase> match = Matcher.match(pa, pb);
    Assert.assertEquals(6, match.size());
    System.out.println(match);
    // Alignment
    final Alignment<Phrase> alignment = Alignment.createPhraseAlignment(match, pa, pb);
    final List<Gap<Phrase>> gaps = alignment.getGaps();
    //    System.out.println(gaps.toString());
    Assert.assertEquals(1, gaps.size());
  }

  private SubsegmentExtractor theSameExtractor() {
    final Segment a = builder.build("06-1", "The same clock as when for example Magee once died.").getFirstSegment();
    final Segment b = builder.build("06-2", "The same as when for example Magee once died.").getFirstSegment();
    final Segment c = builder.build("08-1", "The same as when for example McKee once died .").getFirstSegment();
    final Segment d = builder.build("08-2", "The same as when among others Darly once died &amp; left him.").getFirstSegment();
    final Segment e = builder.build("09-1", "The same as when Darly among others once died and left him.").getFirstSegment();
    final Segment f = builder.build("09-2", "The same as when Darly among others once died and left him.").getFirstSegment();
    final Segment g = builder.build("10-1", "The same as when Darly among others once died and left him.").getFirstSegment();
    final Segment h = builder.build("10-2", "The same as when Darly among others once went and left him.").getFirstSegment();
    final Segment i = builder.build("11-1", "The same as when among others Darly once went andleft him ").getFirstSegment();
    final Segment j = builder.build("11-2", "The same as when among others Darly once died on him &amp; left him.").getFirstSegment();
    final Segment k = builder.build("12-1", "The same as when among others Darly once died and left left him.").getFirstSegment();
    final Segment l = builder.build("12-2", "The same as when among others Darly once died and left him.").getFirstSegment();
    final Segment m = builder.build("13-1", "The same as when among others Darly pnce died and left him.").getFirstSegment();
    final Segment n = builder.build("13-2", "The same as when among others Darly once died and left him.").getFirstSegment();
    final Segment o = builder.build("14-1", "The same as when among others Darly pnce died and left him.").getFirstSegment();
    final Segment p = builder.build("14-2", "The same as when among others Darly once died and left him.").getFirstSegment();
    final Segment q = builder.build("15-1", "The same as when among others Darly once died and left him.").getFirstSegment();
    final Segment r = builder.build("15-2", "The same as when among others Darly once died and left him.").getFirstSegment();
    final Segment s = builder.build("16-1", "The same as when among others Darly once died and left him.").getFirstSegment();
    final Segment t = builder.build("16-2", "The same as when among others Darly once died and left him.").getFirstSegment();
    final Segment u = builder.build("17-1", "The same as when among others Darly once died and left him.").getFirstSegment();
    final Segment v = builder.build("17-2", "The same as when among others Darly once died and left him.").getFirstSegment();
    final Segment w = builder.build("19-1", "The same as when among others Darly once died and left him.").getFirstSegment();
    final Segment x = builder.build("19-2", "The same as when among others Darly once died and left him.").getFirstSegment();
    final Segment y = builder.build("BS-1", "The same as when among others Darly once died and left him.").getFirstSegment();
    final Segment z = builder.build("BS-2", "The same as when among others Darly once died and left him.").getFirstSegment();
    final SubsegmentExtractor sse = new SubsegmentExtractor(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z);
    //    final SubsegmentExtractor sse = new SubsegmentExtractor(a, d);
    return sse;
  }
}
