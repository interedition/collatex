package eu.interedition.collatex2.legacy.gapdetection;

import eu.interedition.collatex2.interfaces.IInternalColumn;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.nonpublic.modifications.IColumns;
import eu.interedition.collatex2.interfaces.nonpublic.modifications.IGap;

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

  @Override
  public IColumns getColumns() {
    return columns;
  }

  @Override
  public IPhrase getPhrase() {
    return phrase;
  }

  @Override
  public boolean isEmpty() {
    return columns.isEmpty() && phrase.isEmpty();
  }

  @Override
  public boolean isReplacement() {
    return !columns.isEmpty() && !phrase.isEmpty();
  }

  @Override
  public boolean isAddition() {
    return columns.isEmpty() && !phrase.isEmpty();
  }

  @Override
  public boolean isOmission() {
    return !columns.isEmpty() && phrase.isEmpty();
  }

  @Override
  public IInternalColumn getNextColumn() {
    return nextColumn;
  }
}
