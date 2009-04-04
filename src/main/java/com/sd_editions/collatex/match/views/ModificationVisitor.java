package com.sd_editions.collatex.match.views;

import com.sd_editions.collatex.permutations.collate.Addition;
import com.sd_editions.collatex.permutations.collate.Omission;

public class ModificationVisitor {

  private final AlignmentTable table;

  public ModificationVisitor(AlignmentTable table) {
    this.table = table;
  }

  // TODO: should addedWords be just a String?
  public void visitAddition(Addition addition) {
    String addedWords = addition.getAddedWords();
    AppElement app = new AppElement(addedWords);
    table.setApp(addition.getPosition() * 2 - 2, app);
  }

  // TODO: should omittedWords be just a String?
  public void visitOmission(Omission omission) {
    String omittedWords = omission.getOmittedWords();
    AppElement app = new AppElement(omittedWords);
    table.setApp(omission.getPosition() * 2 - 1, app);
  }

}
