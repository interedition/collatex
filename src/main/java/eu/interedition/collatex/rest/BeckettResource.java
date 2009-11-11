package eu.interedition.collatex.rest;

import java.util.Arrays;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.sd_editions.collatex.match.SubsegmentExtractor;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.NewAlignmentTableCreator;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.WitnessSegmentPhrases;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class BeckettResource extends ServerResource {
  private static final MediaType[] TYPES = { MediaType.TEXT_HTML, MediaType.TEXT_PLAIN };

  public BeckettResource() {
    getVariants().put(Method.GET, Arrays.asList(TYPES));
  }

  @Override
  public Representation get(final Variant variant) throws ResourceException {
    String html;
    try {
      final SubsegmentExtractor sse = theSameExtractor();
      sse.go();
      final WitnessSegmentPhrases pa = sse.getWitnessSegmentPhrases("06-1");
      final WitnessSegmentPhrases pb = sse.getWitnessSegmentPhrases("06-2");
      final WitnessSegmentPhrases pc = sse.getWitnessSegmentPhrases("08-1");
      final WitnessSegmentPhrases pd = sse.getWitnessSegmentPhrases("08-2");
      final WitnessSegmentPhrases pe = sse.getWitnessSegmentPhrases("09-1");

      System.out.println(pa.toString());
      System.out.println(pb.toString());
      System.out.println(pc.toString());
      System.out.println(pd.toString());
      System.out.println(pe.toString());

      final AlignmentTable2 alignmentTable = NewAlignmentTableCreator.createNewAlignmentTable(pa, pb, pc, pd, pe);
      // HTML
      html = "<html><body> " + AlignmentTable2.alignmentTableToHTML(alignmentTable) + "</body></html>";
    } catch (final Exception e) {
      System.out.println(e);
      throw new RuntimeException(e);
    }
    final Representation representation = new StringRepresentation(html, MediaType.TEXT_HTML);
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
