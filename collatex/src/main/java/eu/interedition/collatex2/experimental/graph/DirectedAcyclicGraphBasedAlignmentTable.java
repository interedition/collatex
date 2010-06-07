package eu.interedition.collatex2.experimental.graph;

import java.util.Iterator;
import java.util.List;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

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

  private final VariantGraph graph;

  public DirectedAcyclicGraphBasedAlignmentTable(VariantGraph graph) {
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

  // TODO: create a DAG here from the VariantGraph
  // NOTE: Ignore possible cycles in VariantGraph for now!
  private void lazyConstructColumns(IWitness witness) {
    if (isEmpty()) {
      String sigil = witness.getSigil();
      getSigli().add(sigil);
      // build new DAG here!
      DAGBuilder builder = new DAGBuilder();
      DirectedAcyclicGraph<CollateXVertex, CollateXEdge> dag = builder.buildDAG(graph);
      
      // nu moeten we het langste pad algoritme gaan gebruiken
      // for now we just walk over all the edges and make no selection
      // we need to start at the first vertex..
      // we use an iterator for that.
      
      Iterator<CollateXVertex> iterator = dag.iterator();
      CollateXVertex startNode = iterator.next();
      // hier langste pad zoeken
      while (iterator.hasNext()) {
        CollateXVertex vertex = iterator.next();
        INormalizedToken token = vertex.getToken(witness);
        addNewColumn(token);
      }
      
//      Set<CollateXEdge> outgoingEdgesOf = dag.outgoingEdgesOf(startNode);
//      // fill it with first witness!
//      List<IVariantGraphArc> arcs = graph.getArcsForWitness(witness);
//      for (IVariantGraphArc arc: arcs) {
//        INormalizedToken token = arc.getToken(witness);
//        addNewColumn(token);
//      }    } 
      
      
//      List<IVariantGraphArc> arcs = graph.getArcsForWitness(witness);
//      for (IVariantGraphArc arc: arcs) {
//        INormalizedToken token = arc.getToken(witness);
//        addNewColumn(token);
//      }   
      } 
  }
  
  private void addNewColumn(INormalizedToken token) {
    columns.add(new Column3(token, -1));
  }




}
