package eu.interedition.collatex.input.visitors;

import java.util.List;

import net.sf.json.JSONObject;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.Word;

public class JSONObjectVisitor implements IResourceVisitor {

  private final JSONObject _jsonObject;
  private List<JSONObject> _words;

  public JSONObjectVisitor() {
    _jsonObject = new JSONObject();
  }

  @Override
  public void postVisitWitness(Witness witness) {
    _jsonObject.put("words", _words);
  }

  @Override
  public void visitWitness(Witness witness) {
    _jsonObject.put("ID", witness.id);
    _words = Lists.newArrayList();
  }

  @Override
  public void visitWitnessSet(WitnessSet witnessSet) {
  // TODO Auto-generated method stub

  }

  // TODO: add punctuation!
  @Override
  public void visitWord(Word word) {
    JSONObject w1 = new JSONObject();
    w1.put("content", word.original);
    _words.add(w1);
  }

  public JSONObject getJSONObject() {
    return _jsonObject;
  }

}
