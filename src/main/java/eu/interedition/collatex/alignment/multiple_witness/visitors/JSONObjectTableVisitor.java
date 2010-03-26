package eu.interedition.collatex.alignment.multiple_witness.visitors;

import java.util.List;

import net.sf.json.JSONObject;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.Column;
import eu.interedition.collatex.input.Word;

public class JSONObjectTableVisitor implements IAlignmentTableVisitor<Word> {

  private final JSONObject jsonObject;
  private List<JSONObject> _columns;

  public JSONObjectTableVisitor() {
    this.jsonObject = new JSONObject();
  }

  @Override
  public void visitColumn(final Column column) {
    _columns.add(new JSONObject());
  }

  public void visitElement(final String sigel, final Word word) {
    final JSONObject jsonObject2 = _columns.get(_columns.size() - 1);
    // Note: this code is duplicated from the other JSON visitor!!
    final JSONObject w1 = new JSONObject();
    w1.put("token", word.original);
    ////
    jsonObject2.put(sigel, w1);
  }

  @Override
  public void visitTable(final AlignmentTable2 table) {
    // here I want to walk over all the columns
    // but since the table already does that I have
    // to collect them in a post visit method

    _columns = Lists.newArrayList();
  }

  public void postVisitTable(final AlignmentTable2 table) {
    jsonObject.put("columns", _columns);
  }

  // TODO: extract jsonvisitorinterface!
  public JSONObject getJSONObject() {
    return jsonObject;
  }

}
