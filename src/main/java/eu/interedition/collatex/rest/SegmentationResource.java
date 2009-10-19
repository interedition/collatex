package eu.interedition.collatex.rest;

import java.util.Arrays;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.input.WitnessSet;

public class SegmentationResource extends ServerResource {

  private static final MediaType[] TYPES = { MediaType.TEXT_HTML, MediaType.TEXT_PLAIN };

  public SegmentationResource() {
    getVariants().put(Method.GET, Arrays.asList(TYPES));
  }

  @Override
  public Representation get(Variant variant) throws ResourceException {
    // String witnessString = getQuery().getFirstValue("witness");
    //    System.err.println("!!" + witnessString);
    String[] witnessStrings = getQuery().getValuesArray("witness");
    WitnessSet set = WitnessSet.createWitnessSet(witnessStrings);
    AlignmentTable2 alignmentTable = WitnessSet.createAlignmentTable(set);
    // TODO: make a visitor out of this! (this is actually tei parallel segmentation)
    String xml = alignmentTable.toXML();
    //    JSONObjectTableVisitor visitor = new JSONObjectTableVisitor();
    //    alignmentTable.accept(visitor);
    //    JSONObject jsonObject = visitor.getJSONObject();
    //    Representation representation = new JsonLibRepresentation(jsonObject);
    Representation representation = new StringRepresentation(xml, MediaType.APPLICATION_XML);
    // Representation representation = null;
    return representation;
  }

}
