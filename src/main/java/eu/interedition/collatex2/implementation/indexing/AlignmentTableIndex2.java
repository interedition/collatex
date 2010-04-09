package eu.interedition.collatex2.implementation.indexing;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import eu.interedition.collatex2.implementation.alignmenttable.Columns;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableIndex;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class AlignmentTableIndex2 implements IAlignmentTableIndex {
  private final Map<String, IColumns> normalizedToColumns;

  private AlignmentTableIndex2() {
    this.normalizedToColumns = Maps.newHashMap();
  }

  public static IAlignmentTableIndex create(final IAlignmentTable table) {
    final AlignmentTableIndex2 index = new AlignmentTableIndex2();
    final List<String> findRepeatingTokens = findRepeatingTokens(table);
    final String row = "A";
    findUniquePhrasesForRow(table, index, findRepeatingTokens, row);
    return index;
  }

  private static void findUniquePhrasesForRow(final IAlignmentTable table, final AlignmentTableIndex2 index, final List<String> findRepeatingTokens, final String row) {
    // filteren would be nicer.. maar we doen het maar even alles in een!
    for (final IColumn column : table.getColumns()) {
      if (column.containsWitness(row)) {
        final INormalizedToken token = column.getToken(row);
        // kijken of ie unique is
        final boolean unique = !findRepeatingTokens.contains(token.getNormalized());
        if (unique) {
          final IColumns columns = new Columns(Lists.newArrayList(column));
          final ColumnPhrase phrase = new ColumnPhrase(token.getNormalized(), columns, Lists.newArrayList(row));
          index.add(phrase);
        } else {
          //System.out.println("We have to combine stuff here!");
          final ColumnPhrase leftPhrase = findUniqueColumnPhraseToTheLeft(table, findRepeatingTokens, row, column, token);
          final ColumnPhrase rightPhrase = findUniqueColumnPhraseToTheRight(table, findRepeatingTokens, row, column, token);
          index.add(leftPhrase);
          index.add(rightPhrase);
        }
      } else {
        System.out.println("Column " + column.getPosition() + " is empty!");
      }
    }
  }

  //TODO: add support for empty cells!
  private static ColumnPhrase findUniqueColumnPhraseToTheLeft(final IAlignmentTable table, final List<String> findRepeatingTokens, final String row, final IColumn column, final INormalizedToken token) {
    // combine to the left
    final ColumnPhrase phrase = new ColumnPhrase(token.getNormalized(), new Columns(Lists.newArrayList(column)), Lists.newArrayList(row));
    boolean found = false; // not nice!
    for (int i = column.getPosition() - 1; !found && i > 0; i--) {
      final IColumn leftColumn = table.getColumns().get(i - 1);
      final boolean empty = !leftColumn.containsWitness(row);
      //TODO: next statement is not allowed if empty column!
      final String normalizedNeighbour = leftColumn.getToken(row).getNormalized();
      found = !empty && !findRepeatingTokens.contains(normalizedNeighbour);
      phrase.addColumnToLeft(leftColumn);
    }
    if (!found) {
      phrase.addColumnToLeft(new NullColumn(1));
    }
    return phrase;
  }

  //TODO: add support for empty cells!
  private static ColumnPhrase findUniqueColumnPhraseToTheRight(final IAlignmentTable table, final List<String> findRepeatingTokens, final String row, final IColumn column, final INormalizedToken token) {
    final ColumnPhrase phrase = new ColumnPhrase(token.getNormalized(), new Columns(Lists.newArrayList(column)), Lists.newArrayList(row));
    boolean found = false; // not nice!
    for (int i = column.getPosition() + 1; !found && i < table.size() + 1; i++) {
      final IColumn rightColumn = table.getColumns().get(i - 1);
      final boolean empty = !rightColumn.containsWitness(row);
      //TODO: next statement is not allowed if empty column!
      final String normalizedNeighbour = rightColumn.getToken(row).getNormalized();
      found = !empty && !findRepeatingTokens.contains(normalizedNeighbour);
      phrase.addColumnToRight(rightColumn);
    }
    if (!found) {
      phrase.addColumnToRight(new NullColumn(table.size()));
    }
    return phrase;
  }

  private void add(final ColumnPhrase phrase) {
    normalizedToColumns.put(phrase.getNormalized(), phrase.getColumns());
  }

  //TODO: move to IAlignmentTable?
  private static List<String> findRepeatingTokens(final IAlignmentTable table) {
    //transform
    Multimap<String, IColumn> yes;
    yes = Multimaps.newArrayListMultimap();
    final List<IColumn> columns = table.getColumns();
    for (final IColumn column : columns) {
      final List<INormalizedToken> variants = column.getVariants();
      for (final INormalizedToken token : variants) {
        yes.put(token.getNormalized(), column);
      }
    }
    //predicate
    final List<String> repeatingNormalizedTokens = Lists.newArrayList();
    for (final String key : yes.keySet()) {
      final Collection<IColumn> xcolumns = yes.get(key);
      if (xcolumns.size() > 1) {
        //System.out.println("Repeating token: " + key + " in columns " + xcolumns.toString());
        repeatingNormalizedTokens.add(key);
      }
    }
    return repeatingNormalizedTokens;
  }

  @Override
  public boolean containsNormalizedPhrase(final String normalized) {
    return normalizedToColumns.containsKey(normalized);
  }

  //TODO: add test!
  @Override
  public IColumns getColumns(final String normalized) {
    if (!containsNormalizedPhrase(normalized)) {
      throw new RuntimeException("No such element " + normalized + " in AlignmentTableIndex!");
    }
    return normalizedToColumns.get(normalized);
  }

  @Override
  public int size() {
    return normalizedToColumns.size();
  }

}
