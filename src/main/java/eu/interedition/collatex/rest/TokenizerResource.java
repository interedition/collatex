package eu.interedition.collatex.rest;

import java.util.Arrays;

import net.sf.json.JSONObject;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.builders.WitnessBuilder;
import eu.interedition.collatex.input.visitors.JSONObjectVisitor;

public class TokenizerResource extends ServerResource {

  private static final MediaType[] TYPES = { MediaType.TEXT_HTML, MediaType.TEXT_PLAIN };

  public TokenizerResource() {
    getVariants().put(Method.GET, Arrays.asList(TYPES));
  }

  @Override
  public Representation get(Variant variant) throws ResourceException {
    String witnessString = getQuery().getFirstValue("witness");
    //    System.err.println("!!" + witnessString);
    WitnessBuilder builder = new WitnessBuilder();
    Witness witness = builder.build("witness", witnessString);
    JSONObjectVisitor visitor = new JSONObjectVisitor();
    witness.accept(visitor);
    JSONObject jsonObject = visitor.getJSONObject();
    Representation representation = new JsonLibRepresentation(jsonObject);
    return representation;
  }

}
