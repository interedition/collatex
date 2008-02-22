package com.sd_editions.collatex.Web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.Collate.Table;
import com.sd_editions.collatex.Collate.TupleToTable;
import com.sd_editions.collatex.Collate.WordAlignmentVisitor;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;

public class Homepage extends WebPage {

  public Homepage() {
    ModelForView model = new ModelForView("a big black cat came in", "on a tiny black mat");

    add(new Label("base", new PropertyModel(model, "base")));
    add(new Label("witness", new PropertyModel(model, "witness")));

    Label label = new Label("alignment", new PropertyModel(model, "html"));
    label.setEscapeModelStrings(false);
    add(label);
    add(new AlignmentForm("alignmentform", model));
  }

  @SuppressWarnings("serial")
  class ModelForView implements Serializable {
    private String base;
    private String witness;
    private String html;

    public ModelForView(String base, String witness) {
      this.base = base;
      this.witness = witness;
      fillAlignmentTable();
    }

    private void fillAlignmentTable() {
      BlockStructure baseStructure;
      BlockStructure witnessStructure;
      try {
        baseStructure = new StringInputPlugin(base).readFile();
        witnessStructure = new StringInputPlugin(witness).readFile();
      } catch (FileNotFoundException e) { // TODO: work away those exceptions.. they are not relevant for Strings
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (BlockStructureCascadeException e) {
        throw new RuntimeException(e);
      }

      WordAlignmentVisitor visitor = new WordAlignmentVisitor(witnessStructure);
      baseStructure.accept(visitor);
      Table alignment = new TupleToTable(baseStructure, witnessStructure, visitor.getResult()).getTable();
      this.html = alignment.toHTML();
    }

    public void setBase(String base) {
      this.base = base;
    }

    public String getBase() {
      return base;
    }

    public void setWitness(String witness) {
      this.witness = witness;
    }

    public String getWitness() {
      return witness;
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
      add(new TextField("witness", new PropertyModel(modelForView, "witness")));
    }

    @Override
    protected void onSubmit() {
      // NOTE: this can be moved to model!
      modelForView.fillAlignmentTable();
    }

  }

}
