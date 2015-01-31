package eu.interedition.collatex.dekker;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.simple.SimpleWitness;

public class Dekker21AlignerTest extends AbstractTest {
	@Test
	public void testCaseDanielStoekl() {
	  // 1: a, b, c, d, e
	  // 2: a, e, c, d
	  // 3: a, d, b
		final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d", "a d b");
		Dekker21Aligner aligner = new Dekker21Aligner(w);
		//Note: the suffix array can have multiple forms
		//outcome of sorting is not guaranteed
		//however the LCP array is fixed we can assert that
		assertEquals("[-1, 1, 1, 0, 1, 0, 2, 0, 1, 1, 0, 1]", Arrays.toString(aligner.lCP_array));
	}
}
