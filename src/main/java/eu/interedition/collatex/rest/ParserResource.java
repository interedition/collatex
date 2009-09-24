package eu.interedition.collatex.rest;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.visitors.JSONObjectTableVisitor;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.Word;

public class ParserResource extends ServerResource {

  @Post
  public Representation acceptItem(Representation entity) {
    System.err.println("Handeling POST!");

    Form form = new Form(entity);
    String firstValue = form.getFirstValue("request");

    JsonRepresentation jsonRepresentation;
    jsonRepresentation = new JsonRepresentation(firstValue);
    WitnessSet set = createSet(jsonRepresentation);

    // Note: duplication with AlignmentResource!
    AlignmentTable2 alignmentTable = set.createAlignmentTable();
    JSONObjectTableVisitor visitor = new JSONObjectTableVisitor();
    alignmentTable.accept(visitor);
    net.sf.json.JSONObject jsonObject = visitor.getJSONObject();
    Representation representation = new JsonLibRepresentation(jsonObject);

    return representation;

  }

  public WitnessSet createSet(JsonRepresentation jsonRepresentation) {
    List<Witness> witnesses = Lists.newArrayList();
    try {
      JSONArray witnessArray = jsonRepresentation.getJsonArray();
      for (int w = 0; w < witnessArray.length(); w++) {
        JSONObject jsonObject = witnessArray.getJSONObject(w);
        Witness createWitness = createWitness(jsonObject);
        witnesses.add(createWitness);
      }
      WitnessSet set = new WitnessSet(witnesses);
      return set;
      //    } catch (IOException e) {
      //      e.printStackTrace();
      //      throw new RuntimeException(e);
    } catch (JSONException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

  }

  public Witness createWitness(JSONObject object) throws JSONException {
    String id = object.getString("id");
    JSONArray jsonArray = object.getJSONArray("tokens");
    List<Word> words = Lists.newArrayList();
    int position = 1;
    for (int i = 0; i < jsonArray.length(); i++) {
      JSONObject jsonObject = jsonArray.getJSONObject(i);
      String token = jsonObject.getString("token");
      Word word = new Word(id, token, position);
      position++;
      words.add(word);
    }
    Witness witness = new Witness(id, words);
    return witness;
  }

}
