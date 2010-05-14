package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import eu.interedition.collatex2.implementation.alignmenttable.BaseAlignmentTable;
import eu.interedition.collatex2.interfaces.IAddition;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableVisitor;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IReplacement;

public class VariantGraphBasedAlignmentTable extends BaseAlignmentTable implements IAlignmentTable {

  private final VariantGraph graph;

  public VariantGraphBasedAlignmentTable(VariantGraph graph) {
    this.graph = graph;
  }

  @Override
  public void accept(IAlignmentTableVisitor visitor) {
    // TODO Auto-generated method stub

  }

  @Override
  public void add(IColumn column) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addAddition(IAddition addition) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addReplacement(IReplacement replacement) {
    // TODO Auto-generated method stub

  }

  @Override
  public IColumns createColumns(int startIndex, int endIndex) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> findRepeatingTokens() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<IColumn> getColumns() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getSigli() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isEmpty() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public int size() {
    // TODO Auto-generated method stub
    return 0;
  }

}
