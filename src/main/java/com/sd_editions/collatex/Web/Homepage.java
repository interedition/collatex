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

@SuppressWarnings("serial")
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
    private String[] witnesses;
    private String html;

    public ModelForView(String newBase, String[] newWitnesses) {
      this.base = newBase;
      this.witnesses = newWitnesses;
      fillAlignmentTable();
    }

    void fillAlignmentTable() {
      BlockStructure baseStructure = string2BlockStructure(base);
      List<BlockStructure> witnessList = new ArrayList<BlockStructure>();
      List<Tuple[]> resultList = new ArrayList<Tuple[]>();
      for (String witness : witnesses) {
        BlockStructure witnessStructure = string2BlockStructure(witness);
        WordAlignmentVisitor visitor = new WordAlignmentVisitor(witnessStructure);
        baseStructure.accept(visitor);
        witnessList.add(witnessStructure);
        resultList.add(visitor.getResult());
      }

      Tuple[][] results = resultList.toArray(new Tuple[][] {});
      Table alignment = new TupleToTable(baseStructure, witnessList, results).getTable();
      this.html = alignment.toHTML();
    }

    BlockStructure string2BlockStructure(String string) {
      BlockStructure result = null;
      try {
        result = new StringInputPlugin(string).readFile();
        // TODO: work away those exceptions.. they are not relevant for Strings
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (BlockStructureCascadeException e) {
        throw new RuntimeException(e);
      }
      return result;
    }

    public void setBase(String newBase) {
      this.base = newBase;
    }

    public String getBase() {
      return base;
    }

    public void setWitness1(String witness) {
      this.witnesses[0] = witness;
    }

    public String getWitness1() {
      return witnesses[0];
    }

    public void setWitness2(String witness) {
      this.witnesses[1] = witness;
    }

    public String getWitness2() {
      return witnesses[1];
    }

    public void setHtml(String newHtml) {
      this.html = newHtml;
    }

    public String getHtml() {
      return html;
    }
  }

  @SuppressWarnings("serial")
  class AlignmentForm extends Form {
    private final ModelForView modelForView;

    public AlignmentForm(String id, ModelForView myModelForView) {
      super(id);
      this.modelForView = myModelForView;
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
