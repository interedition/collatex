package eu.interedition.collatex.input;

public abstract class BaseContainer<T extends BaseElement> {

  // TODO: rename?
  public abstract T getWordOnPosition(final int k);

  public abstract int size();

}
