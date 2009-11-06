package eu.interedition.collatex.input;


// TODO: remove dependency on Word!
public abstract class BaseContainer {

  public abstract Word getWordOnPosition(final int k);

  public abstract int size();

}
