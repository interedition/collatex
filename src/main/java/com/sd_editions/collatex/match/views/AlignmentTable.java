package com.sd_editions.collatex.match.views;

import java.util.Set;

import com.sd_editions.collatex.permutations.Match;
import com.sd_editions.collatex.permutations.Modifications;
import com.sd_editions.collatex.permutations.Word;

public class AlignmentTable {

  private final Element[] cells;

  // NOTE: rename to tree? XML is a tree after all!
  // NOTE: rename to elements? A tree has elements after all!
  public AlignmentTable(Modifications modifications) {
    Set<Match> matches = modifications.getMatches();
    cells = new Element[matches.size() * 2];
    for (Match match : matches) {
      Word matchedWord = match.getBaseWord();
      cells[matchedWord.position * 2 - 1] = new TextElement(matchedWord);
    }
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
