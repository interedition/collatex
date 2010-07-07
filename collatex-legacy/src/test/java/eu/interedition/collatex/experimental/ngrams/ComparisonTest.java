/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
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

package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import junit.framework.TestCase;

import com.google.common.collect.Lists;

import eu.interedition.collatex.experimental.interfaces.WitnessF;
import eu.interedition.collatex.experimental.ngrams.alignment.Alignment;
import eu.interedition.collatex.experimental.ngrams.alignment.Gap;
import eu.interedition.collatex.experimental.ngrams.alignment.Modification;
import eu.interedition.collatex2.interfaces.IWitness;

// TODO rename to alignment test part 2 and move!  
public class ComparisonTest extends TestCase {

  public void testAddition_InTheMiddle() {
    final List<Modification> modifications = getModifications("a cat", "a calico cat");
    assertEquals(1, modifications.size());
    assertEquals("addition: calico position: 2", modifications.get(0).toString());
  }

  //  public void testWorCount() {
  //    final Witness w1 = builder.build("\t\n  a cat");
  //    final Witness w2 = builder.build("a cat");
  //    assertEquals(w1.getFirstSegment().wordSize(), w2.getFirstSegment().wordSize());
  //  }

  public void testAddition_AtTheEnd() {
    final List<Modification> modifications = getModifications("to be", "to be lost");
    assertEquals(1, modifications.size());
    assertEquals("addition: lost position: at the end", modifications.get(0).toString());
  }

  public void testAddition_AtTheStart() {
    final List<Modification> modifications = getModifications("to be", "not to be");
    assertEquals(1, modifications.size());
    assertEquals("addition: not position: 1", modifications.get(0).toString());
  }

  public void testOmission_InTheMiddle() {
    final List<Modification> modifications = getModifications("a white working horse", "a horse");
    assertEquals(1, modifications.size());
    assertEquals("omission: white working position: 2", modifications.get(0).toString());
  }

  public void testOmission_AtTheStart() {
    final List<Modification> modifications = getModifications("an almost certain death", "certain death");
    assertEquals(1, modifications.size());
    assertEquals("omission: an almost position: 1", modifications.get(0).toString());
  }

  public void testOmission_AtTheEnd() {
    final List<Modification> modifications = getModifications("a calico, or tortoiseshell cat", "a calico");
    assertEquals(1, modifications.size());
    assertEquals("omission: or tortoiseshell cat position: 3", modifications.get(0).toString());
  }

  public void testReplacementVariantAtTheStart() {
    final List<Modification> modifications = getModifications("black cat", "white cat");
    assertEquals(1, modifications.size());
    assertEquals("replacement: black / white position: 1", modifications.get(0).toString());
  }

  public void testReplacementVariantAtTheEnd() {
    final List<Modification> modifications = getModifications("it's black", "it's white");
    assertEquals(1, modifications.size());
    assertEquals("replacement: black / white position: 2", modifications.get(0).toString());
  }

  // Note: transpositions and modifications are not in the same classes anymore
  //  public void testTranspositionsShouldNotBeCountedAsAdditions() {
  //    Colors colors = new Colors("a b", "b a");
  //    Comparison comparison = colors.compareWitness(1, 2);
  //    List<Modification> modifications = comparison.getModifications();
  //    assertEquals(1, modifications.size());
  //    assertEquals("transposition: a distance: 1", modifications.get(0).toString());
  //  }

  public void testPhraseAdditionAtTheStart() {
    final List<Modification> modifications = getModifications("a b", "c d a b");
    assertEquals(1, modifications.size());
    assertEquals("addition: c d position: 1", modifications.get(0).toString());
  }

  public void testPhraseAdditionAtTheEnd() {
    final List<Modification> modifications = getModifications("a b", "a b c d");
    assertEquals(1, modifications.size());
    assertEquals("addition: c d position: at the end", modifications.get(0).toString());
  }

  public void testPhraseOmissionAtTheStart() {
    final List<Modification> modifications = getModifications("a b c d", "c d");
    assertEquals(1, modifications.size());
    assertEquals("omission: a b position: 1", modifications.get(0).toString());
  }

  public void testPhraseOmissionAtTheEnd() {
    final List<Modification> modifications = getModifications("a b c d", "a b");
    assertEquals(1, modifications.size());
    assertEquals("omission: c d position: 3", modifications.get(0).toString());
  }

  public void testPhraseVariantReplacementAtTheStart() {
    final List<Modification> modifications = getModifications("a b c d", "e f g c d");
    assertEquals(1, modifications.size());
    assertEquals("replacement: a b / e f g position: 1", modifications.get(0).toString());
  }

  public void testPhraseVariantReplacementAtTheEnd() {
    final List<Modification> modifications = getModifications("a b c d", "a b e f g");
    assertEquals(1, modifications.size());
    assertEquals("replacement: c d / e f g position: 3", modifications.get(0).toString());
  }

  // NOTE: with the old algorithm this resulted in an addition and an omission
  // NOTE: the new algorithm sees it as a replacement
  public void testCombineAdditionAndRemovalToTestPositions() {
    final List<Modification> modifications = getModifications("a b c", "c d e");
    assertEquals(1, modifications.size());
    assertEquals("replacement: a b c / c d e position: 1", modifications.get(0).toString());
  }

  // NOTE: with the old algorithm this resulted in an addition and an omission
  // NOTE: the new algorithm sees it as a replacement
  public void testCombineAdditionAndRemovalToTestPositionsMirrored() {
    final List<Modification> modifications = getModifications("c d e", "a b c");
    assertEquals(1, modifications.size());
    assertEquals("replacement: c d e / a b c position: 1", modifications.get(0).toString());
  }

  //  public void testTransposition() {
  //    Colors colors = new Colors("a b c d e f", "a c d b e g");
  //    Comparison comparison = colors.compareWitness(1, 2);
  //    List<Modification> modifications = comparison.getModifications();
  //    assertEquals(2, modifications.size());
  //    assertEquals("replacement: f / g position: 6", modifications.get(0).toString());
  //    assertEquals("transposition: b distance: 2", modifications.get(1).toString());
  //  }
  //
  //  public void testTransposition2() {
  //    Colors colors = new Colors("a b c", "c b a");
  //    Comparison comparison = colors.compareWitness(1, 2);
  //    List<Modification> modifications = comparison.getModifications();
  //    assertEquals(2, modifications.size());
  //    assertEquals("transposition: a distance: 2", modifications.get(0).toString());
  //    assertEquals("transposition: c distance: 2", modifications.get(0).toString());
  //  }

  private List<Modification> getModifications(final String base, final String witness) {
    final IWitness a = WitnessF.create("A", base);
    final IWitness b = WitnessF.create("B", witness);
    final Alignment al = Alignment.create(a, b);
    return convertAlignmentToModifications(al);
  }

  private List<Modification> convertAlignmentToModifications(final Alignment al) {
    final List<Gap> gaps = al.getGaps();
    final List<Modification> modifications = Lists.newArrayList();
    for (final Gap gap : gaps) {
      modifications.add(gap.getModification());
    }
    return modifications;
  }

  // a b
  // a c b
  @SuppressWarnings("boxing")
  public void testModificationsInMatchSequences() {
    final IWitness a = WitnessF.create("A", "a b");
    final IWitness b = WitnessF.create("B", "a c b");
    final Alignment alignment = Alignment.create(a, b);
    final List<Modification> results = convertAlignmentToModifications(alignment);
    assertEquals(1, results.size());
    assertEquals("addition: c position: 2", results.get(0).toString());
  }

  //NOTE: the old algorithm so 3 modifications
  //NOTE: the new one only sees one!
  public void testModificationsInBetweenMatchSequences() {
    final IWitness a = WitnessF.create("A", "a b y c z d");
    final IWitness b = WitnessF.create("B", "a x b c n d");
    final Alignment alignment = Alignment.create(a, b);
    final List<Modification> results = convertAlignmentToModifications(alignment);
    assertEquals(1, results.size());
    assertEquals("replacement: b y c z / x b c n position: 2", results.get(0).toString());

    //    assertEquals(3, results.size());
    // assertEquals("addition: x position: 2", results.get(0).toString());
    // assertEquals("omission: y position: 3", results.get(1).toString());
    // assertEquals("replacement: z / n position: 5", results.get(2).toString());
  }

  public void testModificationAtTheEnd() {
    final IWitness a = WitnessF.create("A", "a b");
    final IWitness b = WitnessF.create("B", "a c");
    final Alignment alignment = Alignment.create(a, b);
    final List<Modification> results = convertAlignmentToModifications(alignment);
    assertEquals(1, results.size());
    assertEquals("replacement: b / c position: 2", results.get(0).toString());
  }

}
