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

package eu.interedition.collatex2.alignment;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.interedition.collatex2.implementation.CollateXEngine;

public class AlignmentTest {
	private static final Logger LOG = LoggerFactory
			.getLogger(AlignmentTest.class);
	private CollateXEngine factory;

	@Before
	public void setup() {
		factory = new CollateXEngine();
	}

	@Test
	public void dummy() {
		assertTrue(true);
	}

	// @Test
	// public void testSimple1() {
	// final IWitness a = factory.createWitness("A", "a b");
	// final IWitness b = factory.createWitness("B", "a c b");
	// final IAlignment ali = PairwiseAlignmentHelper.align(factory, a, b);
	// final List<IMatch> matches = ali.getMatches();
	// assertEquals(2, matches.size());
	// assertEquals("a", matches.get(0).getNormalized());
	// assertEquals("b", matches.get(1).getNormalized());
	// final List<IGap> gaps = ali.getGaps();
	// assertEquals(1, gaps.size());
	// assertEquals("\"c\" added", gaps.get(0).toString());
	// }
	//
	// //Copied from TextAlignmentTest
	// @Test
	// public void testAlignment() {
	// final IWitness a = factory.createWitness("A", "cat");
	// final IWitness b = factory.createWitness("B", "cat");
	// final IAlignment alignment = PairwiseAlignmentHelper.align(factory, a,
	// b);
	// final List<IMatch> matches = alignment.getMatches();
	// assertEquals(1, matches.size());
	// assertEquals("cat", matches.get(0).getNormalized());
	// }
	//
	// @Test
	// public void testAlignment2Matches() {
	// final IWitness a = factory.createWitness("A", "The black cat");
	// final IWitness b = factory.createWitness("B", "The black and white cat");
	// final IAlignment alignment = PairwiseAlignmentHelper.align(factory, a,
	// b);
	// final List<IMatch> matches = alignment.getMatches();
	// assertEquals(2, matches.size());
	// assertEquals("the black", matches.get(0).getNormalized());
	// assertEquals("cat", matches.get(1).getNormalized());
	// }
	//
	// @Test
	// public void testAlignment2Gaps() {
	// final IWitness a = factory.createWitness("A", "The black cat");
	// final IWitness b = factory.createWitness("B", "The black and white cat");
	// final IAlignment alignment = PairwiseAlignmentHelper.align(factory, a,
	// b);
	// final List<IGap> gaps = alignment.getGaps();
	// assertEquals(1, gaps.size());
	// final IGap gap = gaps.get(0);
	// assertTrue(gap.isAddition());
	// assertTrue("Phrase A is not empty!", gap.getColumns().isEmpty());
	// assertEquals("and white", gap.getPhrase().getNormalized());
	// }
	//
	// // Note: taken from TextAlignmentTest!
	// @Test
	// public void testAddition_AtTheStart() {
	// final IWitness a = factory.createWitness("A", "to be");
	// final IWitness b = factory.createWitness("B", "not to be");
	// final IAlignment alignment = PairwiseAlignmentHelper.align(factory, a,
	// b);
	// final List<IMatch> matches = alignment.getMatches();
	// assertEquals(1, matches.size());
	// assertEquals("to be", matches.get(0).getNormalized());
	// final List<IGap> gaps = alignment.getGaps();
	// assertEquals(1, gaps.size());
	// final IGap gap = gaps.get(0);
	// assertTrue(gap.isAddition());
	// assertTrue("Phrase A is not empty!", gap.getColumns().isEmpty());
	// assertEquals("not", gap.getPhrase().getNormalized());
	// final List<IAddition> additions = alignment.getAdditions();
	// assertEquals(1, additions.size());
	// final IAddition addition = additions.get(0);
	// assertEquals("not", addition.getAddedPhrase().getNormalized());
	// }

	// @Test
	// public void testAddition_AtTheEnd() {
	// final IWitness a = factory.createWitness("A", "to be");
	// final IWitness b = factory.createWitness("B", "to be or not");
	// final IAlignment alignment = PairwiseAlignmentHelper.align(factory, a,
	// b);
	// final List<IMatch> matches = alignment.getMatches();
	// assertEquals(1, matches.size());
	// assertEquals("to be", matches.get(0).getNormalized());
	// final List<IGap> gaps = alignment.getGaps();
	// assertEquals(1, gaps.size());
	// final IGap gap = gaps.get(0);
	// assertTrue(gap.isAddition());
	// assertTrue("Phrase A is not empty!", gap.getColumns().isEmpty());
	// assertEquals("or not", gap.getPhrase().getNormalized());
	// final List<IAddition> additions = alignment.getAdditions();
	// assertEquals(1, additions.size());
	// final IAddition addition = additions.get(0);
	// assertEquals("or not", addition.getAddedPhrase().getNormalized());
	// }
	//
	// @Test
	// public void testAddition_InTheMiddle() {
	// final IWitness a = factory.createWitness("A", "to be");
	// final IWitness b = factory.createWitness("B", "to think, therefore be");
	// final IAlignment alignment = PairwiseAlignmentHelper.align(factory, a,
	// b);
	// final List<IMatch> matches = alignment.getMatches();
	// assertEquals(2, matches.size());
	// assertEquals("to", matches.get(0).getNormalized());
	// assertEquals("be", matches.get(1).getNormalized());
	// final List<IGap> gaps = alignment.getGaps();
	// assertEquals(1, gaps.size());
	// final IGap gap = gaps.get(0);
	// assertTrue(gap.isAddition());
	// assertTrue("Phrase A is not empty!", gap.getColumns().isEmpty());
	// assertEquals("think therefore", gap.getPhrase().getNormalized());
	// final List<IAddition> additions = alignment.getAdditions();
	// assertEquals(1, additions.size());
	// final IAddition addition = additions.get(0);
	// assertEquals("think therefore",
	// addition.getAddedPhrase().getNormalized());
	// }
	//
	// @Test
	// public void testTransposition1Matches() {
	// final IWitness a = factory.createWitness("A",
	// "The black dog chases a red cat.");
	// final IWitness b = factory.createWitness("B",
	// "A red cat chases the black dog.");
	// final IAlignment align = PairwiseAlignmentHelper.align(factory, a, b);
	// final List<IMatch> matches = align.getMatches();
	// assertEquals(3, matches.size());
	// assertEquals("the black dog", matches.get(0).getNormalized());
	// assertEquals("chases", matches.get(1).getNormalized());
	// assertEquals("a red cat", matches.get(2).getNormalized());
	// }
	//
	// @Test
	// public void testTransposition1Gaps() {
	// final IWitness a = factory.createWitness("A",
	// "The black dog chases a red cat.");
	// final IWitness b = factory.createWitness("B",
	// "A red cat chases the black dog.");
	// final IAlignment align = PairwiseAlignmentHelper.align(factory, a, b);
	// final List<IGap> gaps = align.getGaps();
	// assertTrue(gaps.toString(), gaps.isEmpty());
	// }
	//
	// @Test
	// public void testTransposition2Matches() {
	// final IWitness a = factory.createWitness("A", "d a b");
	// final IWitness b = factory.createWitness("B", "a c b d");
	// final IAlignment align = PairwiseAlignmentHelper.align(factory, a, b);
	// final List<IMatch> matches = align.getMatches();
	// assertEquals(3, matches.size());
	// assertEquals("d", matches.get(0).getNormalized());
	// assertEquals("a", matches.get(1).getNormalized());
	// assertEquals("b", matches.get(2).getNormalized());
	// }
	//
	// @Test
	// public void testTransposition2Gaps() {
	// final IWitness a = factory.createWitness("A", "d a b");
	// final IWitness b = factory.createWitness("B", "a c b d");
	// final IAlignment align = PairwiseAlignmentHelper.align(factory, a, b);
	// final List<IGap> gaps = align.getGaps();
	// assertEquals(1, gaps.size());
	// final IGap gap = gaps.get(0);
	// assertTrue(gap.isAddition());
	// assertEquals("c", gap.getPhrase().getNormalized());
	// }
	//
	// @Test
	// public void testTransposition1() {
	// final IWitness a = factory.createWitness("A", "d a b");
	// final IWitness b = factory.createWitness("B", "a b d");
	// final IAlignment align = PairwiseAlignmentHelper.align(factory, a, b);
	// final List<ITransposition> transpositions = align.getTranspositions();
	// assertEquals(2, transpositions.size());
	// assertEquals("d", transpositions.get(0).getMatchA().getNormalized());
	// assertEquals("a b", transpositions.get(0).getMatchB().getNormalized());
	// assertEquals("d", transpositions.get(1).getMatchB().getNormalized());
	// assertEquals("a b", transpositions.get(1).getMatchA().getNormalized());
	// }
	//
	// @Test
	// public void testTransposition2() {
	// final IWitness a = factory.createWitness("A", "d a b");
	// final IWitness b = factory.createWitness("B", "a c b d");
	// final IAlignment align = PairwiseAlignmentHelper.align(factory, a, b);
	// final List<ITransposition> transpositions = align.getTranspositions();
	// assertEquals(3, transpositions.size());
	// assertEquals("d", transpositions.get(0).getMatchA().getNormalized());
	// assertEquals("a", transpositions.get(0).getMatchB().getNormalized());
	// assertEquals("a", transpositions.get(1).getMatchA().getNormalized());
	// assertEquals("b", transpositions.get(1).getMatchB().getNormalized());
	// assertEquals("b", transpositions.get(2).getMatchA().getNormalized());
	// assertEquals("d", transpositions.get(2).getMatchB().getNormalized());
	// }
	//
	// @Test
	// public void testTransposition3() {
	// final IWitness a = factory.createWitness("1", "a b x c d e");
	// final IWitness b = factory.createWitness("2", "c e y a d b");
	// final IAlignment align = PairwiseAlignmentHelper.align(factory, a, b);
	// final List<ITransposition> transpositions = align.getTranspositions();
	// LOG.debug("transpositions=[" +
	// Joiner.on(", ").join(Iterables.transform(transpositions, new
	// Function<ITransposition, String>() {
	// @Override
	// public String apply(final ITransposition from) {
	// return from.getMatchA().getNormalized() + "=>" +
	// from.getMatchB().getNormalized();
	// }
	// })) + "]");
	// assertEquals(4, transpositions.size());
	// assertEquals("a", transpositions.get(0).getMatchA().getNormalized());
	// assertEquals("c", transpositions.get(0).getMatchB().getNormalized());
	// assertEquals("b", transpositions.get(1).getMatchA().getNormalized());
	// assertEquals("e", transpositions.get(1).getMatchB().getNormalized());
	// assertEquals("c", transpositions.get(2).getMatchA().getNormalized());
	// assertEquals("a", transpositions.get(2).getMatchB().getNormalized());
	// assertEquals("e", transpositions.get(3).getMatchA().getNormalized());
	// assertEquals("b", transpositions.get(3).getMatchB().getNormalized());
	// }
	//
}
