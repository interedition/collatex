package eu.interedition.collatex.rest;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.AlignmentTableCreator;
import eu.interedition.collatex.input.WitnessSet;

public class SegmentationResource extends ServerResource {

  public SegmentationResource() {
    getVariants().add(new Variant(MediaType.TEXT_HTML));
    getVariants().add(new Variant(MediaType.TEXT_PLAIN));
  }

  @Override
  public Representation get(Variant variant) throws ResourceException {
    // String witnessString = getQuery().getFirstValue("witness");
    // System.err.println("!!" + witnessString);
    String[] witnessStrings = getQuery().getValuesArray("witness");
    WitnessSet set = WitnessSet.createWitnessSet(witnessStrings);
    AlignmentTable2 alignmentTable = AlignmentTableCreator.createAlignmentTable(set);
    // TODO make a visitor out of this! (this is actually tei parallel
    // segmentation)
    String xml = alignmentTable.toXML();
    // JSONObjectTableVisitor visitor = new JSONObjectTableVisitor();
    // alignmentTable.accept(visitor);
    // JSONObject jsonObject = visitor.getJSONObject();
    // Representation representation = new JsonLibRepresentation(jsonObject);
    Representation representation = new StringRepresentation(xml, MediaType.APPLICATION_XML);
    // Representation representation = null;
    return representation;
  }

}
