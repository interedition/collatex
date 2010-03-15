package eu.interedition.collatex2.alignment;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTest {
	private Factory factory;

	@Before
	public void setup() {
		factory = new Factory();
	}
	
	@Test
	public void testSimple1() {
		IWitness a = factory.createWitness("A", "a b");
		IWitness b = factory.createWitness("B", "a c b");
		IAlignment ali = factory.createAlignment(a, b);
		List<IMatch> matches = ali.getMatches();
		Assert.assertEquals(2, matches.size());
		Assert.assertEquals("a", matches.get(0).getNormalized());
		Assert.assertEquals("b", matches.get(1).getNormalized());
		//TODO: add asserts for gap!
	}
}
