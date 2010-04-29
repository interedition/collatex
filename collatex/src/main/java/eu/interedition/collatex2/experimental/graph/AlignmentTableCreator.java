package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import eu.interedition.collatex2.implementation.alignmenttable.AlignmentTable4;
import eu.interedition.collatex2.implementation.alignmenttable.Column3;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableCreator {

  private final IVariantGraph graph;

  //TODO: instead of a creator object.. make it an actual IAlignmentTable implementation
  //ofcourse based on a VariantGraph
  public AlignmentTableCreator(IVariantGraph graph) {
    this.graph = graph;
  }

  public IAlignmentTable getAlignmentTable() {
    IAlignmentTable table = new AlignmentTable4();
    if (graph.isEmpty()) {
      return table;
    }
    for (IWitness witness : graph.getWitnesses()) {
      if (table.isEmpty()) {
        table.getSigli().add(witness.getSigil());
        List<IVariantGraphNode> path = graph.getPath(witness);
        //now use the path to fill the row ... 
        for (IVariantGraphNode node : path) {
          table.add(new Column3(node.getToken(), -1));
        }
      }
    }
    return table;
  }


}
