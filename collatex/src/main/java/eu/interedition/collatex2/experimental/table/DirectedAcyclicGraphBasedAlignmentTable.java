package eu.interedition.collatex2.experimental.table;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.experimental.graph.VariantGraph;
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

public class DirectedAcyclicGraphBasedAlignmentTable extends BaseAlignmentTable implements IAlignmentTable {

  private final VariantGraph                                 graph;
  private final Map<CollateXVertex, Column3>                 vertexToColumn;
  private DirectedAcyclicGraph<CollateXVertex, CollateXEdge> dag;

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

      // nu moeten we het langste pad algoritme gaan gebruiken
      // for now we just walk over all the vertices and make no selection
      // we need to start at the first vertex..
      // we use an iterator for that.
      Iterator<CollateXVertex> iterator = dag.iterator();
      CollateXVertex startNode = iterator.next();
      // hier langste pad zoeken
      while (iterator.hasNext()) {
        CollateXVertex vertex = iterator.next();
        INormalizedToken token = vertex.getToken(witness);
        Column3 newColumn = addNewColumn(token);
        vertexToColumn.put(vertex, newColumn);
      }
    } else {
      // duplicated with above!
      String sigil = witness.getSigil();
      getSigli().add(sigil);

      Iterator<CollateXVertex> iterator = dag.iterator();
      CollateXVertex startNode = iterator.next();
      // hier pad voor witness zoeken of vertices filteren
      while (iterator.hasNext()) {
        CollateXVertex vertex = iterator.next();
        // TODO:THE FOLLOWING STATEMENT IS NOT ALWAYS POSSIBLE!
        Column3 column3 = vertexToColumn.get(vertex);
        INormalizedToken token = vertex.getToken(witness);
        column3.addMatch(token);
      }
    }

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

  private Column3 addNewColumn(INormalizedToken token) {
    final Column3 column = new Column3(token, -1);
    columns.add(column);
    return column;
  }

}
