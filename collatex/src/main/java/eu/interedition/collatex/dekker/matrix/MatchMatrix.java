package eu.interedition.collatex.dekker.matrix;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.matching.Matches;

@Deprecated
public class MatchMatrix {
  static Logger LOG = LoggerFactory.getLogger(MatchMatrix.class);
  private final ArrayTable<VariantGraphVertex, Token, Boolean> sparseMatrix;

  public static MatchMatrix create(VariantGraph base, Iterable<Token> witness, Comparator<Token> comparator) {
    //    base.rank()/*.adjustRanksForTranspositions()*/;
    Map<VariantGraphVertex, Integer> vertexIndex = Maps.newLinkedHashMap();
    Iterable<VariantGraphVertex> baseVertices = base.vertices();
    int index = 0;
    for (VariantGraphVertex baseVertex : baseVertices) {
      vertexIndex.put(baseVertex, index++);
    }
    Matches matches = Matches.between(baseVertices, witness, comparator);
    MatchMatrix arrayTable = new MatchMatrix(baseVertices, witness);
    Set<Token> unique = matches.getUnique();
    Set<Token> ambiguous = matches.getAmbiguous();
    int column = 0;
    for (Token t : witness) {
      List<VariantGraphVertex> matchingVertices = matches.getAll().get(t);
      if (unique.contains(t)) {
        int row = vertexIndex.get(matchingVertices.get(0)) - 1;
        arrayTable.set(row, column, true);
      } else {
        if (ambiguous.contains(t)) {
          for (VariantGraphVertex vgv : matchingVertices) {
            //            int row = vgv.getRank() - 1;
            int row = vertexIndex.get(vgv) - 1;
            arrayTable.set(row, column, true);
          }
        }
      }
      column++;
    }
    return arrayTable;
  }

  public MatchMatrix(Iterable<VariantGraphVertex> vertices, Iterable<Token> witness) {
    sparseMatrix = ArrayTable.create(vertices, witness);
  }

  public Boolean at(int row, int column) {
    return Objects.firstNonNull(sparseMatrix.at(row, column), false);
  }

  public void set(int row, int column, boolean value) {
    sparseMatrix.set(row, column, value);
  }

  public String toHtml() {
    StringBuilder result = new StringBuilder("<table>\n<tr><td></td>\n");
    ArrayList<String> colLabels = columnLabels();
    for (String cLabel : colLabels) {
      result.append("<td>").append(cLabel).append("</td>");
    }
    result.append("</tr>\n");
    int colNum = sparseMatrix.columnKeyList().size();
    ArrayList<String> rLabels = rowLabels();
    int row = 0;
    for (String label : rLabels) {
      result.append("<tr><td>").append(label).append("</td>");
      for (int col = 0; col < colNum; col++)
        if (at(row, col))
          result.append("<td BGCOLOR=\"lightgreen\">M</td>");
        else
          result.append("<td></td>");
      result.append("</tr>\n");
      row++;
    }
    result.append("</table>");
    return result.toString();
  }

  public String toHtml(Archipelago arch) {
    int mat[] = new int[rowNum()];
    for (Island isl : arch.iterator()) {
      for (Coordinate c : isl) {
        mat[c.row] = c.column;
      }
    }
    StringBuilder result = new StringBuilder("<table>\n<tr><td></td>\n");
    ArrayList<String> colLabels = columnLabels();
    for (String cLabel : colLabels) {
      result.append("<td>").append(cLabel).append("</td>");
    }
    result.append("</tr>\n");
    ArrayList<String> rLabels = rowLabels();
    int row = 0;
    for (String label : rLabels) {
      result.append("<tr><td>").append(label).append("</td>");
      if (mat[row] > 0) {
        result.append("<td colspan=\"").append(mat[row]).append("\"></td>").append("<td BGCOLOR=\"lightgreen\">M</td>");
      }
      result.append("</tr>\n");
      row++;
    }
    result.append("</table>");
    return result.toString();
  }

  public ArrayList<String> rowLabels() {
    ArrayList<String> labels = new ArrayList<String>();
    for (VariantGraphVertex vgv : sparseMatrix.rowKeyList()) {
      String token = vgv.toString();
      int pos = token.indexOf(":'");
      if (pos > -1) {
        labels.add(token.substring(pos + 2, token.length() - 2));
      }
    }
    return labels;
  }

  public List<VariantGraphVertex> rowVertices() {
    List<VariantGraphVertex> vertices = Lists.newArrayList();
    for (VariantGraphVertex vgv : sparseMatrix.rowKeyList()) {
      if (vgv.toString().contains(":")) {
        vertices.add(vgv);
      }
    }
    return vertices;
  }

  public ArrayList<String> columnLabels() {
    ArrayList<String> labels = new ArrayList<String>();
    for (Token t : sparseMatrix.columnKeyList()) {
      String token = t.toString();
      int pos = token.indexOf(":'");
      if (pos > -1) {
        //        LOG.info("token={{}}, pos={}", token, pos);
        labels.add(token.substring(pos + 2, token.length() - 1));
      }
    }
    return labels;
  }

  public List<Token> columnTokens() {
    List<Token> tokens = Lists.newArrayList();
    for (Token t : sparseMatrix.columnKeyList()) {
      if (t.toString().contains(":")) {
        tokens.add(t);
      }
    }
    return tokens;
  }

  public ArrayList<Coordinate> allMatches() {
    ArrayList<Coordinate> pairs = new ArrayList<Coordinate>();
    int rows = rowNum();
    int cols = colNum();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        if (at(i, j)) pairs.add(new Coordinate(i, j));
      }
    }
    return pairs;
  }

  public int rowNum() {
    return rowLabels().size();
  }

  public int colNum() {
    return columnLabels().size();
  }

  public ArrayList<Island> getIslands() {
    ArrayList<Island> islands = new ArrayList<Island>();
    ArrayList<Coordinate> allTrue = allMatches();
    for (Coordinate c : allTrue) {
      //			System.out.println("next coordinate: "+c);
      boolean found = false;
      while (!found) {
        for (Island alc : islands) {
          //					System.out.println("inspect island");
          if (alc.neighbour(c)) {
            alc.add(c);
            found = true;
          }
          if (found) break;
        }
        if (!found) {
          //					System.out.println("new island");
          Island island = new Island();
          island.add(c);
          islands.add(island);
        }
        found = true;
      }
    }
    return islands;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    ArrayList<String> colLabels = columnLabels();
    for (String cLabel : colLabels) {
      result.append(" ").append(cLabel);
    }
    result.append("\n");
    int colNum = sparseMatrix.columnKeyList().size();
    ArrayList<String> rLabels = rowLabels();
    int row = 0;
    for (String label : rLabels) {
      result.append(label);
      for (int col = 0; col < colNum; col++)
        result.append(" ").append(at(row++, col));
      result.append("\n");
    }
    return result.toString();
  }

}
