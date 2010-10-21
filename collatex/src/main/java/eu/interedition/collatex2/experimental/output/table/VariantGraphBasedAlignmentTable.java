package eu.interedition.collatex2.experimental.output.table;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.interfaces.IAddition;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableVisitor;
import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IInternalColumn;
import eu.interedition.collatex2.interfaces.IReplacement;
import eu.interedition.collatex2.interfaces.IRow;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.ITokenIndex;
import eu.interedition.collatex2.legacy.alignmenttable.BaseAlignmentTable;

public class VariantGraphBasedAlignmentTable extends BaseAlignmentTable implements IAlignmentTable {

  private final IVariantGraph                 graph;
  private final Map<IVariantGraphVertex, IColumn> vertexToColumn;

  public VariantGraphBasedAlignmentTable(IVariantGraph graph) {
    this.graph = graph;
    vertexToColumn = Maps.newHashMap();
    init();
  }

  private void init() {
    if (!graph.isEmpty()) {
      // NOTE: we build a column for each vertex in the longest path
      List<IVariantGraphVertex> longestPath = graph.getLongestPath();
      for (IVariantGraphVertex vertex : longestPath) {
        IColumn newColumn = addNewColumn(vertex);
        vertexToColumn.put(vertex, newColumn);
      }
    }
  }

  @Override
  public void accept(IAlignmentTableVisitor visitor) {
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
  public List<String> getRepeatedTokens() {
    // TODO Auto-generated method stub
    return null;
  }

  // for now I am going to do things lazy..
  // we will see where the actual init goes..
  @Override
  public IRow getRow(IWitness witness) {
    if (!this.getSigla().contains(witness.getSigil())) {
      lazyConstructColumns(witness);
    }
    return super.getRow(witness.getSigil());
  }

  // NOTE: Ignore possible cycles in VariantGraph for now!
  private void lazyConstructColumns(IWitness witness) {
    String sigil = witness.getSigil();
    getSigla().add(sigil);

    // Note: search the path through the graph for this witness
    List<IVariantGraphVertex> path = graph.getPath(witness);
    // NOTE: now assign columns to each vertex that is
    // not on the longest path or that is not yet assigned
    IColumn lastColumn = null;
//    System.out.println(path.size());
//    for (CollateXVertex vertex : path) {
//      System.out.println(vertex.getNormalized());
//    }
    for (IVariantGraphVertex vertex : path) {
//       System.out.println("Looking for: "+vertex.getNormalized());
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

  private IColumn addNewColumn(IVariantGraphVertex vertex) {
    final IColumn column = new VariantGraphBasedColumn(vertex, columns.size() + 1);
    columns.add(column);
    return column;
  }

  @Override
  public ITokenIndex getTokenIndex(List<String> repeatingTokens) {
    throw new RuntimeException("DO NOT INDEX THIS STRUCTURE!");
  }

  @Override
  public void add(IInternalColumn column) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String toString() {
    //NOTE INIT (temp)
    List<IWitness> witnesses = graph.getWitnesses();
    for (IWitness witness : witnesses) {
      lazyConstructColumns(witness);
    }
    //NOTE END INIT (Temp)
    final StringBuilder stringBuilder = new StringBuilder();
    for (final IRow row : getRows()) {
      stringBuilder.append(row.getSigil()).append(": ");
      String delim = "";
      for (final ICell cell : row) {
        stringBuilder.append(delim).append(cellToString(cell));
        delim = "|";
      }
      stringBuilder.append("\n");
    }
    return stringBuilder.toString();
  }

  private String cellToString(final ICell cell) {
    if (cell.isEmpty()) {
      return " ";
    }
    //TODO should not be getnormalized!
    return cell.getToken().getNormalized().toString();
  }

}
