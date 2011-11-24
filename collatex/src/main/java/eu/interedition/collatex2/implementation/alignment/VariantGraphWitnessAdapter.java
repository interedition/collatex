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
package eu.interedition.collatex2.implementation.alignment;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import eu.interedition.collatex2.implementation.input.NormalizedToken;
import eu.interedition.collatex2.interfaces.*;

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
  private final IVariantGraph graph;
  private final List<INormalizedToken> tokens;

  public static VariantGraphWitnessAdapter create(IVariantGraph graph) {
    return new VariantGraphWitnessAdapter(graph, Lists.newArrayList(Iterables.filter(graph, INormalizedToken.class)));
  }

  private VariantGraphWitnessAdapter(IVariantGraph graph, List<INormalizedToken> tokens) {
    this.graph = graph;
    this.tokens = tokens;
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
    if (NormalizedToken.START.equals(a)) {
      a = graph.getStartVertex();
    }
    if (NormalizedToken.END.equals(b)) {
      b = graph.getEndVertex();
    }
    return graph.isNear(a, b);
  }

  @Override
  public Iterator<INormalizedToken> tokenIterator() {
    return tokens.iterator();
  }
}
