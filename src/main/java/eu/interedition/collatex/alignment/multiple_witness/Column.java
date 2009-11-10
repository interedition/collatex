package eu.interedition.collatex.alignment.multiple_witness;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.alignment.multiple_witness.visitors.IAlignmentTableVisitor;
import eu.interedition.collatex.input.BaseElement;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;

public class Column<T extends BaseElement> {

  // TODO: rename Word to Element
  protected final Map<String, T> wordsProWitness;
  private final List<T> variants; // TODO: rename to unique words!
  private ColumnState state;

  public Column(final T word) {
    wordsProWitness = Maps.newLinkedHashMap();
    variants = Lists.newLinkedList();
    initColumn(word);
  }

  private void initColumn(final T word) {
    wordsProWitness.put(word.getWitnessId(), word);
    variants.add(word);
    state = ColumnState.NEW;
  }

  public void addMatch(final T word) {
    wordsProWitness.put(word.getWitnessId(), word);
    state = state.addMatch();
  }

  public void addVariant(final T word) {
    wordsProWitness.put(word.getWitnessId(), word);
    variants.add(word);
    state = state.addVariant();
  }

  public void toXML(final StringBuilder builder) {
  // TODO Auto-generated method stub

  }

  @Override
  public String toString() {
    final Collection<T> values = wordsProWitness.values();
    String result = "";
    String delim = "";
    for (final T word : values) {
      result += delim + word.getOriginal();
      delim += " ";
    }
    return result;
  }

  public boolean containsWitness(final Segment witness) {
    return wordsProWitness.containsKey(witness.id);
  }

  public T getWord(final Segment witness) {
    if (!containsWitness(witness)) {
      throw new NoSuchElementException();
    }
    final T result = wordsProWitness.get(witness.id);
    return result;
  }

  public Collection<T> getWords() {
    final Collection<T> values = wordsProWitness.values();
    return values;
  }

  public void addToSuperbase(final Superbase superbase) {
    for (final T variant : variants)
      superbase.addWord((Word) variant, this);
  }

  public ColumnState getColumnState() {
    return state;
  }

  public Set<String> getSigli() {
    return wordsProWitness.keySet();
  }

  public void accept(final IAlignmentTableVisitor<T> visitor) {
    visitor.visitColumn(this);
    final Set<String> sigli = this.getSigli();
    for (final String sigel : sigli) {
      final T word = wordsProWitness.get(sigel);
      visitor.visitElement(sigel, word);
    }
  }

  public List<T> getUniqueElements() {
    return variants;
  }

  public boolean containsWitness(final String witnessId) {
    return wordsProWitness.containsKey(witnessId);
  }

  public T getWord(final String witnessId) {
    if (!containsWitness(witnessId)) {
      throw new NoSuchElementException();
    }
    final T result = wordsProWitness.get(witnessId);
    return result;
  }

}
