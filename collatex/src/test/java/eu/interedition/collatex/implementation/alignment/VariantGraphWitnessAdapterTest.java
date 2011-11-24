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

import com.google.common.collect.Lists;
import eu.interedition.collatex.implementation.graph.VariantGraphVertex;
import eu.interedition.collatex.interfaces.IVariantGraph;
import eu.interedition.collatex.interfaces.IVariantGraphVertex;
import org.junit.Test;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author Ronald
 */
public class VariantGraphWitnessAdapterTest {

  //NOTE: question make start and end vertices here?
  //NOTE: The fact that you always have give a vertex key is not nice
  //NOTE: equals and hashcode are not implemented on vertex!
  //NOTE: we have te make the edges?
  //NOTE: not really since we fake vg.isNEar!
  @Test
  public void tokenIteration() {
    // setup
    IVariantGraphVertex a = new VariantGraphVertex("a", null);
    IVariantGraphVertex white = new VariantGraphVertex("white", null);
    IVariantGraphVertex red = new VariantGraphVertex("red", null);
    IVariantGraphVertex cat = new VariantGraphVertex("cat", null);
    List<IVariantGraphVertex> vertices = Lists.newArrayList(a, white, red, cat);
    // create mock
    IVariantGraph vg = mock(IVariantGraph.class);
    //NOTE: Echt nodig? Maakt het resultaat uit?
    when(vg.iterator()).thenReturn(vertices.iterator());
    // class to test
    VariantGraphWitnessAdapter sb = VariantGraphWitnessAdapter.create(vg);
    sb.getTokens();
    // expectations
    verify(vg).iterator();
    verifyNoMoreInteractions(vg);

  }

  @Test
  public void isNear() {
    // setup
    IVariantGraphVertex a = new VariantGraphVertex("a", null);
    IVariantGraphVertex white = new VariantGraphVertex("white", null);
    List<IVariantGraphVertex> vertices = Lists.newArrayList(a, white);
    // create mock
    IVariantGraph vg = mock(IVariantGraph.class);
    //NOTE: Echt nodig? Maakt het resultaat uit?
    when(vg.iterator()).thenReturn(vertices.iterator());
    // class to test
    VariantGraphWitnessAdapter sb = VariantGraphWitnessAdapter.create(vg);
    sb.isNear(a, white);
    // expectations
    verify(vg).isNear(a, white);
    verify(vg).iterator();
    verifyNoMoreInteractions(vg);
    //TODO: check the values that are being returned!
  }

}
