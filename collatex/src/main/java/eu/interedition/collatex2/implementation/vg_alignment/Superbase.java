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
package eu.interedition.collatex2.implementation.vg_alignment;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import eu.interedition.collatex2.implementation.containers.witness.WitnessToken;
import eu.interedition.collatex2.interfaces.*;

import java.util.Iterator;
import java.util.List;

/**
 * The superbase class is a wrapper around the variant graph it represents a variant graph as a witness.
 * <p/>
 * This makes certain alignment steps easier
 *
 * @author Ronald
 */
public class Superbase implements IWitness {
  private final IVariantGraph graph;
  private final List<INormalizedToken> tokens;

  public Superbase(IVariantGraph graph) {
    this.graph = graph;
    this.tokens = Lists.newArrayList(Iterables.filter(graph, INormalizedToken.class));
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
    if (WitnessToken.START.equals(a)) {
      a = graph.getStartVertex();
    }
    if (WitnessToken.END.equals(b)) {
      b = graph.getEndVertex();
    }
    return graph.isNear(a, b);
  }

  @Override
  public Iterator<INormalizedToken> tokenIterator() {
    return tokens.iterator();
  }
}
