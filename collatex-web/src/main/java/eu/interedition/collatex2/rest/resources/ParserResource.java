package eu.interedition.collatex2.rest.resources;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.implementation.parallel_segmentation.AlignmentTableSegmentator;
import eu.interedition.collatex2.implementation.parallel_segmentation.ParallelSegmentationTable;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.rest.input.WitnessJsonBuilder;
import eu.interedition.collatex2.rest.output.JSONObjectTableVisitor;
import eu.interedition.collatex2.rest.output.JsonLibRepresentation;

public class ParserResource extends ServerResource {

  @Post
  public Representation acceptItem(final Representation entity) {

    final Form form = new Form(entity);
    final String firstValue = form.getFirstValue("request");
    final String format = form.getFirstValue("format", "json");

    JsonRepresentation jsonRepresentation;
    jsonRepresentation = new JsonRepresentation(firstValue);
    JSONArray jsonArray;
    List<IWitness> list;
    try {
      jsonArray = jsonRepresentation.getJsonArray();
      list = WitnessJsonBuilder.createList(jsonArray);
    } catch (final JSONException e) {
      throw new RuntimeException(e);
    }

    final Factory factory = new Factory();
    final IAlignmentTable alignmentTable = factory.createAlignmentTable(list);
    Representation representation = null;
    if( format.equals( "json")) {
    	final JSONObjectTableVisitor visitor = new JSONObjectTableVisitor();
    	alignmentTable.accept(visitor);
    	final net.sf.json.JSONObject jsonObject = visitor.getJSONObject();
    	representation = new JsonLibRepresentation(jsonObject);
    } else if( format.equals( "html" )) {
        final ParallelSegmentationTable table = AlignmentTableSegmentator.createParrallelSegmentationTable(alignmentTable);
        final StringBuilder stringBuilder = new StringBuilder("<html><body> ").//
            append(ParallelSegmentationTable.tableToHTML(table)).//
            append("</body></html>");
        representation = new StringRepresentation(stringBuilder.toString(), MediaType.TEXT_HTML);
    }

    return representation;

  }

}
