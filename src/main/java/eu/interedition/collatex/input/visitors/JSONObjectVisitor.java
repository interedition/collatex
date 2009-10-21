package eu.interedition.collatex.input.visitors;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.Word;

public class JSONObjectVisitor implements IResourceVisitor {

  private final JSONArray _jsonArray;
  private JSONObject _jsonObject;
  private List<JSONObject> _words;

  public JSONObjectVisitor() {
    _jsonArray = new JSONArray();
  }

  @Override
  public void visitWitness(Segment witness) {
    _jsonObject = new JSONObject();
    _jsonObject.put("ID", witness.id);
    _words = Lists.newArrayList();
  }

  @Override
  public void postVisitWitness(Segment witness) {
    _jsonObject.put("tokens", _words);
    _jsonArray.add(_jsonObject);
  }

  @Override
  public void visitWitnessSet(WitnessSet witnessSet) {
  // TODO Auto-generated method stub

  }

  // TODO: add punctuation!
  @Override
  public void visitWord(Word word) {
    JSONObject w1 = new JSONObject();
    w1.put("token", word.original);
    _words.add(w1);
  }

  public JSONArray getJsonArray() {
    return _jsonArray;
  }
}
