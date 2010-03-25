package eu.interedition.collatex2.implementation.modifications;

import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.ITransposition;

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

}
