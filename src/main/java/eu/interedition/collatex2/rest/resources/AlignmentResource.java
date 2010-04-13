package eu.interedition.collatex2.rest.resources;

import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONObject;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.rest.output.JSONObjectTableVisitor;
import eu.interedition.collatex2.rest.output.JsonLibRepresentation;

public class AlignmentResource extends ServerResource {

  private static final MediaType[] TYPES = { MediaType.TEXT_HTML, MediaType.TEXT_PLAIN };

  public AlignmentResource() {
    getVariants().put(Method.GET, Arrays.asList(TYPES));
  }

  @Override
  public Representation get(final Variant variant) throws ResourceException {
    final String[] witnessStrings = getQuery().getValuesArray("witness");
    final Factory factory = new Factory();
    final List<IWitness> witnesses = Lists.newArrayList();
    int i = 1;
    for (final String witnessString : witnessStrings) {
      final IWitness witness = factory.createWitness("witness" + i++, witnessString);
      witnesses.add(witness);
    }

    final IAlignmentTable alignmentTable = factory.createAlignmentTable(witnesses);
    final JSONObjectTableVisitor visitor = new JSONObjectTableVisitor();
    alignmentTable.accept(visitor);
    final JSONObject jsonObject = visitor.getJSONObject();
    final Representation representation = new JsonLibRepresentation(jsonObject);
    return representation;
  }

}
