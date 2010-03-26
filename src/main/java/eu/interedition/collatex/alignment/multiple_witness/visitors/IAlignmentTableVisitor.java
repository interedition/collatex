package eu.interedition.collatex.alignment.multiple_witness.visitors;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.Column;
import eu.interedition.collatex.input.BaseElement;

public interface IAlignmentTableVisitor<T extends BaseElement> {

  public void visitTable(AlignmentTable2 table);

  public void postVisitTable(AlignmentTable2 table);

  public void visitColumn(Column<T> column);

  public void visitElement(String sigel, T element);
}
