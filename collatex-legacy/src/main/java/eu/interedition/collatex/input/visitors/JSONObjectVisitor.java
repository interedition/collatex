/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.input.visitors;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.Word;

public class JSONObjectVisitor implements IResourceVisitor {

  private final JSONArray _jsonArray;
  private JSONObject _jsonObject;
  private List<JSONObject> _words;

  public JSONObjectVisitor() {
    _jsonArray = new JSONArray();
  }

  public void visitWitness(Witness witness) {
  // TODO Auto-generated method stub

  }

  @Override
  public void visitSegment(Segment segment) {
    _jsonObject = new JSONObject();
    _jsonObject.put("ID", segment.id);
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

  // TODO add punctuation!
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
