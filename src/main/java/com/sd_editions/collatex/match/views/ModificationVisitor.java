package com.sd_editions.collatex.match.views;

import com.sd_editions.collatex.permutations.collate.Addition;

public class ModificationVisitor {

  private final AlignmentTable table;

  public ModificationVisitor(AlignmentTable table) {
    this.table = table;
  }

  // TODO: should this be just a String?
  public void visitAddition(Addition addition) {
    String addedWords = addition.getAddedWords();
    AppElement app = new AppElement(addedWords);
    table.setApp(addition.getPosition() * 2 - 2, app);
  }

}
