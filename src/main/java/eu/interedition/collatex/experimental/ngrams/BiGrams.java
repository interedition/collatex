package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class BiGrams {

  public static List<Phrase> calculate(final Witness a) {
    // Note: getFirstSegment is not so nice; extra indirection
    final List<Word> words = a.getFirstSegment().getWords();
    // TODO: it can be that there are not enough words!
    final List<Word> wordsTodo = words.subList(1, words.size());
    Word previous = words.get(0); // note: temp buffer
    final List<Phrase> bigrams = Lists.newArrayList();
    for (final Word next : wordsTodo) {
      final Phrase bigram = new Phrase(a.getFirstSegment(), previous, next, null);
      bigrams.add(bigram);
      previous = next;
    }
    return bigrams;
  }
}
