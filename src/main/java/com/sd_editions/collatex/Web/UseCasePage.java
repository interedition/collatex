package com.sd_editions.collatex.Web;

import java.util.List;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.match.views.UseCaseView;

@SuppressWarnings("serial")
public class UseCasePage extends WebPage {

  public UseCasePage() {
    List<String[]> usecases = Lists.newArrayList();
    usecases.add(new String[] { "The black cat", "The black and white cat", "The black and green cat", "The black very special cat", "The black not very special cat" });
    usecases.add(new String[] { "The black dog chases a red cat.", "A red cat chases the black dog.", "A red cat chases the yellow dog" });
    usecases.add(new String[] { "the black cat and the black mat", "the black dog and the black mat", "the black dog and the black mat" });
    usecases.add(new String[] { "the black cat on the table", "the black saw the black cat on the table", "the black saw the black cat on the table" });
    usecases.add(new String[] { "the black cat sat on the mat", "the cat sat on the black mat", "the cat sat on the black mat" });
    usecases.add(new String[] { "the black cat", "THE BLACK CAT", "The black cat", "The, black cat" });
    usecases.add(new String[] { "the white and black cat", "The black cat", "the black and white cat", "the black and green cat" });
    usecases.add(new String[] { "a cat or dog", "a cat and dog and", "a cat and dog and" });
    usecases.add(new String[] { "He was agast, so", "He was agast", "So he was agast", "He was so agast", "He was agast and feerd", "So was he agast" });
    usecases.add(new String[] { "the big bug had a big head", "the bug big had a big head", "the bug had a small head" });
    usecases.add(new String[] { "the big bug had a big head", "the bug had a small head" });
    usecases.add(new String[] { "the bug big had a big head", "the bug had a small head", "the bug had a small head" });
    usecases.add(new String[] { "the drought of march hath perced to the root and is this the right", "the first march of drought pierced to the root and this is the ",
        "the first march of drought hath perced to the root" });
    usecases.add(new String[] { "the drought of march hath perced to the root", "the march of the drought hath perced to the root", "the march of drought hath perced to the root" });
    usecases.add(new String[] { "the very first march of drought hath", "the drought of march hath", "the drought of march hath" });
    usecases.add(new String[] { "When April with his showers sweet with fruit The drought of March has pierced unto the root",
        "When showers sweet with April fruit The March of drought has pierced to the root", "When showers sweet with April fruit The drought of March has pierced the rood" });
    usecases.add(new String[] { "This Carpenter hadde wedded newe a wyf", "This Carpenter hadde wedded a newe wyf", "This Carpenter hadde newe wedded a wyf",
        "This Carpenter hadde wedded newly a wyf", "This Carpenter hadde E wedded newe a wyf", "This Carpenter hadde newli wedded a wyf", "This Carpenter hadde wedded a wyf" });
    usecases.add(new String[] { "Almost every aspect of what scholarly editors do may be changed",
        "Hardly any aspect of what stupid editors do in the privacy of their own home may be changed again and again",
        "very many aspects of what scholarly editors do in the livingrooms of their own home may not be changed" });
    usecases.add(new String[] { "Du kennst von Alters her meine Art, mich anzubauen, irgend mir an einem vertraulichen Orte ein Hüttchen aufzuschlagen, und da mit aller Einschränkung zu herbergen.",
        "Du kennst von Altersher meine Art, mich anzubauen, mir irgend an einem vertraulichen Ort ein Hüttchen aufzuschlagen, und da mit aller Einschränkung zu herbergen." });
    usecases.add(new String[] { "Auch hier hab ich wieder ein Plätzchen", "Ich hab auch hier wieder ein Pläzchen", "Ich hab auch hier wieder ein Pläzchen" });
    usecases.add(new String[] { "ταυτα ειπων ο ιϲ̅ εξηλθεν ϲυν τοιϲ μαθηταιϲ αυτου περαν του χειμαρρου των κεδρων οπου ην κηποϲ ειϲ ον ειϲηλθεν αυτοϲ και οι μαθηται αυτου",
        "ταυτα ειπων ― ιϲ̅ εξηλθεν ϲυν τοιϲ μαθηταιϲ αυτου περαν του χειμαρρου των κεδρων οπου ην κηποϲ ειϲ ον ειϲηλθεν αυτοϲ και οι μαθηται αυτου",
        "ταυτα ειπων ο ιη̅ϲ εξηλθεν ϲυν τοιϲ μαθηταιϲ αυτου περαν του χειμαρρου του κεδρου οπου ην κηποϲ ειϲ ον ειϲηλθεν αυτοϲ και οι μαθηται αυτου" });
    usecases.add(new String[] { "I bought this glass, because it matches those dinner plates.", "I bought those glasses." });

    final ListView usecaseTabListView = new ListView("tabs", usecases) {
      @Override
      public void populateItem(final ListItem listItem) {
        int num = listItem.getIndex() + 1;
        listItem.add(new Label("link_to_usecase", "<a href=\"#usecase-" + num + "\">" + num + "</a>").setEscapeModelStrings(false));
      }
    };
    add(usecaseTabListView);

    final ListView usecaseListView = new ListView("usecases", usecases) {
      @Override
      public void populateItem(final ListItem listItem) {
        final String[] usecase = (String[]) listItem.getModelObject();
        int num = listItem.getIndex() + 1;
        WebMarkupContainer usecaseDiv = new WebMarkupContainer("usecase_div");
        usecaseDiv.add(new SimpleAttributeModifier("id", "usecase-" + num));
        listItem.add(usecaseDiv);
        usecaseDiv.add(new Label("head", "<a name=\"" + num + "\">Use Case #" + num + "</a>").setEscapeModelStrings(false));
        usecaseDiv.add(new Label("witnesses", makeView(usecase)).setEscapeModelStrings(false));
      }

      private String makeView(String[] usecase) {
        return new UseCaseView(usecase).toHtml();
      }
    };
    add(usecaseListView);

  }
}
