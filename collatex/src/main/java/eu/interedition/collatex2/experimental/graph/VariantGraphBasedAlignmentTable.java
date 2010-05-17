package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import eu.interedition.collatex2.implementation.alignmenttable.BaseAlignmentTable;
import eu.interedition.collatex2.implementation.alignmenttable.Column3;
import eu.interedition.collatex2.interfaces.IAddition;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableVisitor;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IReplacement;
import eu.interedition.collatex2.interfaces.IRow;
import eu.interedition.collatex2.interfaces.IWitness;

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

  
  //for now I am going to do things lazy..
  //we will see where the actual init goes..
  public IRow getRow(IWitness witness) {
    if (!this.getSigli().contains(witness.getSigil())) {
      lazyConstructColumns(witness);
    }
    return super.getRow(witness.getSigil());
  }

  private void lazyConstructColumns(IWitness witness) {
    if (isEmpty()) {
      String sigil = witness.getSigil();
      getSigli().add(sigil);
      List<IVariantGraphArc> arcs = graph.getArcsForWitness(witness);
      for (IVariantGraphArc arc: arcs) {
        INormalizedToken token = arc.getToken(witness);
        addNewColumn(token);
      }
    } else {
      // NOTE: duplicated with above!
      getSigli().add(witness.getSigil());
      List<IVariantGraphArc> arcs = graph.getArcsForWitness(witness);
      // search the right column for each of the tokens of the next witness
      for (IVariantGraphArc arc: arcs) {
        INormalizedToken normalized = arc.getEndNode().getToken();
        boolean found = false;
        for (IColumn column : columns) {
          if (column.getVariants().contains(normalized)) {
            column.addMatch(arc.getToken(witness));
            found = true; // TODO: ugly!
          }
        }
        if (!found) {
          throw new RuntimeException("NOT YET IMPLEMENTED!");
        }
      }
    }
  }

  private void addNewColumn(INormalizedToken token) {
    columns.add(new Column3(token, -1));
  }

}
