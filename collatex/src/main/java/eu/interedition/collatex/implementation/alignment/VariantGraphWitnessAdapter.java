/*
 * Copyright 2011 The Interedition Development Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.interedition.collatex.implementation.alignment;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraph;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphVertex;
import eu.interedition.collatex.implementation.input.NormalizedToken;
import eu.interedition.collatex.interfaces.*;

import java.util.Iterator;
import java.util.List;

/**
 * Adapts a variant graph to be treated as a witness with a defined token sequence.
 * <p/>
 * This makes certain alignment steps easier.
 *
 * @author Ronald
 */
public class VariantGraphWitnessAdapter implements IWitness {
  private final PersistentVariantGraph graph;
  private final List<INormalizedToken> tokens = Lists.newArrayList();

  public static VariantGraphWitnessAdapter create(PersistentVariantGraph graph) {
    final VariantGraphWitnessAdapter witnessAdapter = new VariantGraphWitnessAdapter(graph);
    for (PersistentVariantGraphVertex v : graph.traverseVertices(null)) {
      witnessAdapter.tokens.add(new VariantGraphVertexTokenAdapter(witnessAdapter, v));
    }
    return witnessAdapter;
  }

  private VariantGraphWitnessAdapter(PersistentVariantGraph graph) {
    this.graph = graph;
  }

  @Override
  public List<INormalizedToken> getTokens() {
    return tokens;
  }

  @Override
  public String getSigil() {
    return "superbase";
  }

  @Override
  public int size() {
    return tokens.size();
  }

  @Override
  public boolean isNear(IToken a, IToken b) {
    final PersistentVariantGraphVertex va = NormalizedToken.START.equals(a) ? graph.getStart() : (PersistentVariantGraphVertex) a;
    final PersistentVariantGraphVertex vb = NormalizedToken.END.equals(b) ? graph.getEnd() : (PersistentVariantGraphVertex) b;
    return graph.verticesAreAdjacent(va, vb);
  }

  @Override
  public Iterator<INormalizedToken> tokenIterator() {
    return tokens.iterator();
  }

  @Override
  public int compareTo(IWitness o) {
    Preconditions.checkArgument(!(o instanceof VariantGraphWitnessAdapter));
    return -1;
  }

  /**
   * FIXME: takes first token as representative for the whole vertex; assumes transitivity of token equality
   */
  public static class VariantGraphVertexTokenAdapter implements INormalizedToken {
    private final VariantGraphWitnessAdapter witnessAdapter;
    private final PersistentVariantGraphVertex vertex;
    private INormalizedToken firstToken;

    public VariantGraphVertexTokenAdapter(VariantGraphWitnessAdapter witnessAdapter, PersistentVariantGraphVertex vertex) {
      this.witnessAdapter = witnessAdapter;
      this.vertex = vertex;

      final PersistentVariantGraph graph = vertex.getGraph();
      final PersistentVariantGraphVertex start = graph.getStart();
      final PersistentVariantGraphVertex end = graph.getEnd();
      if (start.equals(vertex)) {
        this.firstToken = NormalizedToken.START;
      } else if (end.equals(vertex)) {
        this.firstToken = NormalizedToken.END;
      } else {
        this.firstToken = vertex.getTokens(null).first();
      }
    }

    public PersistentVariantGraphVertex getVertex() {
      return vertex;
    }

    @Override
    public String getNormalized() {
      return firstToken.getNormalized();
    }

    @Override
    public int compareTo(INormalizedToken o) {
      return firstToken.compareTo(o);
    }

    @Override
    public String getContent() {
      return firstToken.getContent();
    }

    @Override
    public String getTrailingWhitespace() {
      return firstToken.getTrailingWhitespace();
    }

    @Override
    public IWitness getWitness() {
      return witnessAdapter;
    }
  }
}
