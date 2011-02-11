package eu.interedition.collatex2.todo.modifications;

import eu.interedition.collatex2.interfaces.nonpublic.modifications.IMatch;
import eu.interedition.collatex2.interfaces.nonpublic.modifications.ITransposition;

public class Transposition implements ITransposition {

  private final IMatch matchA;
  private final IMatch matchB;

  public Transposition(final IMatch match1, final IMatch match2) {
    this.matchA = match1;
    this.matchB = match2;
  }

  @Override
  public IMatch getMatchA() {
    return matchA;
  }

  @Override
  public IMatch getMatchB() {
    return matchB;
  }

  @Override
  public String toString() {
    return getMatchA().getNormalized() + " -> " + getMatchB().getNormalized();
  }
}
