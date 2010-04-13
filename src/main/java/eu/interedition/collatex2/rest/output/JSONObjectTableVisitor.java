package eu.interedition.collatex2.rest.output;

import java.util.List;

import net.sf.json.JSONObject;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableVisitor;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class JSONObjectTableVisitor implements IAlignmentTableVisitor {

  private final JSONObject jsonObject;
  private List<JSONObject> _columns;

  public JSONObjectTableVisitor() {
    this.jsonObject = new JSONObject();
  }

  @Override
  public void visitColumn(final IColumn column) {
    _columns.add(new JSONObject());
  }

  public void visitToken(final String sigel, final INormalizedToken token) {
    final JSONObject jsonObject2 = _columns.get(_columns.size() - 1);
    // Note: this code is duplicated from the other JSON visitor!!
    final JSONObject w1 = new JSONObject();
    w1.put("token", token.getContent());
    ////
    jsonObject2.put(sigel, w1);
  }

  @Override
  public void visitTable(final IAlignmentTable table) {
    // here I want to walk over all the columns
    // but since the table already does that I have
    // to collect them in a post visit method

    _columns = Lists.newArrayList();
  }

  public void postVisitTable(final IAlignmentTable table) {
    jsonObject.put("columns", _columns);
  }

  public JSONObject getJSONObject() {
    return jsonObject;
  }

}
