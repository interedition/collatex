package eu.interedition.collatex.dekker.vectorspace;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.vectorspace.VectorSpace.Vector;
import eu.interedition.collatex.simple.SimpleWitness;

/*
 * @author: Ronald Haentjens Dekker
 * 
 * Steps: 
 * 1. Tokenize, normalize the witnesses
 * 2. Do the matching
 *    Match every witness with every other witness
 *    Match every token from one witness with every token from
 *    the other witness.
 * 3. Build the vector space from the matches
 * 4. Find the optimal alignment in the vector space
 *    based on the length of the vectors and possible
 *    conflicts between dimensions of vectors.
 * 5. Build the variant graph from the optimal vectors.  
 * 
 * NOTE: steps 2 and 3 are logically separate steps, but the
 * implementation performs them together, so that the matches
 * do not have to be stored.
 * 
 */
public class DekkerVectorSpaceAlgorithm extends CollationAlgorithm.Base {
  private TokenVectorSpace s;
  
  public DekkerVectorSpaceAlgorithm() {
    this(new TokenVectorSpace());
  }

  // for testing purposes
  protected DekkerVectorSpaceAlgorithm(TokenVectorSpace s) {
    this.s = s;
  }

  @Override
  public void collate(VariantGraph against, Iterable<Token> witness) {
    throw new RuntimeException("Not yet implemented!");
  }

  @Override
  public void collate(VariantGraph against, Iterable<Token>... witnesses) {
    throw new RuntimeException("Not yet implemented!");
  }

  @Override
  public void collate(VariantGraph against, List<? extends Iterable<Token>> witnesses) {
    SimpleWitness a = (SimpleWitness) witnesses.get(0);
    SimpleWitness b = (SimpleWitness) witnesses.get(1);
    SimpleWitness c;
    if (witnesses.size()>2) {
      c = (SimpleWitness) witnesses.get(2);
    } else {
      c = new SimpleWitness("c");
    }
    collate(against, a, b, c);
  }

  public void collate(VariantGraph graph, SimpleWitness a, SimpleWitness b, SimpleWitness c) {
    // Step 1:
    s.addWitnesses(a, b, c);
    // Step 2: optimize the alignment...
    optimizeAlignment();
    // Step 3: build the variant graph from the vector space
    //createVariantGraph(graph, a, b, c);
    VSToVGBuilder builder = new VSToVGBuilder(s);
    builder.build(graph, a, b, c, 0, 1, 2);
  }

  public void collate(VariantGraph graph, SimpleWitness a, SimpleWitness b) {
    SimpleWitness c = new SimpleWitness("c");
    collate(graph, a, b, c);
  }

  public List<Vector> getAlignment() {
    return s.getVectors();
  }

  /*
   * This method find the optimal alignment by reducing the number of vectors in
   * the vector space.
   */
  private void optimizeAlignment() {
    if (s.getVectors().isEmpty()) {
      throw new RuntimeException("Vector space is empty! There is nothing to align!");
    }
    // group the vectors together by length; vectors may change after commit
    final Multimap<Integer, Vector> vectorMultimap;
    // sort the vectors based on length
    vectorMultimap = ArrayListMultimap.create();
    for (Vector v : s.getVectors()) {
      vectorMultimap.put(v.length, v);
    }
    // find the maximum vector size
    Integer max = Collections.max(vectorMultimap.keySet());
    // traverse groups in descending order
    List<Vector> fixedVectors = Lists.newArrayList();
    for (int vectorLength = max; vectorLength > 0; vectorLength--) {
      LOG.fine("Checking vectors of size: " + vectorLength);
      // check the possible vectors of a certain length against
      // the already committed vectors.
      removeImpossibleVectors(vectorLength, vectorMultimap, fixedVectors);
      // commit possible vectors
      List<Vector> possibleVectors = Lists.newArrayList(vectorMultimap.get(vectorLength));
      if (possibleVectors.isEmpty()) {
        continue;
      }
      if (possibleVectors.size()==1) {
        Vector vector = possibleVectors.get(0);
        commitVector(vector, vectorMultimap, fixedVectors, possibleVectors);
      } else {
        addBestOfCompetingVectors(possibleVectors, vectorMultimap, fixedVectors);
      }
    }
  }

  private void addBestOfCompetingVectors(List<Vector> possibleVectors, Multimap<Integer, Vector> vectorMultimap, List<Vector> fixedVectors) {
    LOG.fine("Vectors to check for local conflicts: "+possibleVectors);
    Multimap<Integer, Vector> vectorConflictMap = makeVectorConflictMap(possibleVectors);
    for (Integer cd : highestToLowestNumberOfConflicts(vectorConflictMap)) {
      for (Vector v : vectorConflictMap.get(cd)) {
        if (isVectorPossibleAgainstFixedVectors(v, fixedVectors)) {
          commitVector(v, vectorMultimap, fixedVectors, possibleVectors);
        } else {
          removeVector(v, vectorMultimap);
        }
      }
    }
  }

 private Multimap<Integer, Vector> makeVectorConflictMap(List<Vector> possibleVectors) {
   Multimap<Integer, Vector> numberConfVector = ArrayListMultimap.create();
   for (Vector v: possibleVectors) {
     numberConfVector.put(s.getNumberOfDimensionsInConflict(v), v);
   }
   return numberConfVector;
 }
 
 private List<Integer> highestToLowestNumberOfConflicts(Multimap<Integer, Vector> conflictMap) {
   List<Integer> conflicts = Lists.newArrayList(conflictMap.keySet());
   Collections.sort(conflicts);
   Collections.reverse(conflicts);
   return conflicts;
 }
 
  //TODO: re-enable this check!
  // check for partially overlap
  // the element that has the most conflicts should be removed
  private void checkForPartiallyConflictingVectors(List<Vector> possibleVectors) {
    Multiset<Vector> conflicts = HashMultiset.create();
    // TODO: this can be performed with less checks
    for (Vector v: possibleVectors) {
      for (Vector o: possibleVectors) {
        if (v==o) {
          continue;
        }
        if (v.overlapsPartially(o)) {
          LOG.info("Vector "+v+" partially overlaps with "+o);
          conflicts.add(v);
        }
      }
    }
    if (conflicts.isEmpty()) {
      return;
    }
    Vector cause = Multisets.copyHighestCountFirst(conflicts).iterator().next();
    // hack
    cause.length--;
  }

  /*
   * For all the possible vectors of a certain length this method checks whether
   * they conflict with one of the previously committed vectors. If so, the
   * possible vector is removed from the map. TODO: Or in case of overlap, split
   * into a smaller vector and then put in back into the map Note that this
   * method changes the possible vectors map.
   */
  private void removeImpossibleVectors(int islandSize, Multimap<Integer, Vector> vectorMultimap, List<Vector> fixedVectors) {
    Queue<Vector> q = new LinkedList<Vector>(vectorMultimap.get(islandSize));
    while(!q.isEmpty()) {
      Vector v = q.remove();
      for (Vector f : fixedVectors) {
        if (f.conflictsWith(v)) {
          LOG.fine(String.format("%s conflicts with %s", f, v));
          vectorMultimap.remove(islandSize, v);
          s.remove(v);
          Vector newV = v.generateNonConflictingVector(f);
          // when the non conflicting vector
          // has at least two dimensions
          // it should be added to s
          // and the vectorMultimap
          if (newV.getDimensions().size()>1) {
            vectorMultimap.put(newV.length, newV);
            s.add(newV);
            q.add(newV);
          } 
          break;
        }
      }
    }
  }

  private void commitVector(Vector vector, final Multimap<Integer, Vector> vectorMultimap, List<Vector> fixedVectors, List<Vector> possibleVectors) {
    LOG.fine("Committing Vector: "+vector);
    fixedVectors.add(vector);
    possibleVectors.remove(vector);
    vectorMultimap.remove(vector.length, vector);
  }

  private void removeVector(Vector vector, Multimap<Integer, Vector> vectorMultimap) {
    LOG.fine("Removing vector: "+vector);
    s.remove(vector);
    vectorMultimap.remove(vector.length, vector);
  }

  private boolean isVectorPossibleAgainstFixedVectors(Vector v, List<Vector> fixedVectors) {
    for (Vector f : fixedVectors) {
      if (f.conflictsWith(v)) {
        return false;
      }
    }
    return true;
  }
}
