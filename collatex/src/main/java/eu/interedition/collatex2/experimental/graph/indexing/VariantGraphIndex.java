package eu.interedition.collatex2.experimental.graph.indexing;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import eu.interedition.collatex2.experimental.graph.IVariantGraph;
import eu.interedition.collatex2.experimental.graph.IVariantGraphVertex;
import eu.interedition.collatex2.input.Phrase;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphIndex implements IVariantGraphIndex {

  private Multimap<String, INormalizedToken> normalizedToTokens;
  private LinkedHashMap<INormalizedToken, IVariantGraphVertex> tokenToVertex;

  public static IVariantGraphIndex create(IVariantGraph graph, List<String> findRepeatingTokens) {
    final VariantGraphIndex index = new VariantGraphIndex();
    //TODO: change this for three or more witnesses!
    //Note: this code skips begin and end vertices!
    for (IVariantGraphVertex vertex : graph.getVertices().subList(1, graph.getVertices().size()-1)) {
      makeTokenUniqueIfNeeded(index, findRepeatingTokens, vertex);
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
  public IVariantGraphVertex getVertex(INormalizedToken token) {
    return tokenToVertex.get(token);
  }

  private VariantGraphIndex() {
    normalizedToTokens = ArrayListMultimap.create();
    tokenToVertex = Maps.newLinkedHashMap();
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
      final IVariantGraphVertex vertex) {
    String normalized = vertex.getNormalized();
    // kijken of ie unique is
    final boolean unique = !findRepeatingTokens.contains(normalized);
    if (unique) {
      List<IVariantGraphVertex> vertices = Lists.newArrayList(vertex);
      index.add(vertices);
    } 
//    else {
//      //System.out.println("We have to combine stuff here!");
//      final ColumnPhrase leftPhrase = findUniqueColumnPhraseToTheLeft(table, findRepeatingTokens, row, column, token);
//      final ColumnPhrase rightPhrase = findUniqueColumnPhraseToTheRight(table, findRepeatingTokens, row, column, token);
//      index.add(leftPhrase);
//      index.add(rightPhrase);
//    }
  }

  private void add(List<IVariantGraphVertex> vertices) {
    String normalized = "";
    String splitter = "";
    for (IVariantGraphVertex vertex : vertices) {
      normalized += splitter+vertex.getNormalized();
      splitter = " ";
    }
    for (IVariantGraphVertex vertex : vertices) {
      if (vertex.getWitnesses().isEmpty()) {
        throw new RuntimeException("STOP! Witness set is not supposed to be empty! Vertex: "+vertex.getNormalized());
      }
      //Note: this code assumes witnesses = an ordered set
      IWitness firstWitness = vertex.getWitnesses().iterator().next();
      normalizedToTokens.put(normalized, vertex.getToken(firstWitness));
      tokenToVertex.put(vertex.getToken(firstWitness), vertex);
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
