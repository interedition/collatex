package eu.interedition.collatex.alignment.multiple_witness;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.multiple_witness.visitors.IAlignmentTableVisitor;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.parallel_segmentation.AlignmentTableSegmentator;
import eu.interedition.collatex.parallel_segmentation.TeiParallelSegmentationTable;

// Note: for the TEI XML output it is easier to
// have a Column be a list<phrase>
// However, for building the alignment table it
// is easier to have a Column be a list<word>

// NOTE: instead of comparing each of the witnesses with
// each other.. the solution chosen here is based on a
// superbase. So that every witness is compared against
// the super base which is constructed after each compare

public class AlignmentTable2 {
  private final List<Column> columns;
  private final List<Witness> witnesses;

  public AlignmentTable2() {
    this.columns = Lists.newArrayList();
    this.witnesses = Lists.newArrayList();
  }

  // Note: should this be public?
  public void add(Column column) {
    columns.add(column);
  }

  public void addVariantBefore(Column column, List<Word> witnessWords) {
    int indexOf = columns.indexOf(column);
    if (indexOf == -1) {
      throw new RuntimeException("Unexpected error: Column not found!");
    }

    for (Word word : witnessWords) {
      Column extraColumn = new Column(word);
      columns.add(indexOf, extraColumn);
      indexOf++;
    }
  }

  public void addVariantAtTheEnd(List<Word> witnessWords) {
    for (Word word : witnessWords) {
      Column extraColumn = new Column(word);
      columns.add(extraColumn);
    }
  }

  public Superbase createSuperbase() {
    Superbase superbase = new Superbase();
    for (Column column : columns) {
      column.addToSuperbase(superbase);
    }
    return superbase;
  }

  public List<Column> getColumns() {
    return columns;
  }

  public List<Witness> getWitnesses() {
    return witnesses;
  }

  // TODO: move this to a visitor!
  // TODO: separate in two steps: segmentation and xml rendering
  public String toXML() {
    TeiParallelSegmentationTable app = AlignmentTableSegmentator.createTeiParrallelSegmentationTable(this);
    return app.toXML();
  }

  @Override
  public String toString() {
    String collectedStrings = "";
    for (Witness witness : witnesses) {
      collectedStrings += witness.id + ": ";
      String delim = "";
      for (Column column : columns) {
        collectedStrings += delim + cellToString(witness, column);
        delim = "|";
      }
      collectedStrings += "\n";
    }
    return collectedStrings;
  }

  private String cellToString(Witness witness, Column column) {
    if (!column.containsWitness(witness)) {
      return " ";
    }
    return column.getWord(witness).toString();
  }

  // TODO: is this check still necessary?
  // TODO: I dont think one witness is ever
  // TODO: added twice to the table!
  // TODO: rename to add witness?
  void addWitnessToInternalList(Witness witness) {
    // TODO: an ordered set instead of list would be nice here
    if (!witnesses.contains(witness)) {
      witnesses.add(witness);
    }
  }

  // TODO: add visitor who walks over the witnesses
  // Note: this is a visitor who walks over the columns!
  public void accept(IAlignmentTableVisitor visitor) {
    visitor.visitTable(this);
    for (Column column : columns) {
      column.accept(visitor);
    }
    visitor.postVisitTable(this);
  }

  // TODO: move this functionality to a visitor!
  public static String alignmentTableToHTML(AlignmentTable2 alignmentTable) {
    StringBuilder tableHTML = new StringBuilder("<div id=\"alignment-table\"><h4>Alignment Table:</h4>\n<table class=\"alignment\">\n");

    for (Witness witness : alignmentTable.getWitnesses()) {
      tableHTML.append("<tr>");
      tableHTML.append("<th>Witness ").append(witness.id).append(":</th>");
      for (Column column : alignmentTable.getColumns()) {
        tableHTML.append("<td>");
        if (column.containsWitness(witness)) {
          tableHTML.append(column.getWord(witness).normalized); // TODO: add escaping!
        }
        tableHTML.append("</td>");
      }
      tableHTML.append("</tr>\n");
    }
    tableHTML.append("</table>\n</div>\n\n");
    //    return alignmentTable.toString().replaceAll("\n", "<br/>") + "<br/>";
    return tableHTML.toString();
  }
}
