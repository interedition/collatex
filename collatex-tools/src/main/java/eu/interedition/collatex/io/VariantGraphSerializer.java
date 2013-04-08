/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.io;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.util.ParallelSegmentationApparatus;
import eu.interedition.collatex.util.VariantGraphRanking;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.util.SortedMap;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphSerializer extends JsonSerializer<VariantGraph> {

  @Override
  public void serialize(final VariantGraph graph, final JsonGenerator jgen, SerializerProvider provider) throws IOException {
    try {
      ParallelSegmentationApparatus.generate(VariantGraphRanking.of(graph), new ParallelSegmentationApparatus.GeneratorCallback() {
        @Override
        public void start() {
          try {
            jgen.writeStartObject();

            jgen.writeArrayFieldStart("witnesses");
            for (Witness witness : Ordering.from(Witness.SIGIL_COMPARATOR).sortedCopy(graph.witnesses())) {
              jgen.writeString(witness.getSigil());
            }
            jgen.writeEndArray();


            jgen.writeArrayFieldStart("table");
          } catch (IOException e) {
            throw Throwables.propagate(e);
          }
        }

        @Override
        public void segment(SortedMap<Witness, Iterable<Token>> contents) {
          try {
            jgen.writeStartArray();
            for (Iterable<Token> tokens : contents.values()) {
              jgen.writeStartArray();
              for (SimpleToken token : Ordering.natural().immutableSortedCopy(Iterables.filter(tokens, SimpleToken.class))) {
                if (token instanceof JsonToken) {
                  jgen.writeTree(((JsonToken) token).getJsonNode());
                } else {
                  jgen.writeString(token.getContent());
                }
              }
              jgen.writeEndArray();
            }
            jgen.writeEndArray();
          } catch (IOException e) {
            throw Throwables.propagate(e);
          }
        }

        @Override
        public void end() {
          try {
            jgen.writeEndArray();
            jgen.writeEndObject();
          } catch (IOException e) {
            throw Throwables.propagate(e);
          }
        }
      });
    } catch (Throwable t) {
      Throwables.propagateIfInstanceOf(Throwables.getRootCause(t), IOException.class);
      throw Throwables.propagate(t);
    }
  }
}
