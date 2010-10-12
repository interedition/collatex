package eu.interedition.collatex2.legacy.tokencontainers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import eu.interedition.collatex2.input.Phrase;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IRow;
import eu.interedition.collatex2.interfaces.ITokenIndex;
import eu.interedition.collatex2.legacy.indexing.BiGram;
import eu.interedition.collatex2.legacy.indexing.BiGramIndex;
import eu.interedition.collatex2.legacy.indexing.NGram;
import eu.interedition.collatex2.legacy.indexing.NullToken;

//TODO: legacy class REMOVE! REMOVE!
public class AlignmentTableIndex implements ITokenIndex {
  private static Logger logger = LoggerFactory.getLogger(AlignmentTableIndex.class);

  //TODO: rename field normalizedToColumns!
  private final Map<String, IPhrase> normalizedToPhrase;

  private AlignmentTableIndex(IAlignmentTable table) {
    this.normalizedToPhrase = Maps.newLinkedHashMap();
    System.out.println("!!!!!!!!!"+table+"##");
    for (IRow row : table.getRows()) {
      List<INormalizedToken> tokens = Lists.newArrayList(); 
      for (ICell cell : row) {
        if (!cell.isEmpty()) {
          INormalizedToken token = cell.getToken();
          tokens.add(token);
        } else {
          tokens.add(new NullToken(-1, row.getSigil()));
        }
      }
      // do unigram indexing
      final Multimap<String, IPhrase> normalizedTokenMap = ArrayListMultimap.create();
      for (final INormalizedToken token : tokens) {
        normalizedTokenMap.put(token.getNormalized(), new Phrase(Lists.newArrayList(token)));
      }
      // do bigram indexing
      BiGramIndex index = BiGramIndex.create(tokens);
      List<BiGram> biGrams = index.getBiGrams();
      for (BiGram gram : biGrams) {
        normalizedTokenMap.put(gram.getNormalized(), new Phrase(Lists.newArrayList(gram.getFirstToken(), gram.getLastToken())));
      }
      // do the trigram indexing
      if (!biGrams.isEmpty()) {
        List<BiGram> bigramsTodo = Lists.newArrayList(biGrams);//biGrams.subList(1, biGrams.size()-1);
        BiGram current = bigramsTodo.remove(0);
        for (BiGram nextBigram : bigramsTodo) {
          NGram ngram = NGram.create(current);
          ngram.add(nextBigram);
          current = nextBigram;
          normalizedTokenMap.put(ngram.getNormalized(), new Phrase(Lists.newArrayList(ngram)));
        }
      }
      for (final String key : normalizedTokenMap.keySet()) {
        final Collection<IPhrase> tokenCollection = normalizedTokenMap.get(key);
        if (tokenCollection.size() == 1) {
          List<IPhrase> firstPhrase = Lists.newArrayList(normalizedTokenMap.get(key));
          normalizedToPhrase.put(key, firstPhrase.get(0));
        }
      }
    }
  }

  public static ITokenIndex create(final IAlignmentTable table, final List<String> repeatingTokens) {
    final AlignmentTableIndex index = new AlignmentTableIndex(table);
    return index;
  }

  @Override
  public int size() {
    return normalizedToPhrase.size();
  }

  @Override
  public boolean contains(String key) {
    return normalizedToPhrase.containsKey(key);
  }

  @Override
  public IPhrase getPhrase(String key) {
    return normalizedToPhrase.get(key);
  }

  @Override
  public Set<String> keys() {
    return normalizedToPhrase.keySet();
  }
}
