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
import com.sd_editions.collatex.spike2.Modifications;
import com.sd_editions.collatex.spike2.Witness;
import com.sd_editions.collatex.spike2.Word;

public class AlignmentView {
  private final Colors colors;

  public AlignmentView(Colors _colors) {
    this.colors = _colors;
  }

  public String toHtml() {
    return coloredWitnesses(); // + modifications();
  }

  @SuppressWarnings("boxing")
  private String coloredWitnesses() {
    List<Modifications> permutations = colors.compareWitness(1, 2);
    Modifications modifications = permutations.get(0);
    Witness base = colors.witnesses.get(0);
    StringBuilder html = new StringBuilder();
    int colorcounter = 0;
    List<String> words = Lists.newArrayList();
    Map<Integer, Integer> baseWordPositionToColor = Maps.newHashMap();
    for (Word word1 : base.getWords()) {
      //      Match match = modifications.getMatchAtBasePosition(word1.position);
      //      if (match == null) {
      //        words.add(word1.original);
      //      } else {
      colorcounter++;
      Word baseWord = word1; //match.getBaseWord();
      baseWordPositionToColor.put(baseWord.position, colorcounter);
      words.add(new WordColorTuple(word1.original, "color" + colorcounter).toHtml());
      //      }
    }
    html.append(Join.join(" ", words));
    html.append("<br/>");
    for (int number = 2; number <= colors.numberOfWitnesses(); number++) {
      Witness witness = colors.getWitness(number);
      permutations = colors.compareWitness(1, number);
      modifications = permutations.get(0);
      html.append(colorAWitness(witness, modifications, baseWordPositionToColor));
    }
    return html.toString();
  }

  @SuppressWarnings("boxing")
  private String colorAWitness(Witness witness, Modifications modifications, Map<Integer, Integer> baseWordPositionToColor) {
    StringBuilder html = new StringBuilder();
    List<String> words;
    words = Lists.newArrayList();
    for (Word word2 : witness.getWords()) {
      Match match = modifications.getMatchAtWitnessPosition(word2.position);
      if (match == null) {
        words.add(word2.original);
      } else {
        System.out.println("!!" + match.getBaseWord().position);
        System.out.println("##" + baseWordPositionToColor.keySet());
        int color = baseWordPositionToColor.get(match.getBaseWord().position);
        words.add(new WordColorTuple(word2.original, "color" + color).toHtml());
      }
    }
    html.append(Join.join(" ", words));
    html.append("<br/>");
    //    html.append("</li></ol></li>");
    return html.toString();

  }

  public List<Alignment> determineAlignment() {
    Matches matches = colors.getMatches(1, 2);
    Set<Match> set = matches.permutations().get(0);
    List<Alignment> alignments = Lists.newArrayList();
    for (Match match : set) {
      Alignment alignment = new Alignment();
      Word baseWord = match.getBaseWord();
      Word witnessWord = match.getWitnessWord();
      alignment.add(baseWord);
      alignment.add(witnessWord);
      alignments.add(alignment);
    }
    return alignments;
  }
}
