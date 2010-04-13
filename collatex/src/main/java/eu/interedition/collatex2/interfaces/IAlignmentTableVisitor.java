package eu.interedition.collatex2.interfaces;


public interface IAlignmentTableVisitor {

  public void visitTable(IAlignmentTable table);

  public void postVisitTable(IAlignmentTable table);

  public void visitColumn(IColumn column);

  public void visitToken(String sigel, INormalizedToken token);
}
