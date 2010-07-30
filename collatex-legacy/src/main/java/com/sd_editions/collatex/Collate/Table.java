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
    if (cell == null) {
      return Empty.getInstance();
    }
    return cell;
  }

  public void setOmission(int witness, int baseIndexStart, int baseIndexEnd) {
    for (int i = baseIndexStart; i < baseIndexEnd; i++) {
      Cell omission = new Omission(base.get(i));
      addAlignmentInformationToResult(witness, i, 2, omission);
    }
  }

  public void setFrontAddition(int witness, int baseIndex, List<Word> witnessWords) {
    Cell addition = new AdditionCell(witnessWords);
    addAlignmentInformationToResult(witness, baseIndex, 1, addition);
  }

  public void setBackAddition(int witness, int baseIndex, List<Word> witnessWords) {
    Cell addition = new AdditionCell(witnessWords);
    addAlignmentInformationToResult(witness, baseIndex, 3, addition);
  }

  public void setReplacement(int witness, int baseIndex, List<Word> replacementWords) {
    Cell replacement;
    // check if joining words in the witness produces a match
    Word baseWord = base.get(baseIndex);
    int joinStartIndex = 0;
    int joinEndIndex = 0;
    Word joinedWord = null;
    boolean joinFound = false;
    System.out.println(replacementWords.size());
    System.out.println(replacementWords);
    for (int start = 0; start < replacementWords.size() - 1; start++) {
      for (int end = 1; end < replacementWords.size(); end++) {
        StringBuilder joined = new StringBuilder();
        for (int i = start; i <= end; i++) {
          joined.append(replacementWords.get(i).getContent());
        }
        System.out.println(joined);
        joinedWord = new Word(joined.toString());
        if (baseWord.alignsWith(joinedWord)) {
          joinFound = true;
          joinStartIndex = start;
          joinEndIndex = end;
          start = replacementWords.size();
          end = start;
        }
      }
    }
    if (joinFound) {
      System.out.println(joinStartIndex + "," + joinEndIndex);
      if (joinStartIndex > 0) {
        addAlignmentInformationToResult(witness, baseIndex, 1, new AdditionCell(replacementWords.subList(0, joinStartIndex - 1)));
      }
      addAlignmentInformationToResult(witness, baseIndex, 2, new Division(base.get(baseIndex), replacementWords.subList(joinStartIndex, joinEndIndex + 1)));
      if (joinEndIndex < replacementWords.size()) {
        addAlignmentInformationToResult(witness, baseIndex, 3, new AdditionCell(replacementWords.subList(joinEndIndex + 1, replacementWords.size())));
      }
    } else {
      replacement = new ReplacementCell(base.get(baseIndex), replacementWords);
      addAlignmentInformationToResult(witness, baseIndex, 2, replacement);
    }
  }

  public void setIdenticalOrVariant(int witness, int baseIndex, Word witnessWord, Tuple tup) {
    Word baseWord = base.get(baseIndex);
    Cell alignment;
    if (baseWord.alignmentFactor(witnessWord) == 0) {
      if (tup.isTransposTupel()) {
        alignment = new Transposition(witnessWord);
      } else {
        alignment = new AlignmentIdentical(baseWord, witnessWord);
      }
    } else {
      if (tup.isTransposTupel()) {
        alignment = new Transposition(witnessWord);
      } else {
        alignment = new AlignmentVariant(baseWord, witnessWord);
      }
    }
    addAlignmentInformationToResult(witness, baseIndex, 2, alignment);
  }

  private void addAlignmentInformationToResult(int witness, int baseIndex, int offset, Cell alignment) {
    int column = baseIndex * 2 - 2;
    setCell(witness, column + offset, alignment);
  }

  public void removeAlignmentInformationFromResult(int witness, int baseIndex, int offset) {
    int column = baseIndex * 2 - 2;
    cells[witness][column + offset] = null;
  }

  public void setCell(int witness, int column, Cell alignment) {
    cells[witness][column] = alignment;
    //System.out.println("setCell(): " + alignment.toString() + "-->" + "[" + witness + "]" + "[" + column + "]");
  }

  public String toHTML() {
    StringBuilder alignmentTableHTML = new StringBuilder("<table border=\"1\">").//
        append("<tr><th>Numbers</th>");
    for (int i = 1; i <= size(); i++) {
      alignmentTableHTML.append("<th>").append(i).append("</th>");
    }
    alignmentTableHTML.append("</tr>").//
        append(showRow(0, "Base")).//
        append(showRow(1, "Witness 1")).//
        append(showRow(2, "Witness 2")).//
        append("</table>");
    return alignmentTableHTML.toString();
  }

  private String showRow(int row, String label) {
    StringBuilder alignmentTableHTML = new StringBuilder("<tr><th>").append(label).append("</th>");
    for (int i = 1; i <= size(); i++) {
      Cell cell = get(row, i);
      alignmentTableHTML.append("<td class=").append(cell.getType()).append(">").//
          append(cell.toHTML()).//
          append("</td>");
    }
    alignmentTableHTML.append("</tr>");
    return alignmentTableHTML.toString();
  }

}
