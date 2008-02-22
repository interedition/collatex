package com.sd_editions.collatex.Collate;

import java.util.List;

import com.sd_editions.collatex.Block.Block;
import com.sd_editions.collatex.Block.IntBlockVisitor;
import com.sd_editions.collatex.Block.Line;
import com.sd_editions.collatex.Block.Word;

public class Table extends Block {
  private final Line base;
  private final Cell[][] cells;

  public Table(Line base) {
    this.base = base;
    this.cells = new Cell[10][size() + 1];
    for (int i = 1; i <= base.size(); i++) {
      cells[0][i * 2] = new BaseWord(base.get(i));
    }
  }

  private int size() {
    return base.size() * 2 + 1;
  }

  @Override
  public void accept(IntBlockVisitor visitor) {}

  public Cell get(int witness, int column) {
    Cell cell = cells[witness][column];
    if (cell == null)
      return Empty.getInstance();
    return cell;
  }

  public void setOmission(int witness, int baseIndexStart, int baseIndexEnd) {
    for (int i = baseIndexStart; i < baseIndexEnd; i++) {
      Cell omission = new Omission(base.get(i));
      addAlignmentInformationToResult(witness, i, 2, omission);
    }
  }

  public void setFrontAddition(int witness, int baseIndex, List<Word> witnessWords) {
    Cell addition = new Addition(witnessWords);
    addAlignmentInformationToResult(witness, baseIndex, 1, addition);
  }

  public void setBackAddition(int witness, int baseIndex, List<Word> witnessWords) {
    Cell addition = new Addition(witnessWords);
    addAlignmentInformationToResult(witness, baseIndex, 3, addition);
  }

  public void setReplacement(int witness, int baseIndex, List<Word> replacementWords) {
    Cell replacement = new Replacement(base.get(baseIndex), replacementWords);
    addAlignmentInformationToResult(witness, baseIndex, 2, replacement);
  }

  public void setIdenticalOrVariant(int witness, int baseIndex, Word witnessWord) {
    Word baseWord = base.get(baseIndex);
    Cell alignment;
    if (baseWord.alignmentFactor(witnessWord) == 0) {
      alignment = new AlignmentIdentical(baseWord, witnessWord);
    } else {
      alignment = new AlignmentVariant(baseWord, witnessWord);
    }
    addAlignmentInformationToResult(witness, baseIndex, 2, alignment);
  }

  private void addAlignmentInformationToResult(int witness, int baseIndex, int offset, Cell alignment) {
    int column = baseIndex * 2 - 2;
    setCell(witness, column + offset, alignment);
  }

  public void setCell(int witness, int column, Cell alignment) {
    cells[witness][column] = alignment;
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
