package eu.interedition.collatex2.rest.resources;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableResource extends AbstractHtmlTextResource {
  private CollateXEngine factory = new CollateXEngine();

  @Override
  public Representation get(final Variant variant) throws ResourceException {
    final IWitness w1 = factory.createWitness("A", "the black cat");
    final IWitness w2 = factory.createWitness("B", "the white cat");
    final IWitness w3 = factory.createWitness("C", "the red cat");
    final IAlignmentTable alignmentTable = factory.align(w1, w2, w3);
    // TODO: not finished!
    return new StringRepresentation("We are done");
  }
}
