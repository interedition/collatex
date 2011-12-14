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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import eu.interedition.collatex.implementation.graph.VariantGraph;
import eu.interedition.collatex.implementation.graph.VariantGraphVertex;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.implementation.input.Witness;
import eu.interedition.collatex.interfaces.*;

import java.util.List;

/**
 * Adapts a variant graph to be treated as a witness with a defined token sequence.
 * <p/>
 * This makes certain alignment steps easier.
 *
 * @author Ronald
 */
public class VariantGraphWitnessAdapter implements IWitness {
  private final VariantGraph graph;
  private final List<Token> tokens = Lists.newArrayList();

  public static VariantGraphWitnessAdapter create(VariantGraph graph) {
    final VariantGraphWitnessAdapter witnessAdapter = new VariantGraphWitnessAdapter(graph);
    for (VariantGraphVertex v : graph.vertices()) {
      witnessAdapter.tokens.add(new VariantGraphVertexTokenAdapter(witnessAdapter, v));
    }
    return witnessAdapter;
  }

  private VariantGraphWitnessAdapter(VariantGraph graph) {
    this.graph = graph;
  }

  @Override
  public List<Token> getTokens() {
    return tokens;
  }

  @Override
  public String getSigil() {
    return Witness.SUPERBASE.getSigil();
  }

  @Override
  public boolean isNear(Token a, Token b) {
    final VariantGraphVertex va = SimpleToken.START.equals(a) ? graph.getStart() : ((VariantGraphVertexTokenAdapter) a).getVertex();
    final VariantGraphVertex vb = SimpleToken.END.equals(b) ? graph.getEnd() : ((VariantGraphVertexTokenAdapter) b).getVertex();
    return graph.verticesAreAdjacent(va, vb) && (Iterables.size(va.outgoing()) == 1 || Iterables.size(vb.incoming()) == 1);
  }

  @Override
  public int compareTo(IWitness o) {
    Preconditions.checkArgument(!(o instanceof VariantGraphWitnessAdapter));
    return -1;
  }

  @Override
  public String toString() {
    return getSigil();
  }

  /**
   * FIXME: takes first token as representative for the whole vertex; assumes transitivity of token equality
   */
  public static class VariantGraphVertexTokenAdapter implements Token {
    private final VariantGraphWitnessAdapter witnessAdapter;
    private final VariantGraphVertex vertex;
    private Token firstToken;

    public VariantGraphVertexTokenAdapter(VariantGraphWitnessAdapter witnessAdapter, VariantGraphVertex vertex) {
      this.witnessAdapter = witnessAdapter;
      this.vertex = vertex;

      final VariantGraph graph = vertex.getGraph();
      final VariantGraphVertex start = graph.getStart();
      final VariantGraphVertex end = graph.getEnd();
      if (start.equals(vertex)) {
        this.firstToken = SimpleToken.START;
      } else if (end.equals(vertex)) {
        this.firstToken = SimpleToken.END;
      } else {
        this.firstToken = vertex.tokens().first();
      }
    }

    public VariantGraphVertex getVertex() {
      return vertex;
    }

    @Override
    public int compareTo(Token o) {
      return firstToken.compareTo(o);
    }

    @Override
    public String getContent() {
      return firstToken.getContent();
    }

    @Deprecated
    public String getNormalized() {
      return ((SimpleToken) firstToken).getNormalized();
    }

    @Override
    public IWitness getWitness() {
      return witnessAdapter;
    }

    @Override
    public String toString() {
      return vertex.toString();
    }
  }
}
