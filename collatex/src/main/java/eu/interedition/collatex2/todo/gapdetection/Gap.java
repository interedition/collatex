package eu.interedition.collatex2.todo.gapdetection;

import eu.interedition.collatex2.interfaces.IInternalColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IPhrase;

public class Gap implements IGap {
  private final IColumns columns;
  private final IPhrase phrase;
  private final IInternalColumn nextColumn;

  public Gap(final IColumns columns, final IPhrase phrase, final IInternalColumn nextColumn) {
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
  public IInternalColumn getNextColumn() {
    return nextColumn;
  }
}
