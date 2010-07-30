package com.sd_editions.collatex.Collate;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sd_editions.collatex.Block.Word;

public class AdditionCell extends Cell {

  private final List<Word> additions;

  public AdditionCell(List<Word> additions1) {
    this.additions = additions1;
  }

  public AdditionCell(Word witnessWord) {
    this(Collections.singletonList(witnessWord));
  }

  @Override
  public String toString() {
    return "addition: " + additionsToString();
  }

  private String additionsToString() {
    StringBuilder additionsAsString = new StringBuilder();
    for (Iterator<Word> iterator = additions.iterator(); iterator.hasNext();) {
      Word addition = iterator.next();
      additionsAsString.append(addition.getContent());
      if (iterator.hasNext()) {
        additionsAsString.append(" ");
      }
    }
    return additionsAsString.toString();
  }

  @Override
  public String toHTML() {
    return additionsToString();
  }

  @Override
  public String getType() {
    return "addition";
  }

}
