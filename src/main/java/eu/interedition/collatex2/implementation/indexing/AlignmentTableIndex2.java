package eu.interedition.collatex2.implementation.indexing;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import eu.interedition.collatex2.implementation.alignmenttable.Columns;
import eu.interedition.collatex2.interfaces.IAligmentTableIndex;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class AlignmentTableIndex2 implements IAligmentTableIndex {
  private final Map<String, IColumns> normalizedToColumns;

  private AlignmentTableIndex2() {
    this.normalizedToColumns = Maps.newHashMap();
  }

  public static IAligmentTableIndex create(final IAlignmentTable table) {
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
          index.put(token.getNormalized(), columns);
        } else {
          //System.out.println("We have to combine stuff here!");

          findUniqueTokenToTheLeft(table, index, findRepeatingTokens, row, column, token);
          findUniqueTokenToTheRight(table, index, findRepeatingTokens, row, column, token);
        }

      } else {
        System.out.println("Column " + column.getPosition() + " is empty!");
      }
    }
  }

  //TODO: add support for empty cells!
  private static void findUniqueTokenToTheLeft(final IAlignmentTable table, final AlignmentTableIndex2 index, final List<String> findRepeatingTokens, final String row, final IColumn column,
      final INormalizedToken token) {
    // combine to the left
    String normalized = token.getNormalized();
    final List<IColumn> buffer = Lists.newArrayList();
    boolean found = false; // not nice!
    for (int i = column.getPosition() - 1; i > 0; i--) {
      final IColumn leftColumn = table.getColumns().get(i - 1);
      final boolean empty = !leftColumn.containsWitness(row);
      found = !empty && !findRepeatingTokens.contains(leftColumn.getToken(row).getNormalized());
      if (found) {
        buffer.add(0, leftColumn);
        normalized = leftColumn.getToken(row).getNormalized() + " " + normalized;
        break;
      }
      buffer.add(0, leftColumn);
      normalized = leftColumn.getToken(row).getNormalized() + " " + normalized;
    }
    if (found) {
      index.put(normalized, new Columns(buffer));
    } else {
      buffer.add(0, new NullColumn(1));
      index.put("# " + normalized, new Columns(buffer));
    }
  }

  //TODO: add support for empty cells!
  private static void findUniqueTokenToTheRight(final IAlignmentTable table, final AlignmentTableIndex2 index, final List<String> findRepeatingTokens, final String row, final IColumn column,
      final INormalizedToken token) {
    String normalized = token.getNormalized();
    final List<IColumn> buffer = Lists.newArrayList();
    boolean found = false; // not nice!
    for (int i = column.getPosition() + 1; i < table.size() + 1; i++) {
      final IColumn rightColumn = table.getColumns().get(i - 1);
      final boolean empty = !rightColumn.containsWitness(row);
      found = !empty && !findRepeatingTokens.contains(rightColumn.getToken(row).getNormalized());
      if (found) {
        buffer.add(rightColumn);
        normalized += " " + rightColumn.getToken(row).getNormalized();
        break;
      }
      buffer.add(rightColumn);
      normalized += " " + rightColumn.getToken(row).getNormalized();
    }
    if (found) {
      index.put(normalized, new Columns(buffer));
    } else {
      buffer.add(new NullColumn(table.size()));
      index.put(normalized + "# ", new Columns(buffer));
    }
  }

  private void put(final String normalized, final IColumns columns) {
    // System.out.println("adding " + normalized);
    normalizedToColumns.put(normalized, columns);
  }

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

  @Override
  public IColumns getColumns(final String normalized) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int size() {
    return normalizedToColumns.size();
  }

}
