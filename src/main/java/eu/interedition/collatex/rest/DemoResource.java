package eu.interedition.collatex.rest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.visitors.JSONObjectTableVisitor;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class DemoResource extends ServerResource {
  private static final MediaType[] TYPES = { MediaType.TEXT_HTML, MediaType.TEXT_PLAIN };
  private String readFileToString;
  private final WitnessSet set;

  @SuppressWarnings("unchecked")
  public DemoResource() {
    getVariants().put(Method.GET, Arrays.asList(TYPES));
    File file = new File("docs/demodirk/json_input.txt");
    try {
      readFileToString = FileUtils.readFileToString(file);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    List<Witness> witnesses = Lists.newArrayList();
    WitnessBuilder builder = new WitnessBuilder();
    try {
      JSONObject jsonObject = new JSONObject(readFileToString);
      Iterator<String> keys = jsonObject.keys();
      while (keys.hasNext()) {
        String key = keys.next();
        String text = jsonObject.getString(key);
        Witness witness = builder.build(key, text);
        witnesses.add(witness);
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    set = new WitnessSet(witnesses);
  }

  @Override
  public Representation get(Variant variant) throws ResourceException {
    //    Representation representation = new StringRepresentation(readFileToString);
    AlignmentTable2 alignmentTable = set.createAlignmentTable();
    JSONObjectTableVisitor visitor = new JSONObjectTableVisitor();
    alignmentTable.accept(visitor);
    net.sf.json.JSONObject jsonObject = visitor.getJSONObject();
    Representation representation = new JsonLibRepresentation(jsonObject);
    return representation;
  }
  //    try {
  //      JSONArray witnessArray = jsonRepresentation.getJsonArray();
  //      for (int w = 0; w < witnessArray.length(); w++) {
  //        JSONObject jsonObject = witnessArray.getJSONObject(w);
  //        Witness createWitness = createWitness(jsonObject);
  //        witnesses.add(createWitness);
  //      }
  //      WitnessSet set = new WitnessSet(witnesses);
  //      return set;
  //      //    } catch (IOException e) {
  //      //      e.printStackTrace();
  //      //      throw new RuntimeException(e);
  //    } catch (JSONException e) {
  //      e.printStackTrace();
  //      throw new RuntimeException(e);
  //    }

  //    public Witness createWitness(JSONObject object) throws JSONException {
  //      String id = object.getString("id");
  //      JSONArray jsonArray = object.getJSONArray("tokens");
  //      List<Word> words = Lists.newArrayList();
  //      int position = 1;
  //      for (int i = 0; i < jsonArray.length(); i++) {
  //        JSONObject jsonObject = jsonArray.getJSONObject(i);
  //        String token = jsonObject.getString("token");
  //        Word word = new Word(id, token, position);
  //        position++;
  //        words.add(word);
  //      }
  //      Witness witness = new Witness(id, words);
  //      return witness;
  //    }

}
