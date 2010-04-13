package eu.interedition.collatex2.rest.resources;

import static com.google.common.collect.Iterables.transform;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.common.base.Function;
import com.google.common.base.Join;
import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.implementation.parallel_segmentation.AlignmentTableSegmentator;
import eu.interedition.collatex2.implementation.parallel_segmentation.ParallelSegmentationTable;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ICallback;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class DarwinResource extends ServerResource {
  private static final MediaType[] TYPES = { MediaType.TEXT_HTML, MediaType.TEXT_PLAIN };
  int[] fileNums = { 100, 110, 120, 200, 210, 600, 700, 800, 900, 1000, 1100, 1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000, 2100, 2200, 2300, 2400, 2500, 2600, 2700, 2800, 2900, 3000, 3100,
      3200, 3300, 3400, 3500, 3600, 3700, 3800, 3900, 4000, 4100, 4200, 4300, 4400, 4500, 4550, 4600 };
  protected static final Log LOG = LogFactory.getLog(DarwinResource.class);
  private String readFileToString;
  private final List<IWitness> witnesses = Lists.newArrayList();

  @SuppressWarnings("unchecked")
  public DarwinResource() {
    getVariants().put(Method.GET, Arrays.asList(TYPES));

  }

  @Override
  protected void doInit() throws ResourceException {}

  private static final ICallback LOG_ALIGNMENT = new ICallback() {
    @Override
    public void alignment(final IAlignment alignment) {
      LOG.info(alignment.getMatches().size());
    }
  };
  private int i;

  @Override
  public Representation get(final Variant variant) throws ResourceException {
    i = 0;
    try {
      i = Integer.parseInt((String) getRequest().getAttributes().get("i"));
    } catch (final NumberFormatException e) {}

    final File file = new File("docs/darwin/Ch1-" + fileNums[i] + ".json");
    try {
      readFileToString = FileUtils.readFileToString(file);
    } catch (final IOException e) {
      e.printStackTrace();
    }

    final Factory factory = new Factory();
    try {
      final List<String> sortedKeys = Lists.newArrayList();
      final JSONObject jsonObject = new JSONObject(readFileToString);
      final Iterator<String> keys = jsonObject.keys();
      while (keys.hasNext()) {
        final String key = keys.next();
        sortedKeys.add(key);
      }

      Collections.sort(sortedKeys);

      for (final String key : sortedKeys) {
        final String text = jsonObject.getString(key);
        final IWitness witness = factory.createWitness(key, text.replaceAll("  +", " ").trim());
        //        LOG.info(witness.getSigil());
        witnesses.add(witness);
      }
    } catch (final JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    final IAlignmentTable alignmentTable = factory.createAlignmentTable(witnesses, LOG_ALIGNMENT);
    final ParallelSegmentationTable table = AlignmentTableSegmentator.createParrallelSegmentationTable(alignmentTable);
    final StringBuilder stringBuilder = new StringBuilder("<html><body> ").//
        append(ParallelSegmentationTable.tableToHTML(table)).//
        append(witnessesAsString(witnesses)).//
        append("</body></html>");
    final Representation representation = new StringRepresentation(stringBuilder.toString(), MediaType.TEXT_HTML);
    return representation;
  }

  private static final Function<INormalizedToken, String> GETCONTENT = new Function<INormalizedToken, String>() {
    @Override
    public String apply(final INormalizedToken token) {
      return token.getContent();
    }
  };

  private String witnessesAsString(final List<IWitness> witnessList) {
    final StringBuilder builder = new StringBuilder();
    for (final IWitness w : witnessList) {
      builder.append(w.getSigil()).append(": ").append(Join.join(" ", transform(w.getTokens(), GETCONTENT)) + "<br/>");
    }
    return builder.toString();
  }
}
