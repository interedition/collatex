package eu.interedition.collatex2.experimental.graph;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import eu.interedition.collatex2.input.Phrase;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;

public class VariantGraphIndex implements IVariantGraphIndex {

  private Multimap<String, INormalizedToken> normalizedToTokens;
  private LinkedHashMap<INormalizedToken, IVariantGraphVertex> tokenToNode;

  public static IVariantGraphIndex create(IVariantGraph graph, List<String> findRepeatingTokens) {
    final VariantGraphIndex index = new VariantGraphIndex();
    //TODO: change this for three or more witnesses!
    for (IVariantGraphVertex node : graph.getVertices().subList(1, graph.getVertices().size())) {
      makeTokenUniqueIfNeeded(index, findRepeatingTokens, node);
    }
    //    for (final String sigil : table.getSigli()) {
//      findUniquePhrasesForRow(sigil, table, index, repeatingTokens);
//    }
    return index;
  }

  @Override
  public boolean containsNormalizedPhrase(String normalized) {
    return normalizedToTokens.containsKey(normalized);
  }

  @Override
  public IVariantGraphVertex getAlignmentNode(INormalizedToken token) {
    return tokenToNode.get(token);
  }

  @Override
  public Collection<INormalizedToken> getTokens(String normalized) {
    return normalizedToTokens.get(normalized);
  }

  private VariantGraphIndex() {
    normalizedToTokens = ArrayListMultimap.create();
    tokenToNode = Maps.newLinkedHashMap();
  }

//  private static void findUniquePhrasesForRow(final String row, final IAlignmentTable table, final AlignmentTableIndex index, final List<String> findRepeatingTokens) {
//    // filteren would be nicer.. maar we doen het maar even alles in een!
//    for (final IColumn column : table.getColumns()) {
//      if (column.containsWitness(row)) {
//        makeTokenUnique(row, table, index, findRepeatingTokens, column);
//      } else {
//        logger.debug("Column " + column.getPosition() + " is empty!");
//      }
//    }
//  }

  

  private static void makeTokenUniqueIfNeeded(final VariantGraphIndex index, final List<String> findRepeatingTokens,
      final IVariantGraphVertex node) {
    INormalizedToken token = node.getToken();
    // kijken of ie unique is
    final boolean unique = !findRepeatingTokens.contains(token.getNormalized());
    if (unique) {
      List<IVariantGraphVertex> nodes = Lists.newArrayList(node);
      index.add(nodes);
    } 
//    else {
//      //System.out.println("We have to combine stuff here!");
//      final ColumnPhrase leftPhrase = findUniqueColumnPhraseToTheLeft(table, findRepeatingTokens, row, column, token);
//      final ColumnPhrase rightPhrase = findUniqueColumnPhraseToTheRight(table, findRepeatingTokens, row, column, token);
//      index.add(leftPhrase);
//      index.add(rightPhrase);
//    }
  }

  private void add(List<IVariantGraphVertex> nodes) {
    String normalized = "";
    String splitter = "";
    for (IVariantGraphVertex node : nodes) {
      normalized += splitter+node.getNormalized();
      splitter = " ";
    }
    for (IVariantGraphVertex node : nodes) {
      normalizedToTokens.put(normalized, node.getToken());
      tokenToNode.put(node.getToken(), node);
    }
  }

  @Override
  public String toString() {
    String result = "AlignmentGraphIndex: (";
    String delimiter = "";
    for (final String normalizedPhrase : normalizedToTokens.keySet()) {
      result += delimiter + normalizedPhrase;
      delimiter = ", ";
    }
    result += ")";
    return result;
  }

  //TODO: remove duplication!
  @Override
  public boolean contains(String normalized) {
    return containsNormalizedPhrase(normalized);
  }

  //TODO: this is workaround! store real phrases instead of token!
  @Override
  public IPhrase getPhrase(String normalized) {
    if(!contains(normalized)) {
      throw new RuntimeException("Item does not exist!");
    }
    Collection<INormalizedToken> tokens = normalizedToTokens.get(normalized);
    return new Phrase(Lists.newArrayList(tokens));
  }

  @Override
  public Collection<IPhrase> getPhrases() {
    List<IPhrase> phrases = Lists.newArrayList();
    for (String key : normalizedToTokens.keySet()) {
      phrases.add(getPhrase(key));
    }
    return phrases;
  }

  @Override
  public int size() {
    return normalizedToTokens.size();
  }

}
