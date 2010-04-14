package eu.interedition.collatex2.rest.resources;

import java.util.List;

import net.sf.json.JSONObject;

import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.rest.output.JSONObjectTableVisitor;
import eu.interedition.collatex2.rest.output.JsonLibRepresentation;

public class AlignmentResource extends AbstractHtmlTextResource {

  @Override
  public Representation get(final Variant variant) throws ResourceException {
    final String[] witnessStrings = getQuery().getValuesArray("witness");
    final CollateXEngine factory = new CollateXEngine();
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
