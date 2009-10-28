package eu.interedition.collatex.input.builders;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.Word;

public class WitnessJsonBuilder {

  public static Witness createWitness(JSONObject object) throws JSONException {
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
    Witness witness = new Witness(new Segment(id, words));
    return witness;
  }

  public static WitnessSet createSet(JSONArray witnessArray) throws JSONException {
    List<Witness> witnesses = Lists.newArrayList();
    for (int w = 0; w < witnessArray.length(); w++) {
      JSONObject jsonObject = witnessArray.getJSONObject(w);
      Witness witness = createWitness(jsonObject);
      witnesses.add(witness);
    }
    WitnessSet set = new WitnessSet(witnesses);
    return set;
  }

}
