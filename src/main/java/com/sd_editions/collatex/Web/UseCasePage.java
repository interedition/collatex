package com.sd_editions.collatex.Web;

import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.match_spike.views.UseCaseView;

@SuppressWarnings("serial")
public class UseCasePage extends WebPage {

  public UseCasePage() {
    List<String[]> usecases = Lists.newArrayList();
    usecases.add(new String[] { "the black cat", "THE BLACK CAT", "The black cat", "The, black cat" });
    usecases.add(new String[] { "the white and black cat", "The black cat", "the black and white cat", "the black and green cat" });
    usecases.add(new String[] { "The black cat", "The black and white cat", "The black and green cat" });
    usecases.add(new String[] { "The black cat", "The black and white cat", "The black and green cat", "The black very special cat", "The black not very special cat" });
    usecases.add(new String[] { "This Carpenter hadde wedded newe a wyf", "This Carpenter hadde wedded a newe wyf", "This Carpenter hadde newe wedded a wyf",
        "This Carpenter hadde wedded newly a wyf", "This Carpenter hadde E wedded newe a wyf", "This Carpenter hadde newli wedded a wyf", "This Carpenter hadde wedded a wyf" });
    usecases.add(new String[] { "He was agast, so", "He was agast", "So he was agast", "He was so agast", "He was agast and feerd", "So was he agast" });
    usecases.add(new String[] { "the drought of march hath perced to the root", "the march of the drought hath perced to the root", "the march of drought hath perced to the root" });
    //    usecases.add(new String[] { "the big bug had a big head", "the bug big had a big head", "the bug had a small head" });
    //    usecases.add(new String[] { "the black cat sat on the mat", "the cat sat on the black mat", "the cat sat on the black mat" });
    usecases.add(new String[] { "a cat or dog", "a cat and dog and", "a cat and dog and" });
    //    usecases.add(new String[] { "Auch hier hab ich wieder ein Plätzchen", "Ich hab auch hier wieder ein Pläzchen", "Ich hab auch hier wieder ein Pläzchen" });
    //    usecases.add(new String[] { "the black cat on the table", "the black saw the black cat on the table", "the black saw the black cat on the table" });
    //    usecases.add(new String[] { "the black cat and the black mat", "the black dog and the black mat", "the black dog and the black mat" });

    final ListView usecaseListView = new ListView("usecases", usecases) {
      @Override
      public void populateItem(final ListItem listItem) {
        final String[] usecase = (String[]) listItem.getModelObject();
        int num = listItem.getIndex() + 1;
        listItem.add(new Label("head", "<a name=\"" + num + "\">Use Case #" + num + "</a>").setEscapeModelStrings(false));
        listItem.add(new Label("witnesses", makeView(listItem.getIndex(), usecase)).setEscapeModelStrings(false));
      }

      private String makeView(int index, String[] usecase) {
        return new UseCaseView(usecase).toHtml();
      }
    };
    add(usecaseListView);

  }
}
