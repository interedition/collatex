package com.sd_editions.collatex.match.views;


import eu.interedition.collatex.experimental.ngrams.NGram;
import eu.interedition.collatex.experimental.ngrams.alignment.Addition;
import eu.interedition.collatex.experimental.ngrams.alignment.Omission;
import eu.interedition.collatex.experimental.ngrams.alignment.Replacement;
import eu.interedition.collatex.experimental.ngrams.table.AlignmentTable;

public class ModificationVisitor {

  private final AlignmentTable table;

  public ModificationVisitor(final AlignmentTable table) {
    this.table = table;
  }

  public void visitAddition(final Addition addition) {
    final NGram addedWords = addition.getAddedWords();
    final AppElement app = new AppElement(addedWords);
    table.setApp(addition.getPosition() * 2 - 2, app);
  }

  public void visitOmission(final Omission omission) {
    final NGram omittedWords = omission.getOmittedWords();
    final AppElement app = new AppElement(omittedWords);
    table.setApp(omission.getPosition() * 2 - 1, app);
  }

  public void visitReplacement(final Replacement replacement) {
    final NGram lemma = replacement.getOriginalWords();
    final NGram reading = replacement.getReplacementWords();
    final AppElement app = new AppElement(lemma, reading);
    table.setApp(replacement.getPosition() * 2 - 1, app);
  }

}
