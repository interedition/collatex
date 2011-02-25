package eu.interedition.collatex2.legacy.gapdetection;

import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.nonpublic.modifications.IColumns;
import eu.interedition.collatex2.interfaces.nonpublic.modifications.IGap;

public class Gap implements IGap {
  private final IColumns columns;
  private final IPhrase phrase;
  private final IColumn nextColumn;
  private final IWitness witness;

  public Gap(IWitness witness, final IColumns columns, final IPhrase phrase, final IColumn nextColumn) {
    this.columns = columns;
    this.phrase = phrase;
    this.nextColumn = nextColumn;
    this.witness = witness;
  }

  @Override
  public String toString() {
    if (isAddition()) {
      return "\"" + phrase.getNormalized() + "\" added";
    }
    if (isOmission()) {
      return columns.toString() + " omitted";
    }
    return columns.toString() + " -> " + witness.getSigil() + ": " + phrase.getNormalized();
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
  public IColumn getNextColumn() {
    return nextColumn;
  }
}
