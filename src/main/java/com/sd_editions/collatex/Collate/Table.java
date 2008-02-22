package com.sd_editions.collatex.Collate;

import com.sd_editions.collatex.Block.Block;
import com.sd_editions.collatex.Block.IntBlockVisitor;
import com.sd_editions.collatex.Block.Line;

public class Table extends Block {

  private final Cell[][] cells;
  private final int baselinesize;

  public Table(Line base) {
    this.baselinesize = base.size();
    this.cells = new Cell[10][size() + 1];
    for (int i = 1; i <= baselinesize; i++) {
      cells[0][i * 2] = new BaseWord(base.get(i));
    }
  }

  private int size() {
    return baselinesize * 2 + 1;
  }

  @Override
  public void accept(IntBlockVisitor visitor) {}

  public Cell get(int variant, int word) {
    Cell cell = cells[variant][word];
    if (cell == null)
      return Empty.getInstance();
    return cell;
  }

  public void setCell(int variant, int column, Cell alignment) {
    cells[variant][column] = alignment;
  }

  public String toHTML() {
    String alignmentTableHTML = "<table border=\"1\">";
    alignmentTableHTML += "<tr><th>Numbers</th>";
    for (int i = 1; i <= size(); i++) {
      alignmentTableHTML += "<th>";
      alignmentTableHTML += "" + i;
      alignmentTableHTML += "</th>";
    }
    alignmentTableHTML += "</tr>";
    alignmentTableHTML += showRow(0, "Base");
    alignmentTableHTML += showRow(1, "Witness");
    alignmentTableHTML += "</table>";
    return alignmentTableHTML;
  }

  private String showRow(int row, String label) {
    String alignmentTableHTML = "<tr><th>" + label + "</th>";
    for (int i = 1; i <= size(); i++) {
      Cell cell = get(row, i);
      alignmentTableHTML += "<td class=" + cell.getType() + ">";
      alignmentTableHTML += cell.toHTML();
      alignmentTableHTML += "</td>";
    }
    alignmentTableHTML += "</tr>";
    return alignmentTableHTML;
  }

}
