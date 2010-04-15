package eu.interedition.collatex2.implementation.alignment;

import eu.interedition.collatex2.implementation.modifications.Addition;
import eu.interedition.collatex2.implementation.modifications.Omission;
import eu.interedition.collatex2.implementation.modifications.Replacement;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IModification;
import eu.interedition.collatex2.interfaces.IPhrase;

public class Gap implements IGap {
  private final IColumns columns;
  private final IPhrase phrase;
  private final IColumn nextColumn;

  //TODO decouple gaps and modifications... 
  //TODO Modifications should know about gaps not the other way around!
  public Gap(final IColumns columns, final IPhrase phrase, final IColumn nextColumn) {
    this.columns = columns;
    this.phrase = phrase;
    this.nextColumn = nextColumn;
  }

  @Override
  public String toString() {
    if (isAddition()) {
      return "\"" + phrase.getNormalized() + "\" added";
    }
    if (isOmission()) {
      return columns.toString() + " omitted";
    }
    return columns.toString() + " -> " + phrase.getSigil() + ": " + phrase.getNormalized();
  }

  public IColumns getColumns() {
    return columns;
  }

  public IPhrase getPhrase() {
    return phrase;
  }

  public boolean isEmpty() {
    return columns.isEmpty() && phrase.isEmpty();
  }

  public boolean isReplacement() {
    return !columns.isEmpty() && !phrase.isEmpty();
  }

  public boolean isAddition() {
    return columns.isEmpty() && !phrase.isEmpty();
  }

  public boolean isOmission() {
    return !columns.isEmpty() && phrase.isEmpty();
  }

  @Override
  public IColumn getNextColumn() {
    return nextColumn;
  }

  //TODO: remove method!
  public IModification getModification() {
    if (isAddition()) {
      return Addition.create(this);
    }
    if (isOmission()) {
      return createOmission(this);
    }
    if (isReplacement()) {
      return createReplacement(this);
    }
    throw new RuntimeException("Not a modification!");
  }
  
  private static IModification createOmission(IGap gap) {
    return new Omission(gap.getColumns());
  }

  private static IModification createReplacement(IGap gap) {
    return new Replacement(gap.getColumns(), gap.getPhrase(), gap.getNextColumn());
  }
}
