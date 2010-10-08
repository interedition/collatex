package eu.interedition.collatex.rest;

import net.sf.json.JSONArray;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.visitors.JSONObjectVisitor;
import eu.interedition.collatex2.rest.output.JsonLibRepresentation;

public class TokenizerResource extends ServerResource {

  public TokenizerResource() {
    getVariants().add(new Variant(MediaType.TEXT_HTML));
    getVariants().add(new Variant(MediaType.TEXT_PLAIN));
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
