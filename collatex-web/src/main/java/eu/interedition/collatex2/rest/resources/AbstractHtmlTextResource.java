package eu.interedition.collatex2.rest.resources;

import org.restlet.data.MediaType;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;

import eu.interedition.collatex2.implementation.parallel_segmentation.ParallelSegmentationTable;
import eu.interedition.collatex2.implementation.parallel_segmentation.SegmentColumn;

public abstract class AbstractHtmlTextResource extends ServerResource {

  public AbstractHtmlTextResource() {
    getVariants().add(new Variant(MediaType.TEXT_HTML));
    getVariants().add(new Variant(MediaType.TEXT_PLAIN));
  }

  static String renderParallelSegmentationTable(ParallelSegmentationTable table) {
    final StringBuilder tableHTML = new StringBuilder("<div id=\"alignment-table\"><h4>Alignment Table:</h4>\n<table border=\"1\" class=\"alignment\">\n");

    for (final String witnessId : table.getSigli()) {
      tableHTML.append("<tr>").append("<th>Witness ").append(witnessId).append(":</th>");
      for (final SegmentColumn column : table.getColumns()) {
        tableHTML.append("<td nowrap>");
        if (column.containsWitness(witnessId)) {
          tableHTML.append(column.getPhrase(witnessId).getContent()); // TODO
                                                                      // add
                                                                      // escaping!
        }
        tableHTML.append("</td>");
      }
      tableHTML.append("</tr>\n");
    }
    tableHTML.append("</table>\n</div><br/><br/>");
    return tableHTML.toString();

  }
}
