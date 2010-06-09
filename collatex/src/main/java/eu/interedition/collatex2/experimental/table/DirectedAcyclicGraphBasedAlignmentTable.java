package eu.interedition.collatex2.experimental.table;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
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

  private final VariantGraph                 graph;
  private final Map<CollateXVertex, IColumn> vertexToColumn;
  private DAVariantGraph                     dag;

  public DirectedAcyclicGraphBasedAlignmentTable(VariantGraph graph) {
    this.graph = graph;
    vertexToColumn = Maps.newHashMap();
    init();
  }

  private void init() {
    // Note: we build the new DAG here (based on the variant graph)
    DAGBuilder builder = new DAGBuilder();
    dag = builder.buildDAG(graph);

    // NOTE: we build a column for each vertex in the longest path
    List<CollateXVertex> longestPath = dag.getLongestPath();
    for (CollateXVertex vertex : longestPath) {
      IColumn newColumn = addNewColumn(vertex);
      vertexToColumn.put(vertex, newColumn);
    }
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
    String sigil = witness.getSigil();
    getSigli().add(sigil);

    // Note: search the path through the graph for this witness
    List<CollateXVertex> path = getPathFor(witness);
    // NOTE: now assign columns to each vertex that is
    // not on the longest path or that is not yet assigned
    IColumn lastColumn = null;
    for (CollateXVertex vertex : path) {
      // System.out.println("Looking for: "+vertex.getNormalized());
      if (vertexToColumn.containsKey(vertex)) {
        // skip... vertex is already placed
        lastColumn = vertexToColumn.get(vertex);
      } else {
        //System.out.println("vertex " + vertex.getNormalized() + " is not yet present in columns!");
        if (lastColumn == null) {
          throw new RuntimeException("LASTCOLUMN == null; THIS IS NOT SUPPOSED TO HAPPEN!");
        }
        int position = lastColumn.getPosition();
        if (position > getColumns().size()) {
          throw new RuntimeException("Not enough columns in table; THIS IS NOT SUPPOSED TO HAPPEN!");
        }
        IColumn nextColumn = getColumns().get(position);
        nextColumn.addVertex(vertex);
        lastColumn = nextColumn;
      }
    }
    // if (isEmpty()) {
    //
    // }
    // else {
    // // duplicated with above!
    // String sigil = witness.getSigil();
    // getSigli().add(sigil);
    // }
    /*
     * Iterator<CollateXVertex> iterator = dag.iterator(); CollateXVertex
     * startNode = iterator.next(); // TODO: hier pad voor witness zoeken of
     * vertices filteren while (iterator.hasNext()) { CollateXVertex vertex =
     * iterator.next(); // TODO:THE FOLLOWING STATEMENT IS NOT ALWAYS POSSIBLE!
     * Column3 column3 = vertexToColumn.get(vertex); INormalizedToken token =
     * vertex.getToken(witness); column3.addMatch(token); }
     * 
     * }
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

  // TODO: move to DAVG
  private List<CollateXVertex> getPathFor(IWitness witness) {
    List<CollateXVertex> path = Lists.newArrayList();
    CollateXVertex startVertex = dag.iterator().next();
    CollateXVertex currentVertex = startVertex;
    while (dag.outDegreeOf(currentVertex) > 0) {
      Set<CollateXEdge> outgoingEdges = dag.outgoingEdgesOf(currentVertex);
      for (CollateXEdge edge : outgoingEdges) {
        CollateXVertex edgeTarget = dag.getEdgeTarget(edge);
        if (edgeTarget.containsWitness(witness.getSigil())) {
          if (!edgeTarget.getNormalized().equals("#")) {
            path.add(edgeTarget);
          }
        }
        currentVertex = edgeTarget;
      }
    }
    return path;
  }

  private IColumn addNewColumn(CollateXVertex vertex) {
    final IColumn column = new AVGColumn(vertex, columns.size() + 1);
    columns.add(column);
    return column;
  }

}
