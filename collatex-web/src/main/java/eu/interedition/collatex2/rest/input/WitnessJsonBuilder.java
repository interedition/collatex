package eu.interedition.collatex2.rest.input;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.input.NormalizedWitness;
import eu.interedition.collatex2.implementation.input.Token;
import eu.interedition.collatex2.implementation.tokenization.DefaultTokenNormalizer;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenNormalizer;
import eu.interedition.collatex2.interfaces.IWitness;

public class WitnessJsonBuilder {
  private static ITokenNormalizer tokenNormalizer = new DefaultTokenNormalizer();
  
  public static List<IWitness> createList(final JSONArray witnessArray) throws JSONException {
    final List<IWitness> witnesses = Lists.newArrayList();
    for (int w = 0; w < witnessArray.length(); w++) {
      final JSONObject jsonObject = witnessArray.getJSONObject(w);
      final IWitness witness = createWitness(jsonObject);
      witnesses.add(witness);
    }
    return witnesses;
  }

  public static IWitness createWitness(final JSONObject object) throws JSONException {
    final String sigil = object.getString("id");
    final JSONArray jsonArray = object.getJSONArray("tokens");
    final List<INormalizedToken> normalizedTokens = Lists.newArrayList();
    int position = 1;
    for (int i = 0; i < jsonArray.length(); i++) {
      final JSONObject jsonObject = jsonArray.getJSONObject(i);
      final String tokenString = jsonObject.getString("token");
      final Token token = new Token(sigil, tokenString, position);
      final INormalizedToken normalizedToken = tokenNormalizer.apply(token);
      position++;
      normalizedTokens.add(normalizedToken);
    }
    final IWitness witness = new NormalizedWitness(sigil, normalizedTokens);
    return witness;
  }
}
