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

package eu.interedition.collatex.rest;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.AlignmentTableCreator;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class DemoResource extends ServerResource {
  private String readFileToString;
  private final WitnessSet set;

  @SuppressWarnings("unchecked")
  public DemoResource() {
    getVariants().add(new Variant(MediaType.TEXT_HTML));
    getVariants().add(new Variant(MediaType.TEXT_PLAIN));
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
      List<String> sortedKeys = Lists.newArrayList();
      JSONObject jsonObject = new JSONObject(readFileToString);
      Iterator<String> keys = jsonObject.keys();
      while (keys.hasNext()) {
        String key = keys.next();
        sortedKeys.add(key);
      }

      Collections.sort(sortedKeys);

      for (String key : sortedKeys) {
        String text = jsonObject.getString(key);
        Witness witness = builder.build(key, text);
        witnesses.add(witness);
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // limit the number of witnesses by cutting something of the list (just a temp measure!)

    witnesses = witnesses.subList(0, 2);

    set = new WitnessSet(witnesses);
  }

  @Override
  public Representation get(Variant variant) throws ResourceException {
    //    Representation representation = new StringRepresentation(readFileToString);
    AlignmentTable2 alignmentTable = AlignmentTableCreator.createAlignmentTable(set);
    // HTML
    String html = "<html><body> " + witnessesAsString(set) + AlignmentTable2.alignmentTableToHTML(alignmentTable) + "</body></html>";
    Representation representation = new StringRepresentation(html, MediaType.TEXT_HTML);
    // TEI
    //    String xml = alignmentTable.toXML();
    //    //    JSONObjectTableVisitor visitor = new JSONObjectTableVisitor();
    //    //    alignmentTable.accept(visitor);
    //    //    JSONObject jsonObject = visitor.getJSONObject();
    //    //    Representation representation = new JsonLibRepresentation(jsonObject);
    //    Representation representation = new StringRepresentation(xml, MediaType.APPLICATION_XML);
    // Representation representation = null;

    // JSON
    //    JSONObjectTableVisitor visitor = new JSONObjectTableVisitor();
    //    alignmentTable.accept(visitor);
    //    net.sf.json.JSONObject jsonObject = visitor.getJSONObject();
    //    Representation representation = new JsonLibRepresentation(jsonObject);
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

  private String witnessesAsString(WitnessSet set2) {
    StringBuilder builder = new StringBuilder();
    for (Witness w : set2.getWitnesses()) {
      builder.append(w.toString() + "<br/>");
    }
    // TODO Auto-generated method stub
    return builder.toString();
  }

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
