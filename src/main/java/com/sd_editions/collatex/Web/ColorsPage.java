package com.sd_editions.collatex.Web;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.PropertyModel;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.CollateCore;

@SuppressWarnings("serial")
public class ColorsPage extends WebPage {

  public ColorsPage() {
    ColorsModel model = new ColorsModel("the drought of march hath perced to the root and is this the right", "the first march of drought pierced to the root and this is the ",
        "the first march of drought hath perced to the root", "");
    add(new Label("colorview", new PropertyModel(model, "html")).setEscapeModelStrings(false));
    add(new ColorsForm("alignmentform", model));
    add(new BookmarkablePageLink("usecaselink", UseCasePage.class));
    add(new BookmarkablePageLink("previous", Homepage.class));
  }

}

@SuppressWarnings("serial")
class ColorsModel implements Serializable {

  public String witness1;
  public String witness2;
  public String witness3;
  public String witness4;
  public String html;

  public ColorsModel(String _witness1, String _witness2, String _witness3, String _witness4) {
    this.witness1 = _witness1;
    this.witness2 = _witness2;
    this.witness3 = _witness3;
    this.witness4 = _witness4;
    generateHTML();
  }

  public void generateHTML() {
    List<String> witnesses = Lists.newArrayList();
    add(witness1, witnesses);
    add(witness2, witnesses);
    add(witness3, witnesses);
    add(witness4, witnesses);
    //    Util.p(witnesses);
    CollateCore colors = new CollateCore(witnesses);
    this.html = new ColorsView(colors).toHtml();
  }

  private void add(String witness, List<String> witnesses) {
    if (!witness.isEmpty()) {
      witnesses.add(witness);
    }
  }
}

@SuppressWarnings("serial")
class ColorsForm extends Form {
  private final ColorsModel modelForView;

  public ColorsForm(String id, ColorsModel myModelForView) {
    super(id);
    this.modelForView = myModelForView;
    add(new TextField("witness1", new PropertyModel(modelForView, "witness1")).setConvertEmptyInputStringToNull(false));
    add(new TextField("witness2", new PropertyModel(modelForView, "witness2")).setConvertEmptyInputStringToNull(false));
    add(new TextField("witness3", new PropertyModel(modelForView, "witness3")).setConvertEmptyInputStringToNull(false));
    add(new TextField("witness4", new PropertyModel(modelForView, "witness4")).setConvertEmptyInputStringToNull(false));
  }

  @Override
  protected void onSubmit() {
    modelForView.generateHTML();
  }
}
