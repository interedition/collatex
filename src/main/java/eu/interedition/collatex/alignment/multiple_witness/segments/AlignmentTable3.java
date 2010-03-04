package eu.interedition.collatex.alignment.multiple_witness.segments;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.alignment.functions.Aligner;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.Word;

public class AlignmentTable3 {

  private final List<Segment> _witnesses;
  private final List<SegmentColumn> _columns;

  public static AlignmentTable3 create(final WitnessSet set) {
    final AlignmentTable3 table = new AlignmentTable3();
    for (final Witness witness : set.getWitnesses()) {
      table.addWitness(witness.getFirstSegment());
    }
    return table;
  }

  @Override
  public String toString() {
    String collectedStrings = "";
    for (final Segment witness : _witnesses) {
      collectedStrings += witness.id + ": ";
      String delim = "";
      for (final SegmentColumn column : _columns) {
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

  // NOTE: THIS ONLY WORKS FOR WITNESSES
  // WITH one SEGMENT!
  public void addWitness(final Witness witness) {
    addWitness(witness.getFirstSegment());
  }

  // TODO: could make this protected?
  // TODO: rename to addSegment!
  public void addWitness(final Segment witness) {
    if (_witnesses.isEmpty()) {
      final List<Word> words = witness.getWords();
      final OldSegment segment = new OldSegment(words);
      addColumn(new SegmentColumn(segment));
      _witnesses.add(witness);
      return;
    }

    // TODO: remove call to Aligner!
    // make the superbase from the alignment table
    final SegmentSuperbase superbase = SegmentSuperbase.create(this);
    final Alignment alignment = Aligner.align(superbase, witness);

    addMatchesToAlignmentTable(superbase, alignment);
    _witnesses.add(witness);

    // TODO add stuff for alignment!

  }

  private void addMatchesToAlignmentTable(final SegmentSuperbase superbase, final Alignment alignment) {
    final List<MatchSequence> matchSequencesOrderedForWitnessA = alignment.getMatchSequencesOrderedForWitnessA();
    for (final MatchSequence<Word> seq : matchSequencesOrderedForWitnessA) {
      final SegmentColumn segmentColumn = superbase.getColumnFor(seq.getFirstMatch().getBaseWord());
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
  private String cellToString(final Segment witness, final SegmentColumn column) {
    return column.toString();
  }

  private void addColumn(final SegmentColumn segmentColumn) {
    _columns.add(segmentColumn);
  }

  public static String alignmentTableToHTML(final AlignmentTable3 alignmentTable) {
    final StringBuilder tableHTML = new StringBuilder("<div id=\"alignment-table\"><h4>Alignment Table:</h4>\n<table class=\"alignment\">\n");

    for (final Segment witness : alignmentTable.getWitnesses()) {
      tableHTML.append("<tr>");
      tableHTML.append("<th>Witness ").append(witness.id).append(":</th>");
      for (final SegmentColumn column : alignmentTable.getColumns()) {
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
