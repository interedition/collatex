package eu.interedition.collatex.alignment.multiple_witness.segments;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.alignment.functions.Matcher;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.Word;

public class AlignmentTable3 {

  private final List<Segment> _witnesses;
  private final List<SegmentColumn> _columns;

  public static AlignmentTable3 create(WitnessSet set) {
    AlignmentTable3 table = new AlignmentTable3();
    for (Segment witness : set.getWitnesses()) {
      table.addWitness(witness);
    }
    return table;
  }

  @Override
  public String toString() {
    String collectedStrings = "";
    for (Segment witness : _witnesses) {
      collectedStrings += witness.id + ": ";
      String delim = "";
      for (SegmentColumn column : _columns) {
        collectedStrings += delim + cellToString(witness, column);
        delim = "|";
      }
      collectedStrings += "\n";
    }
    return collectedStrings;
  }

  // TODO: could be made protected!
  public AlignmentTable3() {
    this._witnesses = Lists.newArrayList();
    this._columns = Lists.newArrayList();
  }

  // TODO: could make this protected?
  public void addWitness(Segment witness) {
    if (_witnesses.isEmpty()) {
      List<Word> words = witness.getWords();
      OldSegment segment = new OldSegment(words);
      addColumn(new SegmentColumn(segment));
      _witnesses.add(witness);
      return;
    }

    // make the superbase from the alignment table
    SegmentSuperbase superbase = SegmentSuperbase.create(this);
    Alignment alignment = Matcher.align(superbase, witness);

    addMatchesToAlignmentTable(superbase, alignment);
    _witnesses.add(witness);

    // TODO add stuff for alignment!

  }

  private void addMatchesToAlignmentTable(SegmentSuperbase superbase, Alignment alignment) {
    List<MatchSequence> matchSequencesOrderedForWitnessA = alignment.getMatchSequencesOrderedForWitnessA();
    for (MatchSequence seq : matchSequencesOrderedForWitnessA) {
      SegmentColumn segmentColumn = superbase.getColumnFor(seq.getFirstMatch().getBaseWord());
      // TODO: directly implement size() on column?
      if (segmentColumn.getSegment().getWords().size() == (seq.getMatches().size())) {
        // we have complete alignment; remember there could be added words!
        segmentColumn.addMatchSequenceToColumn(seq);
      } else {
        throw new RuntimeException("Incomplete match! we have to split up Segments, and add Columns!");
      }
    }
    //    Set<Match> matches = compresult.getMatches();
    //    for (Match match : matches) {
    //      Column column = superbase.getColumnFor(match);
    //      Word witnessWord = match.getWitnessWord();
    //      column.addMatch(witnessWord);
    //    }

    // TODO Auto-generated method stub

  }

  // TODO: could be made protected!
  public List<SegmentColumn> getColumns() {
    return _columns;
  }

  // TODO: add stuff!
  private String cellToString(Segment witness, SegmentColumn column) {
    return column.toString();
  }

  private void addColumn(SegmentColumn segmentColumn) {
    _columns.add(segmentColumn);
  }

  public static String alignmentTableToHTML(AlignmentTable3 alignmentTable) {
    StringBuilder tableHTML = new StringBuilder("<div id=\"alignment-table\"><h4>Alignment Table:</h4>\n<table class=\"alignment\">\n");

    for (Segment witness : alignmentTable.getWitnesses()) {
      tableHTML.append("<tr>");
      tableHTML.append("<th>Witness ").append(witness.id).append(":</th>");
      for (SegmentColumn column : alignmentTable.getColumns()) {
        tableHTML.append("<td>");
        if (column.containsWitness(witness)) {
          tableHTML.append(column.getSegment(witness)); // TODO: add escaping!
        }
        tableHTML.append("</td>");
      }
      tableHTML.append("</tr>\n");
    }
    tableHTML.append("</table>\n</div>\n\n");
    //    return alignmentTable.toString().replaceAll("\n", "<br/>") + "<br/>";
    return tableHTML.toString();
  }

  private List<Segment> getWitnesses() {
    return _witnesses;
  }

}
