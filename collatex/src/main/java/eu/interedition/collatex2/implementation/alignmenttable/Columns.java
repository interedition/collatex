package eu.interedition.collatex2.implementation.alignmenttable;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;

public class Columns implements IColumns {
  private final List<IColumn> columns;

  public Columns(final List<IColumn> columns1) {
    this.columns = columns1;
  }

  public Columns() {
    this.columns = Lists.newArrayList();
  }

  //NOTE: for now we assume that phraseA is longer than phraseB!
  //NOTE: this method is only for matches!
  public void addMatchPhrase(final IPhrase phraseB) {
    if (phraseB.size() > columns.size()) {
      // System.out.println(columns.size());
      // System.out.println(phraseB.size());
      throw new RuntimeException("The phrase to be placed in the table is longer than columns!");
    }
    final List<INormalizedToken> tokens = phraseB.getTokens();
    for (int i = 0; i < phraseB.size(); i++) {
      final IColumn column = columns.get(i);
      final INormalizedToken token = tokens.get(i);
      column.addMatch(token);
    }
  }

  //NOTE: for now we assume that phraseA is longer than phraseB!
  //NOTE: this method is only for variants!
  public void addVariantPhrase(final IPhrase phraseB) {
    if (phraseB.size() > columns.size()) {
      // System.out.println("!!" + toString() + ":" + phraseB.toString());
      // System.out.println(columns.size());
      // System.out.println(phraseB.size());
      throw new RuntimeException("The phrase to be placed in the table is longer than columns!");
    }
    final List<INormalizedToken> tokens = phraseB.getTokens();
    for (int i = 0; i < phraseB.size(); i++) {
      final IColumn column = columns.get(i);
      final INormalizedToken token = tokens.get(i);
      column.addVariant(token);
    }
  }

  @Override
  public int getBeginPosition() {
    return getFirstColumn().getPosition();
  }

  @Override
  public int getEndPosition() {
    return getLastColumn().getPosition();
  }

  @Override
  public IColumn getFirstColumn() {
    if (isEmpty()) {
      throw new RuntimeException("Columns are empty!");
    }
    return columns.get(0);
  }

  public IColumn getLastColumn() {
    if (isEmpty()) {
      throw new RuntimeException("Columns are empty!");
    }
    return columns.get(columns.size() - 1);
  }

  @Override
  public boolean isEmpty() {
    return columns.isEmpty();
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    String splitter="";
    for (IColumn column : columns) {
      buffer.append(splitter);
      buffer.append(column.toString());
      splitter = " ";
    }
    return buffer.toString();
  }
//    if (isEmpty()) {
//      return "Columns: EMPTY";
//    }
//    return "Columns: " + getBeginPosition() + "-" + getEndPosition();
//  }

  @Override
  public int size() {
    return columns.size();
  }

  @Override
  public List<IColumn> getColumns() {
    return columns;
  }

}
