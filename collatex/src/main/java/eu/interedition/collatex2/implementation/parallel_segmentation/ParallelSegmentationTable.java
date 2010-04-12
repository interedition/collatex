package eu.interedition.collatex2.implementation.parallel_segmentation;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ParallelSegmentationTable {

  private final List<SegmentColumn> segmentColumns;

  public ParallelSegmentationTable(final List<SegmentColumn> segmentColumns) {
    this.segmentColumns = segmentColumns;
  }

  private List<SegmentColumn> getColumns() {
    return segmentColumns;
  }

  private List<String> getSigli() {
    final Set<String> sigli = Sets.newLinkedHashSet();
    for (final SegmentColumn column : segmentColumns) {
      sigli.addAll(column.getSigli());
    }
    return Lists.newArrayList(sigli);
  }

  public static String tableToHTML(final ParallelSegmentationTable table) {
    final StringBuilder tableHTML = new StringBuilder("<div id=\"alignment-table\"><h4>Alignment Table:</h4>\n<table border=\"1\" class=\"alignment\">\n");

    for (final String witnessId : table.getSigli()) {
      tableHTML.append("<tr>").append("<th>Witness ").append(witnessId).append(":</th>");
      for (final SegmentColumn column : table.getColumns()) {
        tableHTML.append("<td nowrap>");
        if (column.containsWitness(witnessId)) {
          tableHTML.append(column.getPhrase(witnessId).getContent()); // TODO add escaping!
        }
        tableHTML.append("</td>");
      }
      tableHTML.append("</tr>\n");
    }
    tableHTML.append("</table>\n</div><br/><br/>");
    return tableHTML.toString();
  }

}
