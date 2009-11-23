package eu.interedition.collatex.experimental.ngrams;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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

  public static List<Subsegment2> getOverlappingBiGrams(final Witness a, final Witness b) {
    final List<Phrase> biGrams1 = calculate(a);
    final List<Phrase> biGrams2 = calculate(b);
    final Map<String, Phrase> biGramMapped1 = normalize(biGrams1);
    final Map<String, Phrase> biGramMapped2 = normalize(biGrams2);
    final Set<String> union = biGramMapped1.keySet();
    union.retainAll(biGramMapped2.keySet());
    final List<Subsegment2> subsegments = Lists.newArrayList();
    for (final String normalized : union) {
      final Subsegment2 subsegment = new Subsegment2(normalized);
      subsegments.add(subsegment);
    }
    return subsegments;
  }

  private static Map<String, Phrase> normalize(final List<Phrase> ngrams) {
    final Map<String, Phrase> normalized = Maps.newLinkedHashMap();
    for (final Phrase ngram : ngrams) {
      // TODO: should be get normalized!
      normalized.put(ngram.getOriginal(), ngram);
    }
    return normalized;
  }
}
