package eu.interedition.collatex.input.builders;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.Word;

public class WitnessJsonBuilder {

  public static Segment createWitness(JSONObject object) throws JSONException {
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
    Segment witness = new Segment(id, words);
    return witness;
  }

  public static WitnessSet createSet(JSONArray witnessArray) throws JSONException {
    List<Segment> witnesses = Lists.newArrayList();
    for (int w = 0; w < witnessArray.length(); w++) {
      JSONObject jsonObject = witnessArray.getJSONObject(w);
      Segment witness = createWitness(jsonObject);
      witnesses.add(witness);
    }
    WitnessSet set = new WitnessSet(witnesses);
    return set;
  }

}
