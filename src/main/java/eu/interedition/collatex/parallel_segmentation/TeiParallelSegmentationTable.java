package eu.interedition.collatex.parallel_segmentation;

import java.util.List;

public class TeiParallelSegmentationTable {

  private final List<SegmentColumn2> _columns;

  public TeiParallelSegmentationTable(final List<SegmentColumn2> columns) {
    this._columns = columns;
  }

  public String toXML() {
    final StringBuilder result = new StringBuilder(); // FIXME initialize length
    result.append("<collation>");
    result.append("<seg>");
    String delimiter = "";
    for (final SegmentColumn2 column : _columns) {
      if (column != null) {
        result.append(delimiter); // FIXME can we just introduce whitespace here!?
        result.append(column.toXML());
        delimiter = " ";
      }
    }
    result.append("</seg>");
    result.append("</collation>");

    return result.toString();
  }
}
