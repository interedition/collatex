/*
 * NMerge is Copyright 2009-2011 Desmond Schmidt
 *
 * This file is part of NMerge. NMerge is a Java library for merging
 * multiple versions into multi-version documents (MVDs), and for
 * reading, searching and comparing them.
 *
 * NMerge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.interedition.collatex.nmerge.graph;

import eu.interedition.collatex.nmerge.exception.MVDException;
import eu.interedition.collatex.nmerge.mvd.Witness;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;

import static java.util.Collections.disjoint;

/**
 * Represent a set of unattached arcs during building of the Graph
 *
 * @author Desmond Schmidt
 */
public class UnattachedSet<T> extends HashSet<VariantGraphArc<T>> {
  private static final long serialVersionUID = 1L;

  /**
   * the union of all the versions in the unattached set
   */
  Set<Witness> versions;

  /**
   * Create an unattached set
   */
  UnattachedSet() {
    versions = Sets.newHashSet();
  }

  /**
   * Add all unattached arcs to the given node as incoming
   *
   * @param u the node desiring incoming arcs
   */
  void addAllAsIncoming(VariantGraphNode<T> u) throws MVDException {
    Iterator<VariantGraphArc<T>> iter = iterator();
    while (iter.hasNext()) {
      VariantGraphArc<T> a = iter.next();
      u.addIncoming(a);
    }
    // remove them from the unattached set
    ListIterator<VariantGraphArc<T>> iter2 = u.incomingArcs();
    while (iter2.hasNext()) {
      VariantGraphArc<T> a = iter2.next();
      remove(a);
      versions.removeAll(a.versions);
    }
  }

  /**
   * Add any unattached arcs that intersect with the given set
   * of versions
   *
   * @param u  the node desiring incoming arcs
   * @param is versions of the outgoing arc whose versions
   *           must also be incoming
   */
  void addAsIncoming(VariantGraphNode<T> u, Set<Witness> is) throws MVDException {
    Iterator<VariantGraphArc<T>> iter = iterator();
    boolean wasAttached = false;
    while (iter.hasNext()) {
      VariantGraphArc<T> a = iter.next();
      if (!disjoint(a.versions, is)) {
        u.addIncoming(a);
        wasAttached = true;
      }
    }
    if (wasAttached) {
      // now remove the incoming arcs from the unattached set
      // because we can't remove while adding
      ListIterator<VariantGraphArc<T>> iter2 = u.incomingArcs();
      while (iter2.hasNext()) {
        VariantGraphArc<T> a = iter2.next();
        if (remove(a)) {
          versions.removeAll(a.versions);
        }
      }
    }
  }

  /**
   * Override the add method in order to maintain the versions
   *
   * @param a the arc to add
   * @return true if the arc wasn't already there
   */
  public boolean add(VariantGraphArc<T> a) {
    boolean answer = super.add(a);
    versions.addAll(a.versions);
    return answer;
  }

  /**
   * Get the unique arc that intersects with the given arc
   *
   * @param a the arc to get an intersection for
   * @return the relevant arc
   */
  VariantGraphArc<T> getIntersectingArc(VariantGraphArc<T> a) {
    Iterator<VariantGraphArc<T>> iter = iterator();
    while (iter.hasNext()) {
      VariantGraphArc<T> b = iter.next();
      if (!disjoint(b.versions, a.versions)) {
        return b;
      }
    }
    return null;
  }

  /**
   * Take some versions away from a hint, and if it is now empty, remove it
   *
   * @param a   the hint to subtract versions from
   * @param set the set of versions to subtract
   * @return true if the hint was removed
   */
  boolean removeEmptyArc(VariantGraphArc<T> a, Set<Witness> set) throws Exception {
    versions.removeAll(set);
    a.versions.removeAll(set);
    a.getFrom().removeOutgoingVersions(set);
    if (a.versions.isEmpty()) {
      remove(a);
      VariantGraphNode<T> u = a.getFrom();
      u.removeOutgoing(a);
      return true;
    }
    return false;
  }
}
