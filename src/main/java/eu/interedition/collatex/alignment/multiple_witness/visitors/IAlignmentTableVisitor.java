package eu.interedition.collatex.alignment.multiple_witness.visitors;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.Column;
import eu.interedition.collatex.input.Word;

public interface IAlignmentTableVisitor {

  public void visitTable(AlignmentTable2 table);

  public void postVisitTable(AlignmentTable2 table);

  public void visitColumn(Column column);

  public void visitWord(String sigel, Word word);
}
