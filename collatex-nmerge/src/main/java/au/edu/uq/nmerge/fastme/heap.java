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
package au.edu.uq.nmerge.fastme;

/**
 * This class is used to represent a heap structure (not
 * memory!) in the fastME program. All the code here is adapted
 * from Desper and Gascuel's original 2002 C program.
 * http://www.ncbi.nlm.nih.gov/CBBresearch/Desper/FastME.html
 * They say you can use it for academic purposes, which this is.
 *
 * @author Desmond Schmidt 24/12/10
 */
public class heap {
  int[] p;

  heap(int size) {
    p = new int[size];
    for (int i = 0; i < size; i++)
      p[i] = i;
  }

  int makeThreshHeap(heap q, double[] v, int arraySize, double thresh) {
    int heapsize;
    heapsize = 0;
    for (int i = 1; i < arraySize; i++)
      if (v[q.p[i]] < thresh)
        pushHeap(q, v, heapsize++, i);
    return heapsize;
  }

  void pushHeap(heap q, double[] v, int length, int i) {
    swap(q, i, length + 1); /*puts new value at the last position in the heap*/
    reHeapElement(q, v, length + 1, length + 1); /*put that guy in the right place*/
  }

  /*swaps two values of a permutation*/
  void swap(heap q, int i, int j) {
    int temp;
    temp = p[i];
    p[i] = p[j];
    p[j] = temp;
    q.p[p[i]] = i;
    q.p[p[j]] = j;
  }

  /**
   * heap is of indices of elements of v,
   * popHeap takes the index at position i and pushes it out of the heap
   * (by pushing it to the bottom of the heap, where it is not noticed)
   */
  void reHeapElement(heap q, double[] v, int length, int i) {
    int up, here;
    here = i;
    up = i / 2;
    if ((up > 0) && (v[p[here]] < v[p[up]])) {
      /// we push the new value up the heap
      while ((up > 0) && (v[p[here]] < v[p[up]])) {
        swap(q, up, here);
        here = up;
        up = here / 2;
      }
    } else
      heapify(q, v, i, length);
  }

  /**
   * The usual Heapify function, tailored for our use with a heap of
   * scores will use array p to keep track of indexes after scoreHeapify
   * is called, the subtree rooted at i will be a heap. p goes from heap
   * to array, q goes from array to heap
   */
  void heapify(heap q, double[] HeapArray, int i, int n) {
    boolean moreswap = true;
    do {
      int left = 2 * i;
      int right = 2 * i + 1;
      int smallest;
      if ((left <= n) && (HeapArray[p[left]] < HeapArray[p[i]]))
        smallest = left;
      else
        smallest = i;
      if ((right <= n) && (HeapArray[p[right]] < HeapArray[p[smallest]]))
        smallest = right;
      if (smallest != i) {
        swap(q, i, smallest);
        // push smallest up the heap
        i = smallest; //check next level down
      } else
        moreswap = false;
    } while (moreswap);
  }

  void permInverse(heap q, int length) {
    for (int i = 0; i < length; i++)
      q.p[p[i]] = i;
  }

  void popHeap(heap q, double[] v, int length, int i) {
    swap(q, i, length); //puts new value at the last position in the heap
    reHeapElement(q, v, length - 1, i); //put the swapped guy in the right place
  }

}
