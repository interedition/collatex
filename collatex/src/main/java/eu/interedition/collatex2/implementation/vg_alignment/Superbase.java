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

import com.google.common.collect.Lists;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Ronald
 */
// The superbase class is a wrapper around the variant graph
// it represents a variant graph as a witness
// this makes certain alignment steps easier
// TODO: implement IWitness interface
class Superbase {
    private final IVariantGraph vg;

    Superbase(IVariantGraph vg) {
        this.vg = vg;
    }

    List<INormalizedToken> getTokens() {
        List<INormalizedToken> tokens = Lists.newArrayList();
        Iterator<IVariantGraphVertex> iterator = vg.iterator();
        while(iterator.hasNext()) {
            tokens.add(iterator.next());
        }
        return tokens;
    }

    boolean isNear(INormalizedToken t1, INormalizedToken t2) {
        return vg.isNear(t1, t2);
    }
}
