package eu.interedition.text.util;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SQL {
  public static String select(String tableName, String... columns) {
    final StringBuilder select = new StringBuilder();
    for (int cc = 0; cc < columns.length; cc++) {
      final String column = columns[cc];
      select.append(cc == 0 ? "" : ", ");
      select.append(tableName).append(".").append(column).append(" as ").append(tableName).append("_").append(column);
    }
    return select.toString();
  }
}
