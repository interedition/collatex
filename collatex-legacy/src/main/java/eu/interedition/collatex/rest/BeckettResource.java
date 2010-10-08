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

package eu.interedition.collatex.rest;

import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.match.SubsegmentExtractor;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.NewAlignmentTableCreator;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.WitnessSegmentPhrases;
import eu.interedition.collatex.input.builders.WitnessBuilder;

//TODO: Port over to the new API!!
public class BeckettResource extends ServerResource {
  public BeckettResource() {
    getVariants().add(new Variant(MediaType.TEXT_HTML));
    getVariants().add(new Variant(MediaType.TEXT_PLAIN));
  }

  @Override
  public Representation get(final Variant variant) throws ResourceException {
    final Representation representation;
    try {
      final SubsegmentExtractor sse = theSameExtractor();
      sse.go();
      // TODO 11-2 en 12-1 subsegmenten gaan nog fout!
      //      sigli.remove("11-2");
      //      sigli.remove("12-1");

      final List<String> sigli = sse.getSigla();
      final List<String> shownSigli = sigli.subList(0, 26);
      shownSigli.remove("11-1"); // andleft is missing!
      shownSigli.remove("11-2"); // died is weggevallen
      shownSigli.remove("12-1"); // him goes wrong; died is weg and left 2x
      final List<WitnessSegmentPhrases> set = Lists.newLinkedList();
      for (final String sigil : shownSigli) {
        set.add(sse.getWitnessSegmentPhrases(sigil));
      }

      final AlignmentTable2 alignmentTable = NewAlignmentTableCreator.createNewAlignmentTable(set);
      final String[] output = getQuery().getValuesArray("output");
//      if (output.length > 0 && output[0].equals("xml")) {
//        final TeiParallelSegmentationTable createTEI = NewTeiCreator.createTEI(alignmentTable);
//        final String xml = createTEI.toXML();
//        representation = new StringRepresentation(xml, MediaType.APPLICATION_XML);
//      } else {
        final String html = "<html><body> " + AlignmentTable2.alignmentTableToHTML(alignmentTable) + "</body></html>";
        representation = new StringRepresentation(html, MediaType.TEXT_HTML);
//      }
    } catch (final Exception e) {
      System.out.println(e);
      throw new RuntimeException(e);
    }
    return representation;
  }

  private SubsegmentExtractor theSameExtractor() {
    final WitnessBuilder builder = new WitnessBuilder();
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
