package eu.interedition.collatex.alignment.multiple_witness.segments;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.Word;

public class AlignmentTable3 {

  private final List<Witness> _witnesses;
  private final List<SegmentColumn> _columns;

  public static AlignmentTable3 create(WitnessSet set) {
    AlignmentTable3 table = new AlignmentTable3();
    for (Witness witness : set.getWitnesses()) {
      table.addWitness(witness);
    }
    return table;
  }

  @Override
  public String toString() {
    String collectedStrings = "";
    for (Witness witness : _witnesses) {
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
  public void addWitness(Witness witness) {
    if (_witnesses.isEmpty()) {
      List<Word> words = witness.getWords();
      Segment segment = new Segment(words);
      addColumn(new SegmentColumn(segment));
      _witnesses.add(witness);
      return;
    }

    // TODO add stuff for alignment!
    List<Word> words = witness.getWords();
    Segment segment = new Segment(words);
    addColumn(new SegmentColumn(segment));
    _witnesses.add(witness);

  }

  // TODO: could be made protected!
  public List<SegmentColumn> getColumns() {
    return _columns;
  }

  // TODO: add stuff!
  private String cellToString(Witness witness, SegmentColumn column) {
    return column.toString();
  }

  private void addColumn(SegmentColumn segmentColumn) {
    _columns.add(segmentColumn);
  }

}
