package com.sd_editions.collatex.Web;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Join;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sd_editions.collatex.match_spike.WordColorTuple;
import com.sd_editions.collatex.spike2.Colors;
import com.sd_editions.collatex.spike2.Match;
import com.sd_editions.collatex.spike2.Matches;
import com.sd_editions.collatex.spike2.Witness;
import com.sd_editions.collatex.spike2.Word;

public class AlignmentView {
  private final Colors colors;

  public AlignmentView(Colors _colors) {
    this.colors = _colors;
  }

  public String toHtml() {
    Map<Word, Alignment> bla = determineAlignment();
    StringBuilder html = new StringBuilder();
    for (int i = 0; i < colors.numberOfWitnesses(); i++) {
      Witness witness = colors.witnesses.get(i);
      html.append(colorWitness(bla, witness));
    }
    return html.toString();
  }

  public Map<Word, Alignment> determineAlignment() {
    int colorCounter = 1;
    Map<Word, Alignment> wordToAlignment = Maps.newHashMap();
    for (int i = 2; i <= colors.numberOfWitnesses(); i++) {
      Matches matches = colors.getMatches(1, i);
      Set<Match> set = matches.permutations().get(0);
      for (Match match : set) {
        Word baseWord = match.getBaseWord();
        Alignment alignment = wordToAlignment.get(baseWord);
        if (alignment == null) {
          alignment = new Alignment(colorCounter++);
        }
        Word witnessWord = match.getWitnessWord();
        alignment.add(baseWord);
        alignment.add(witnessWord);
        wordToAlignment.put(baseWord, alignment);
        wordToAlignment.put(witnessWord, alignment);
      }
    }
    return wordToAlignment;
  }

  private String colorWitness(Map<Word, Alignment> bla, Witness base) {
    StringBuilder html = new StringBuilder();
    List<String> words = Lists.newArrayList();
    for (Word word : base.getWords()) {
      Alignment alignment = bla.get(word);
      if (alignment != null) {
        words.add(new WordColorTuple(word.original, "color" + alignment.color).toHtml());
      } else {
        words.add(word.original);
      }
    }
    html.append(Join.join(" ", words));
    html.append("<br/>");

    return html.toString();
  }
}
