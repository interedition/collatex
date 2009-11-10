package eu.interedition.collatex.input;

public abstract class BaseContainer<T extends BaseElement> {

  public abstract T getElementOnWordPosition(final int k);

  public abstract int wordSize();

}
