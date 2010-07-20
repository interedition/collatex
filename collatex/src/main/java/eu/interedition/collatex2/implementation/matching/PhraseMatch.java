package eu.interedition.collatex2.implementation.matching;

import eu.interedition.collatex2.experimental.vg_alignment.IMatch2;
import eu.interedition.collatex2.interfaces.IPhrase;

public class PhraseMatch implements IMatch2 {

  private final IPhrase tablePhrase;
  private final IPhrase phrase; //witness

  public PhraseMatch(IPhrase tablePhrase, IPhrase phrase) {
    this.tablePhrase = tablePhrase;
    this.phrase = phrase;
  }

  public IPhrase getTablePhrase() {
    return tablePhrase;
  }

  public IPhrase getPhrase() {
    return phrase;
  }
  
  @Override
  public String toString() {
    return phrase+" -> "+tablePhrase;
  }

  @Override
  public String getNormalized() {
    return phrase.getNormalized();
  }

}
