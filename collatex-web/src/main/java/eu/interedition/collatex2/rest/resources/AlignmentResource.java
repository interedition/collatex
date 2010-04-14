package eu.interedition.collatex2.rest.resources;

import net.sf.json.JSONObject;

import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAligner;
import eu.interedition.collatex2.rest.output.JSONObjectTableVisitor;
import eu.interedition.collatex2.rest.output.JsonLibRepresentation;

public class AlignmentResource extends AbstractHtmlTextResource {

  private CollateXEngine factory = new CollateXEngine();

  @Override
  public Representation get(final Variant variant) throws ResourceException {
    IAligner aligner = factory.createAligner();
    int i = 1;
    for (final String witnessString : getQuery().getValuesArray("witness")) {
      aligner.add(factory.createWitness("witness" + i++, witnessString));
    }

    final JSONObjectTableVisitor visitor = new JSONObjectTableVisitor();
    aligner.getResult().accept(visitor);
    final JSONObject jsonObject = visitor.getJSONObject();
    final Representation representation = new JsonLibRepresentation(jsonObject);
    return representation;
  }

}
