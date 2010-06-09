package eu.interedition.collatex2.experimental.table;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.experimental.graph.VariantGraph;
import eu.interedition.collatex2.implementation.alignmenttable.BaseAlignmentTable;
import eu.interedition.collatex2.interfaces.IAddition;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableVisitor;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IReplacement;
import eu.interedition.collatex2.interfaces.IRow;
import eu.interedition.collatex2.interfaces.IWitness;

public class DirectedAcyclicGraphBasedAlignmentTable extends BaseAlignmentTable implements IAlignmentTable {

  private final VariantGraph                                 graph;
  private final Map<CollateXVertex, IColumn>                 vertexToColumn;
  private DAVariantGraph dag;

  public DirectedAcyclicGraphBasedAlignmentTable(VariantGraph graph) {
    this.graph = graph;
    vertexToColumn = Maps.newHashMap();
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

  // for now I am going to do things lazy..
  // we will see where the actual init goes..
  public IRow getRow(IWitness witness) {
    if (!this.getSigli().contains(witness.getSigil())) {
      lazyConstructColumns(witness);
    }
    return super.getRow(witness.getSigil());
  }

  // NOTE: Ignore possible cycles in VariantGraph for now!
  private void lazyConstructColumns(IWitness witness) {
    if (isEmpty()) {
      String sigil = witness.getSigil();
      getSigli().add(sigil);

      // build new DAG here!
      DAGBuilder builder = new DAGBuilder();
      dag = builder.buildDAG(graph);

      // NOTE: we build a column for each vertex in the longest path
      List<CollateXVertex> longestPath = dag.getLongestPath();
      for (CollateXVertex vertex : longestPath) {
        IColumn newColumn = addNewColumn(vertex);
        vertexToColumn.put(vertex, newColumn);
      }
    } 
    else {
      // duplicated with above!
      String sigil = witness.getSigil();
      getSigli().add(sigil);
  }
    /*
       Iterator<CollateXVertex> iterator = dag.iterator();
      CollateXVertex startNode = iterator.next();
      // TODO: hier pad voor witness zoeken of vertices filteren
      while (iterator.hasNext()) {
        CollateXVertex vertex = iterator.next();
        // TODO:THE FOLLOWING STATEMENT IS NOT ALWAYS POSSIBLE!
        Column3 column3 = vertexToColumn.get(vertex);
        INormalizedToken token = vertex.getToken(witness);
        column3.addMatch(token);
      }
      
    }
*/
    // Set<CollateXEdge> outgoingEdgesOf = dag.outgoingEdgesOf(startNode);
    // // fill it with first witness!
    // List<IVariantGraphArc> arcs = graph.getArcsForWitness(witness);
    // for (IVariantGraphArc arc: arcs) {
    // INormalizedToken token = arc.getToken(witness);
    // addNewColumn(token);
    // } }

    // List<IVariantGraphArc> arcs = graph.getArcsForWitness(witness);
    // for (IVariantGraphArc arc: arcs) {
    // INormalizedToken token = arc.getToken(witness);
    // addNewColumn(token);
    // }

  }

  private IColumn addNewColumn(CollateXVertex vertex) {
    final IColumn column = new AVGColumn(vertex);
    columns.add(column);
    return column;
  }

}
