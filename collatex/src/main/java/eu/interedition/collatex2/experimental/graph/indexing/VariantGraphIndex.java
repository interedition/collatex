package eu.interedition.collatex2.experimental.graph.indexing;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.experimental.graph.IVariantGraph;
import eu.interedition.collatex2.experimental.graph.IVariantGraphVertex;
import eu.interedition.collatex2.implementation.indexing.NullToken;
import eu.interedition.collatex2.input.Phrase;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.ITokenIndex;

public class VariantGraphIndex implements ITokenIndex {
  private final Map<String, List<INormalizedToken>> normalizedToTokens;
  private final IVariantGraph graph;

  public static ITokenIndex create(IVariantGraph graph, List<String> repeatingTokens) {
    final VariantGraphIndex index = new VariantGraphIndex(graph);
    for (IWitness witness: graph.getWitnesses()) {
      List<IVariantGraphVertex> path = graph.getPath(witness);
      int position=0; //NOTE: position => index in path
      for (IVariantGraphVertex vertex : path) {
        index.makeTokenUniqueIfneeded(position, index, repeatingTokens, vertex, path);
        position++;
      }
    }
    return index;
  }

  //TODO: Remove index parameter!
  private void makeTokenUniqueIfneeded(int position, VariantGraphIndex index, List<String> repeatingTokens, IVariantGraphVertex vertex, List<IVariantGraphVertex> path) {
    // System.out.println("Trying "+vertex.getNormalized());
    String normalized = vertex.getNormalized();
    // check uniqueness
    final boolean unique = !repeatingTokens.contains(normalized);
    if (unique) {
      List<IVariantGraphVertex> vertices = Lists.newArrayList(vertex);
      index.add(vertices); //TODO: extract separate add method with single vertex parameter!
    } else {
      final List<IVariantGraphVertex> leftVertices = findUniqueVerticesToTheLeft(path, repeatingTokens, position);
      final List<IVariantGraphVertex> rightVertices = findUniqueVerticesToTheRight(path, repeatingTokens, position);
      index.add(leftVertices);
      index.add(rightVertices);
    }
  }

  
  //TODO: I need an index to move to the left and right here!
  //TODO: or an iterator!
  private List<IVariantGraphVertex> findUniqueVerticesToTheRight(List<IVariantGraphVertex> path, List<String> repeatingTokens, int the_real_parameter) {
    List<IVariantGraphVertex> vertices = Lists.newArrayList();
    boolean found = false; // not nice!
    int position = the_real_parameter; //TODO
    for (int i = position ; !found && i < path.size(); i++ ) {
      IVariantGraphVertex rightVertex = path.get(i);
      String normalizedNeighbour = rightVertex.getNormalized();
      found = !repeatingTokens.contains(normalizedNeighbour);
      vertices.add(rightVertex);
    }
    if (!found) {
      vertices.add(graph.getEndVertex());
    }
    return vertices;
  }

  //TODO: I need an index to move to the left and right here!
  //TODO: or an iterator!
  private List<IVariantGraphVertex> findUniqueVerticesToTheLeft(List<IVariantGraphVertex> path, List<String> repeatingTokens, int the_real_parameter) {
    List<IVariantGraphVertex> vertices = Lists.newArrayList();
    boolean found = false; // not nice!
    int position = the_real_parameter; //TODO
    for (int i = position ; !found && i > -1; i-- ) {
      IVariantGraphVertex leftVertex = path.get(i);
      String normalizedNeighbour = leftVertex.getNormalized();
      found = !repeatingTokens.contains(normalizedNeighbour);
      vertices.add(0, leftVertex);
    }
    if (!found) {
      vertices.add(0, graph.getStartVertex());
    }
    return vertices;
  }
  
  private VariantGraphIndex(IVariantGraph graph) {
    this.graph = graph;
    normalizedToTokens = Maps.newLinkedHashMap();
  }

  private void add(List<IVariantGraphVertex> vertices) {
    StringBuilder normalized = new StringBuilder();
    String splitter = "";
    for (IVariantGraphVertex vertex : vertices) {
      normalized.append(splitter).append(vertex.getNormalized());
      splitter = " ";
    }
    // System.out.println("Adding normalized: "+normalized);
    // fill token map; skip begin and end vertices (which contain no tokens!)
    List<INormalizedToken> tokens = Lists.newArrayList();
    for (IVariantGraphVertex vertex : vertices) {
      if (vertex.equals(graph.getStartVertex())||vertex.equals(graph.getEndVertex())) {
        tokens.add(new NullToken(0, null)); //This is not very nice!
        continue;
      }
      if (vertex.getWitnesses().isEmpty()) {
        throw new RuntimeException("STOP! Witness set is not supposed to be empty! Vertex: " + vertex.getNormalized());
      }
      //Note: this code assumes witnesses = an ordered set
      IWitness firstWitness = vertex.getWitnesses().iterator().next();
      INormalizedToken token = vertex.getToken(firstWitness);
      tokens.add(token);
    }
    normalizedToTokens.put(normalized.toString(), tokens);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("VariantGraphIndex: (");
    String delimiter = "";
    for (final String normalizedPhrase : normalizedToTokens.keySet()) {
      result.append(delimiter).append(normalizedPhrase);
      delimiter = ", ";
    }
    result.append(")");
    return result.toString();
  }

  @Override
  public boolean contains(String normalized) {
    return normalizedToTokens.containsKey(normalized);
  }

  //TODO: this is workaround! store real phrases instead of token!
  @Override
  public IPhrase getPhrase(String normalized) {
    if (!contains(normalized)) {
      throw new RuntimeException("Item does not exist!");
    }
    Collection<INormalizedToken> tokens = normalizedToTokens.get(normalized);
    return new Phrase(Lists.newArrayList(tokens));
  }

  @Override
  public int size() {
    return normalizedToTokens.size();
  }

  @Override
  public Set<String> keys() {
    return normalizedToTokens.keySet();
  }
}
