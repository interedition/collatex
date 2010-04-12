package eu.interedition.collatex.rest;

import java.util.Arrays;

import net.sf.json.JSONArray;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.visitors.JSONObjectVisitor;

public class TokenizerResource extends ServerResource {

  private static final MediaType[] TYPES = { MediaType.TEXT_HTML, MediaType.TEXT_PLAIN };

  public TokenizerResource() {
    getVariants().put(Method.GET, Arrays.asList(TYPES));
  }

  @Override
  public Representation get(Variant variant) throws ResourceException {
    String[] witnessStrings = getQuery().getValuesArray("witness");
    WitnessSet set = WitnessSet.createWitnessSet(witnessStrings);
    JSONObjectVisitor visitor = new JSONObjectVisitor();
    set.accept(visitor);
    JSONArray jsonArray = visitor.getJsonArray();
    Representation representation = new JsonLibRepresentation(jsonArray);
    return representation;
  }

}
