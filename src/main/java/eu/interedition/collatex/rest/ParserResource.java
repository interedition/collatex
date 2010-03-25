package eu.interedition.collatex.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.restlet.data.Form;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.AlignmentTableCreator;
import eu.interedition.collatex.alignment.multiple_witness.visitors.JSONObjectTableVisitor;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.builders.WitnessJsonBuilder;

public class ParserResource extends ServerResource {

  @Post
  public Representation acceptItem(Representation entity) {
    System.err.println("Handeling POST!");

    Form form = new Form(entity);
    String firstValue = form.getFirstValue("request");

    JsonRepresentation jsonRepresentation;
    jsonRepresentation = new JsonRepresentation(firstValue);
    JSONArray jsonArray;
    WitnessSet set;
    try {
      jsonArray = jsonRepresentation.getJsonArray();
      set = WitnessJsonBuilder.createSet(jsonArray);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }

    // Note: duplication with AlignmentResource!
    AlignmentTable2 alignmentTable = AlignmentTableCreator.createAlignmentTable(set);
    JSONObjectTableVisitor visitor = new JSONObjectTableVisitor();
    alignmentTable.accept(visitor);
    net.sf.json.JSONObject jsonObject = visitor.getJSONObject();
    Representation representation = new JsonLibRepresentation(jsonObject);

    return representation;

  }

}
