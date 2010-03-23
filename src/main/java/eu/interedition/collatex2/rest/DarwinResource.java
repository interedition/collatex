package eu.interedition.collatex2.rest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.implementation.alignmenttable.AlignmentTable4;
import eu.interedition.collatex2.implementation.alignmenttable.AlignmentTableCreator3;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

public class DarwinResource extends ServerResource {
  private static final MediaType[] TYPES = { MediaType.TEXT_HTML, MediaType.TEXT_PLAIN };
  private String readFileToString;
  private final List<IWitness> witnesses = Lists.newArrayList();

  @SuppressWarnings("unchecked")
  public DarwinResource() {
    getVariants().put(Method.GET, Arrays.asList(TYPES));
    final File file = new File("docs/darwin/Ch1-100.json");
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
        final IWitness witness = factory.createWitness(key, text);
        witnesses.add(witness);
      }
    } catch (final JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public Representation get(final Variant variant) throws ResourceException {
    final IAlignmentTable alignmentTable = AlignmentTableCreator3.createAlignmentTable(witnesses);
    final String html = "<html><body> " + witnessesAsString(witnesses) + AlignmentTable4.alignmentTableToHTML(alignmentTable) + "</body></html>";
    final Representation representation = new StringRepresentation(html, MediaType.TEXT_HTML);
    return representation;
  }

  private String witnessesAsString(final List<IWitness> witnessList) {
    final StringBuilder builder = new StringBuilder();
    for (final IWitness w : witnessList) {
      builder.append(w.toString() + "<br/>");
    }
    return builder.toString();
  }

}
