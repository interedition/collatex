package de.tud.kom.stringutils.preprocessing;

/**
*
* This file is part of Shingle Cloud Library, Copyright (C) 2009 Arno Mittelbach, Lasse Lehmann
* 
* Shingle Cloud Library is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Shingle Cloud Library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with Shingle Cloud Library.  If not, see <http://www.gnu.org/licenses/>.
*
*
*/

import java.io.Serializable;
import java.util.HashSet;

/**
 * Performs case folding and stop wording.
 * 
 * @author Arno Mittelbach
 *
 */
public class StopWordRemoval implements Preprocess, Serializable {

	private static java.util.Set<String> stopwords = new HashSet<String>();
	static {
		stopwords.add("d");
		stopwords.add("ll");
		stopwords.add("s");
		stopwords.add("t");
		stopwords.add("ve");
		stopwords.add("a");
		stopwords.add("about");

		stopwords.add("above");

		stopwords.add("abroad");

		stopwords.add("across");
		stopwords.add("after");

		stopwords.add("again");

		stopwords.add("against");

		stopwords.add("ago");

		stopwords.add("ahead");
		stopwords.add("all");

		stopwords.add("almost");

		stopwords.add("alongside");
		stopwords.add("already");

		stopwords.add("also");

		stopwords.add("although");

		stopwords.add("always");

		stopwords.add("am");

		stopwords.add("amid");
		stopwords.add("amidst");
		stopwords.add("among");

		stopwords.add("amongst");
		stopwords.add("an");

		stopwords.add("and");

		stopwords.add("another");

		stopwords.add("any");

		stopwords.add("anybody");

		stopwords.add("anyone");

		stopwords.add("anything");

		stopwords.add("anywhere");

		stopwords.add("apart");
		stopwords.add("are");

		stopwords.add("aren't");

		stopwords.add("around");

		stopwords.add("as");

		stopwords.add("aside");
		stopwords.add("at");

		stopwords.add("away");

		stopwords.add("back");

		stopwords.add("be");

		stopwords.add("because");
		stopwords.add("been");

		stopwords.add("before");

		stopwords.add("behind");
		stopwords.add("being");

		stopwords.add("below");

		stopwords.add("between");

		stopwords.add("beyond");

		stopwords.add("both");

		stopwords.add("but");

		stopwords.add("by");

		stopwords.add("can");

		stopwords.add("can't");

		stopwords.add("cannot");

		stopwords.add("could");

		stopwords.add("couldn't");

		stopwords.add("dare");

		stopwords.add("daren't");

		stopwords.add("despite");

		stopwords.add("did");

		stopwords.add("didn't");

		stopwords.add("directly");

		stopwords.add("do");

		stopwords.add("does");

		stopwords.add("doesn't");

		stopwords.add("doing");

		stopwords.add("don't");

		stopwords.add("done");

		stopwords.add("down");

		stopwords.add("during");

		stopwords.add("each");

		stopwords.add("either");

		stopwords.add("else");

		stopwords.add("elsewhere");

		stopwords.add("enough");
		stopwords.add("even");

		stopwords.add("ever");

		stopwords.add("evermore");

		stopwords.add("every");

		stopwords.add("everybody");

		stopwords.add("everyone");

		stopwords.add("everything");

		stopwords.add("everywhere");

		stopwords.add("except");

		stopwords.add("fairly");
		stopwords.add("farther");

		stopwords.add("few");
		stopwords.add("fewer");

		stopwords.add("for");

		stopwords.add("forever");
		stopwords.add("forward");

		stopwords.add("from");

		stopwords.add("further");

		stopwords.add("furthermore");

		stopwords.add("had");

		stopwords.add("hadn't");

		stopwords.add("half");
		stopwords.add("hardly");
		stopwords.add("has");

		stopwords.add("hasn't");

		stopwords.add("have");

		stopwords.add("haven't");

		stopwords.add("having");

		stopwords.add("he");

		stopwords.add("hence");

		stopwords.add("her");

		stopwords.add("here");

		stopwords.add("hers");

		stopwords.add("herself");

		stopwords.add("him");

		stopwords.add("himself");

		stopwords.add("his");

		stopwords.add("how");

		stopwords.add("however");

		stopwords.add("if");

		stopwords.add("in");

		stopwords.add("indeed");

		stopwords.add("inner");
		stopwords.add("inside");
		stopwords.add("instead");

		stopwords.add("into");

		stopwords.add("is");

		stopwords.add("isn't");

		stopwords.add("it");

		stopwords.add("its");

		stopwords.add("itself");

		stopwords.add("just");

		stopwords.add("keep");

		stopwords.add("kept");

		stopwords.add("later");

		stopwords.add("least");

		stopwords.add("less");
		stopwords.add("lest");

		stopwords.add("like");

		stopwords.add("likewise");

		stopwords.add("may");

		stopwords.add("mayn't");

		stopwords.add("me");

		stopwords.add("might");

		stopwords.add("mightn't");

		stopwords.add("mine");

		stopwords.add("moreover");

		stopwords.add("most");
		stopwords.add("much");

		stopwords.add("must");

		stopwords.add("mustn't");

		stopwords.add("my");

		stopwords.add("myself");

		stopwords.add("needn't");

		stopwords.add("neither");

		stopwords.add("neverf");

		stopwords.add("neverless");

		stopwords.add("next");

		stopwords.add("no");

		stopwords.add("no-one");

		stopwords.add("nobody");

		stopwords.add("none");

		stopwords.add("nor");

		stopwords.add("not");

		stopwords.add("nothing");

		stopwords.add("notwithstanding");

		stopwords.add("now");

		stopwords.add("nowhere");

		stopwords.add("of");

		stopwords.add("off");

		stopwords.add("often");

		stopwords.add("on");

		stopwords.add("once");

		stopwords.add("one");

		stopwords.add("ones");

		stopwords.add("only");

		stopwords.add("onto");

		stopwords.add("opposite");

		stopwords.add("or");

		stopwords.add("other");

		stopwords.add("others");

		stopwords.add("otherwise");

		stopwords.add("ought");

		stopwords.add("oughtn't");

		stopwords.add("our");

		stopwords.add("ours");

		stopwords.add("ourselves");

		stopwords.add("out");

		stopwords.add("outside");

		stopwords.add("over");

		stopwords.add("own");

		stopwords.add("past");

		stopwords.add("per");

		stopwords.add("perhaps");

		stopwords.add("please");
		stopwords.add("plus");

		stopwords.add("provided");

		stopwords.add("quite");

		stopwords.add("rather");

		stopwords.add("really");

		stopwords.add("round");

		stopwords.add("same");

		stopwords.add("self");

		stopwords.add("selves");

		stopwords.add("several");

		stopwords.add("shall");

		stopwords.add("shan't");

		stopwords.add("she");

		stopwords.add("should");

		stopwords.add("shouldn't");

		stopwords.add("since");

		stopwords.add("so");

		stopwords.add("some");

		stopwords.add("somebody");

		stopwords.add("someday");

		stopwords.add("someone");

		stopwords.add("something");

		stopwords.add("sometimes");

		stopwords.add("somewhat");

		stopwords.add("still");

		stopwords.add("such");

		stopwords.add("than");
		stopwords.add("that");
		stopwords.add("the");

		stopwords.add("their");

		stopwords.add("theirs");

		stopwords.add("them");

		stopwords.add("themselves");

		stopwords.add("then");

		stopwords.add("there");

		stopwords.add("therefore");

		stopwords.add("these");

		stopwords.add("they");

		stopwords.add("thing");

		stopwords.add("things");

		stopwords.add("this");

		stopwords.add("those");

		stopwords.add("though");

		stopwords.add("through");

		stopwords.add("throughout");

		stopwords.add("thus");

		stopwords.add("till");

		stopwords.add("to");

		stopwords.add("together");

		stopwords.add("too");

		stopwords.add("towards");

		stopwords.add("under");

		stopwords.add("underneath");

		stopwords.add("undoing");

		stopwords.add("unless");

		stopwords.add("unlike");

		stopwords.add("until");

		stopwords.add("up");

		stopwords.add("upon");

		stopwords.add("upwards");

		stopwords.add("us");

		stopwords.add("versus");

		stopwords.add("very");

		stopwords.add("via");

		stopwords.add("was");

		stopwords.add("wasn't");

		stopwords.add("way");

		stopwords.add("we");

		stopwords.add("were");

		stopwords.add("weren't");

		stopwords.add("what");

		stopwords.add("whatever");

		stopwords.add("when");

		stopwords.add("whence");

		stopwords.add("whenever");

		stopwords.add("where");

		stopwords.add("whereby");

		stopwords.add("wherein");

		stopwords.add("wherever");

		stopwords.add("whether");

		stopwords.add("which");

		stopwords.add("whichever");

		stopwords.add("while");

		stopwords.add("whilst");

		stopwords.add("whither");

		stopwords.add("who");

		stopwords.add("whoever");

		stopwords.add("whom");

		stopwords.add("whose");

		stopwords.add("why");

		stopwords.add("will");

		stopwords.add("with");

		stopwords.add("within");

		stopwords.add("without");

		stopwords.add("won't");

		stopwords.add("would");

		stopwords.add("wouldn't");

		stopwords.add("yet");

		stopwords.add("you");

		stopwords.add("your");

		stopwords.add("yours");

		stopwords.add("yourself");

		stopwords.add("yourselves");
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4733578465599606320L;

	public String preprocessInput(String input) {
		String output = input.toLowerCase();
		
		StringBuffer buf = new StringBuffer();
		for(String w : output.split(" "))
			if(! stopwords.contains(w))
				buf.append(" ").append(w);
		
		String out = buf.toString();
		return out.length() > 0 ? out.substring(1) : out;
	}

}
