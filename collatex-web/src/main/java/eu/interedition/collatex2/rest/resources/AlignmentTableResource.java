package eu.interedition.collatex2.rest.resources;

import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableResource extends ServerResource {

  public AlignmentTableResource() {
    getVariants().add(new Variant(MediaType.TEXT_HTML));
    getVariants().add(new Variant(MediaType.TEXT_PLAIN));
  }

  @Override
  public Representation get(final Variant variant) throws ResourceException {
    final Factory factory = new Factory();
    final IWitness w1 = factory.createWitness("A", "the black cat");
    final IWitness w2 = factory.createWitness("B", "the white cat");
    final IWitness w3 = factory.createWitness("C", "the red cat");
    final List<IWitness> set = Lists.newArrayList(w1, w2, w3);
    final IAlignmentTable alignmentTable = factory.createAlignmentTable(set);
    // TODO: not finished!
    return new StringRepresentation("We are done");
  }
}
