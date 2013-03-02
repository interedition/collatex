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

package eu.interedition.collatex.neo4j;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import org.neo4j.graphdb.Relationship;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Neo4jVariantGraphEdge implements VariantGraph.Edge {
  protected final Neo4jVariantGraph graph;
  protected final Relationship relationship;

  public Neo4jVariantGraphEdge(Neo4jVariantGraph graph, Relationship relationship) {
    this.graph = graph;
    this.relationship = relationship;
  }

  public Neo4jVariantGraphEdge(Neo4jVariantGraph graph, Neo4jVariantGraphVertex from, Neo4jVariantGraphVertex to, Set<Witness> witnesses) {
    this(graph, from.getNode().createRelationshipTo(to.getNode(), Neo4jGraphRelationships.PATH));
    graph.adapter.setWitnesses(this, witnesses);
  }

  public boolean traversableWith(Set<Witness> witnesses) {
    if (witnesses == null || witnesses.isEmpty()) {
      return true;
    }
    final Set<Witness> edgeWitnesses = witnesses();
    for (Witness witness : witnesses) {
      if (edgeWitnesses.contains(witness)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public VariantGraph.Edge add(Set<Witness> witnesses) {
    graph.adapter.setWitnesses(this, Sets.union(witnesses(), witnesses));
    return this;
  }

  @Override
  public Set<Witness> witnesses() {
    return graph.adapter.getWitnesses(this);
  }

  public static Predicate<VariantGraph.Edge> createTraversableFilter(final Set<Witness> witnesses) {
    return new Predicate<VariantGraph.Edge>() {

      @Override
      public boolean apply(VariantGraph.Edge input) {
        return ((Neo4jVariantGraphEdge) input).traversableWith(witnesses);
      }
    };
  }

  @Override
  public VariantGraph graph() {
    return graph;
  }

  @Override
  public VariantGraph.Vertex from() {
    return graph.vertexWrapper.apply(relationship.getStartNode());
  }

  @Override
  public VariantGraph.Vertex to() {
    return graph.vertexWrapper.apply(relationship.getEndNode());
  }

  @Override
  public void delete() {
    relationship.delete();
  }

  @Override
  public int hashCode() {
    return relationship.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof VariantGraph.Edge) {
      return relationship.equals(((Neo4jVariantGraphEdge) obj).relationship);
    }
    return super.equals(obj);
  }

  @Override
  public String toString() {
    return new StringBuilder(from().toString()).append(" -> ").append(to().toString()).toString();
  }
}
