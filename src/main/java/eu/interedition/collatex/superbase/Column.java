package eu.interedition.collatex.superbase;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class Column {

  protected Map<String, Word> wordsProWitness;
  private final List<Word> variants;

  public Column(Word word) {
    wordsProWitness = Maps.newHashMap();
    variants = Lists.newLinkedList();
    addVariant(word);
  }

  public void addMatch(Word word) {
    wordsProWitness.put(word.getWitnessId(), word);
  }

  public void addVariant(Word word) {
    wordsProWitness.put(word.getWitnessId(), word);
    variants.add(word);
  }

  public void toXML(StringBuilder builder) {
  // TODO Auto-generated method stub

  }

  @Override
  public String toString() {
    Collection<Word> values = wordsProWitness.values();
    String result = "";
    String delim = "";
    for (Word word : values) {
      result += delim + word.original;
      delim += " ";
    }
    return result;
  }

  public boolean containsWitness(Witness witness) {
    return wordsProWitness.containsKey(witness.id);
  }

  public Word getWord(Witness witness) {
    if (!containsWitness(witness)) {
      throw new NoSuchElementException();
    }
    Word result = wordsProWitness.get(witness.id);
    return result;
  }

  public Collection<Word> getWords() {
    Collection<Word> values = wordsProWitness.values();
    return values;
  }

  public void addToSuperbase(Superbase superbase) {
    for (Word variant : variants)
      superbase.addWord(variant, this);
  }

}
