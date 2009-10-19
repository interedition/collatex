package eu.interedition.collatex.rest;

import java.util.Arrays;

import net.sf.json.JSONObject;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.visitors.JSONObjectTableVisitor;
import eu.interedition.collatex.input.WitnessSet;

public class AlignmentResource extends ServerResource {

  private static final MediaType[] TYPES = { MediaType.TEXT_HTML, MediaType.TEXT_PLAIN };

  public AlignmentResource() {
    getVariants().put(Method.GET, Arrays.asList(TYPES));
  }

  @Override
  public Representation get(Variant variant) throws ResourceException {
    // String witnessString = getQuery().getFirstValue("witness");
    //    System.err.println("!!" + witnessString);
    String[] witnessStrings = getQuery().getValuesArray("witness");
    WitnessSet set = WitnessSet.createWitnessSet(witnessStrings);
    AlignmentTable2 alignmentTable = WitnessSet.createAlignmentTable(set);
    JSONObjectTableVisitor visitor = new JSONObjectTableVisitor();
    alignmentTable.accept(visitor);
    JSONObject jsonObject = visitor.getJSONObject();
    Representation representation = new JsonLibRepresentation(jsonObject);
    return representation;
  }

}
