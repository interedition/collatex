package com.sd_editions.collatex.Web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.Collate.Table;
import com.sd_editions.collatex.Collate.Tuple;
import com.sd_editions.collatex.Collate.TupleToTable;
import com.sd_editions.collatex.Collate.WordAlignmentVisitor;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;

public class Homepage extends WebPage {

  public Homepage() {
    ModelForView model = new ModelForView("a big black cat came in", new String[] { "on a tiny black mat", "with a small black cat" });

    add(new Label("base", new PropertyModel(model, "base")));
    add(new Label("witness1", new PropertyModel(model, "witness1")));
    add(new Label("witness2", new PropertyModel(model, "witness2")));

    Label label = new Label("alignment", new PropertyModel(model, "html"));
    label.setEscapeModelStrings(false);
    add(label);
    add(new AlignmentForm("alignmentform", model));
  }

  @SuppressWarnings("serial")
  class ModelForView implements Serializable {
    private String base;
    private String witness1;
    private String witness2;
    private String html;

    public ModelForView(String base, String[] witnesses) {
      this.base = base;
      this.witness1 = witnesses[0];
      this.witness2 = witnesses[1];
      fillAlignmentTable();
    }

    private void fillAlignmentTable() {
      BlockStructure baseStructure;
      BlockStructure witnessStructure1;
      BlockStructure witnessStructure2;
      try {
        baseStructure = new StringInputPlugin(base).readFile();
        witnessStructure1 = new StringInputPlugin(witness1).readFile();
        witnessStructure2 = new StringInputPlugin(witness2).readFile();
      } catch (FileNotFoundException e) { // TODO: work away those exceptions.. they are not relevant for Strings
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (BlockStructureCascadeException e) {
        throw new RuntimeException(e);
      }

      WordAlignmentVisitor visitor1 = new WordAlignmentVisitor(witnessStructure1);
      baseStructure.accept(visitor1);
      Tuple[] result1 = visitor1.getResult();
      WordAlignmentVisitor visitor2 = new WordAlignmentVisitor(witnessStructure2);
      baseStructure.accept(visitor2);
      Tuple[] result2 = visitor2.getResult();
      List<BlockStructure> witnessList = new ArrayList<BlockStructure>();
      witnessList.add(witnessStructure1);
      witnessList.add(witnessStructure2);
      Tuple[][] results = new Tuple[][] { result1, result2 };
      Table alignment = new TupleToTable(baseStructure, witnessList, results).getTable();
      this.html = alignment.toHTML();
    }

    public void setBase(String base) {
      this.base = base;
    }

    public String getBase() {
      return base;
    }

    public void setWitness1(String witness) {
      this.witness1 = witness;
    }

    public String getWitness1() {
      return witness1;
    }

    public void setWitness2(String witness) {
      this.witness2 = witness;
    }

    public String getWitness2() {
      return witness2;
    }

    public void setHtml(String html) {
      this.html = html;
    }

    public String getHtml() {
      return html;
    }
  }

  @SuppressWarnings("serial")
  class AlignmentForm extends Form {

    private final ModelForView modelForView;

    public AlignmentForm(String id, ModelForView modelForView) {
      super(id);
      this.modelForView = modelForView;
      add(new TextField("base", new PropertyModel(modelForView, "base")));
      add(new TextField("witness1", new PropertyModel(modelForView, "witness1")));
      add(new TextField("witness2", new PropertyModel(modelForView, "witness2")));
    }

    @Override
    protected void onSubmit() {
      // NOTE: this can be moved to model!
      modelForView.fillAlignmentTable();
    }

  }

}
