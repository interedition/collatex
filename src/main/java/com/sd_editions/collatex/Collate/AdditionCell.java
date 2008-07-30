package com.sd_editions.collatex.Collate;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sd_editions.collatex.Block.Word;

public class AdditionCell extends Cell {

  private final List<Word> additions;

  public AdditionCell(List<Word> additions) {
    this.additions = additions;
  }

  public AdditionCell(Word witnessWord) {
    this(Collections.singletonList(witnessWord));
  }

  @Override
  public String toString() {
    return "addition: " + additionsToString();
  }

  private String additionsToString() {
    String additionsAsString = "";
    for (Iterator iterator = additions.iterator(); iterator.hasNext();) {
      Word addition = (Word) iterator.next();
      additionsAsString += addition.getContent();
      if (iterator.hasNext()) {
        additionsAsString += " ";
      }
    }
    return additionsAsString;
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
