package com.sd_editions.collatex.match.views;

import java.util.Set;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.visualization.Modifications;

public class AlignmentTable {

  private final Element[] cells;

  // NOTE: rename to tree? XML is a tree after all!
  // NOTE: rename to elements? A tree has elements after all!
  public AlignmentTable(Modifications modifications) {
    // NOTE: move this to Modifications?
    Set<Match> matches = modifications.getMatches();
    cells = new Element[100]; // TODO: take longest witness?
    // NOTE: move this to ModificationVisitor?
    for (Match match : matches) {
      Word matchedWord = match.getBaseWord();
      cells[matchedWord.position * 2 - 1] = new TextElement(matchedWord);
    }
    // Note: move this to Modifications?
    ModificationVisitor modificationVisitor = new ModificationVisitor(this);
    modifications.accept(modificationVisitor);
  }

  // TODO: use a StringBuilder or Buffer instead of +=
  // TODO: should we store the whitespace in a Word?
  public String toXML() {
    String result = "<xml>";
    String whitespace = "";
    for (Element element : cells) {
      if (element != null) {
        result += whitespace + element.toXML();
        whitespace = " ";
      }
    }
    result += "</xml>";
    return result;
  }

  public void setApp(int i, AppElement app) {
    cells[i] = app;
  }
}
