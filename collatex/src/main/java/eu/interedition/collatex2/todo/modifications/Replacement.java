package eu.interedition.collatex2.todo.modifications;

import eu.interedition.collatex2.interfaces.IInternalColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IReplacement;

public class Replacement implements IReplacement {
  private final IColumns _original;
  private final IPhrase _replacement;
  private final IInternalColumn _nextColumn;

  private Replacement(final IColumns gapA, final IPhrase replacement, final IInternalColumn nextColumn) {
    _original = gapA;
    _replacement = replacement;
    _nextColumn = nextColumn;
  }

  @Override
  public String toString() {
    final String baseWords = _original.toString();
    // TODO Not getNormalized!
    final String replacementWords = _replacement.getNormalized();
    return "replacement: " + baseWords + " / " + replacementWords + " position: " + _original.getFirstColumn().getPosition();
  }

  public int getPosition() {
    return _original.getFirstColumn().getPosition();
  }

  public IColumns getOriginalColumns() {
    return _original;
  }

  public IPhrase getReplacementPhrase() {
    return _replacement;
  }

  //TODO: do we need to make this defensive?
  @Override
  public IInternalColumn getNextColumn() {
    return _nextColumn;
  }

  public static IReplacement create(IGap gap) {
    return new Replacement(gap.getColumns(), gap.getPhrase(), gap.getNextColumn());
  }

  //  @Override
  //  public void accept(final ModificationVisitor modificationVisitor) {
  //    modificationVisitor.visitReplacement(this);
  //  }

}
