package eu.interedition.collatex2.implementation.vg_alignment;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class TokenIndexUtil {

  public static List<String> getRepeatedTokens(IVariantGraph graph) {
    // remove start and end vertices
    Set<IVariantGraphVertex> copy = Sets.newLinkedHashSet(graph.vertexSet());
    copy.remove(graph.getStartVertex());
    copy.remove(graph.getEndVertex());
    // we map all vertices to their normalized version
    Multimap<String, IVariantGraphVertex> mapped = ArrayListMultimap.create();
    for (IVariantGraphVertex v : copy) {
      mapped.put(v.getNormalized(), v);
    }
    // fetch all the duplicate keys and return them 
    List<String> result = Lists.newArrayList();
    for (String key : mapped.keySet()) {
      if (mapped.get(key).size() > 1) {
        result.add(key);
      }
    }
    return result;
  }

  public static List<String> getRepeatedTokens(IWitness witness) {
    final Multimap<String, INormalizedToken> normalizedTokenMap = ArrayListMultimap.create();
    for (final INormalizedToken token : witness.getTokens()) {
      normalizedTokenMap.put(token.getNormalized(), token);
    }
    final List<String> repeatingNormalizedTokens = Lists.newArrayList();
    for (final String key : normalizedTokenMap.keySet()) {
      final Collection<INormalizedToken> tokenCollection = normalizedTokenMap.get(key);
      if (tokenCollection.size() > 1) {
        repeatingNormalizedTokens.add(key);
      }
    }
    return repeatingNormalizedTokens;
  }

}
