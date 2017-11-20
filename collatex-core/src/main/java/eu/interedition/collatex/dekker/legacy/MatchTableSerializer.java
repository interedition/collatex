/*
 * Copyright (c) 2015 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.dekker.legacy;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.island.Archipelago;
import eu.interedition.collatex.dekker.island.Coordinate;
import eu.interedition.collatex.dekker.island.Island;

import java.util.ArrayList;

/**
 * @author Meindert Kroese
 * @author Ronald Haentjens Dekker
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
                if (sparseMatrix.vertexAt(row, col) != null)
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
        for (Island isl : arch.getIslands()) {
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
                result.append(" ").append(sparseMatrix.vertexAt(row++, col) != null);
            result.append("\n");
        }
        return result.toString();
    }

    public ArrayList<String> rowLabels() {
        ArrayList<String> labels = new ArrayList<>();
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
        ArrayList<String> labels = new ArrayList<>();
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
