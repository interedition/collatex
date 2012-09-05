package eu.interedition.collatex.dekker.matrix;

import java.util.ArrayList;

import eu.interedition.collatex.Token;

/**
 * 
 * @author Meindert Kroese
 * @author Ronald Haentjens Dekker
 *
 */
//TODO: The methods in this class are extracted from the old MatchMatrix class
//TODO: check correctness
public class MatchTableSerializer {
  //TODO: rename
  private final MatchTable sparseMatrix;
  
  public MatchTableSerializer(MatchTable table) {
    this.sparseMatrix = table;
  }
  
  public String toHtml() {
    StringBuilder result = new StringBuilder("<table>\n<tr><td></td>\n");
    ArrayList<String> colLabels = columnLabels();
    for (String cLabel : colLabels) {
      result.append("<td>").append(cLabel).append("</td>");
    }
    result.append("</tr>\n");
    int colNum = sparseMatrix.columnList().size();
    ArrayList<String> rLabels = rowLabels();
    int row = 0;
    for (String label : rLabels) {
      result.append("<tr><td>").append(label).append("</td>");
      for (int col = 0; col < colNum; col++)
        if (sparseMatrix.vertexAt(row, col)!=null)
          result.append("<td BGCOLOR=\"lightgreen\">M</td>");
        else
          result.append("<td></td>");
      result.append("</tr>\n");
      row++;
    }
    result.append("</table>");
    return result.toString();
  }

  // arch = preferred matches
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

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    ArrayList<String> colLabels = columnLabels();
    for (String cLabel : colLabels) {
      result.append(" ").append(cLabel);
    }
    result.append("\n");
    int colNum = sparseMatrix.columnList().size();
    ArrayList<String> rLabels = rowLabels();
    int row = 0;
    for (String label : rLabels) {
      result.append(label);
      for (int col = 0; col < colNum; col++)
        result.append(" ").append(sparseMatrix.vertexAt(row++, col)!=null);
      result.append("\n");
    }
    return result.toString();
  }

  public ArrayList<String> rowLabels() {
    ArrayList<String> labels = new ArrayList<String>();
    for (Token vgv : sparseMatrix.rowList()) {
      String token = vgv.toString();
      int pos = token.indexOf(":'");
      if (pos > -1) {
        labels.add(token.substring(pos + 2, token.length() - 2));
      }
    }
    return labels;
  }

  public ArrayList<String> columnLabels() {
    ArrayList<String> labels = new ArrayList<String>();
    for (Integer t : sparseMatrix.columnList()) {
      String token = t.toString();
      int pos = token.indexOf(":'");
      if (pos > -1) {
        //        LOG.debug("token={{}}, pos={}", token, pos);
        labels.add(token.substring(pos + 2, token.length() - 1));
      }
    }
    return labels;
  }

  public int rowNum() {
    return rowLabels().size();
  }

  public int colNum() {
    return columnLabels().size();
  }
}
