package eu.interedition.collatex.alignment.multiple_witness.visitors;

import java.util.List;

import net.sf.json.JSONObject;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.Column;
import eu.interedition.collatex.input.Word;

public class JSONObjectTableVisitor implements IAlignmentTableVisitor {

  private final JSONObject jsonObject;
  private List<JSONObject> _columns;

  public JSONObjectTableVisitor() {
    this.jsonObject = new JSONObject();
  }

  @Override
  public void visitColumn(Column column) {
    _columns.add(new JSONObject());
  }

  public void visitWord(String sigel, Word word) {
    JSONObject jsonObject2 = _columns.get(_columns.size() - 1);
    jsonObject2.put(sigel, word.original);
  }

  public void postVisitColumn(Column column) {
  //    JSONObject object = new JSONObject();
  //    object.put("sigli", column.getSigli());
  //    _columns.add(object);
  }

  @Override
  public void visitTable(AlignmentTable2 table) {
    // here I want to walk over all the columns
    // but since the table already does that I have
    // to collect them in a post visit method

    _columns = Lists.newArrayList();
  }

  public void postVisitTable(AlignmentTable2 table) {
    jsonObject.put("columns", _columns);
  }

  // TODO: extract jsonvisitorinterface!
  public JSONObject getJSONObject() {
    return jsonObject;
  }

}
