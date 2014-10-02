package de.tud.kom.stringmatching.gst.utils;

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

import de.tud.kom.stringmatching.gst.GST;
import de.tud.kom.stringmatching.gst.GSTTile;
import de.tud.kom.stringutils.preprocessing.DummyPreprocess;
import de.tud.kom.stringutils.preprocessing.Preprocess;
import de.tud.kom.stringutils.tokenization.Tokenizer;
import de.tud.kom.stringutils.tokenization.WordTokenizer;

/**
 * A utility class that allows you to highlight the results of a GST comparison.
 * 
 * <p>
 * Matching tiles will be wrapped with custom tags (by default &lt;span class=\"highlighted\"&gt;).
 * If the matched text was XML you should use the {@link XMLHighlighter} instead as it makes sure
 * to properly nest tags.
 * </p>
 * 
 * @author Arno Mittelbach
 * @see XMLHighlighter
 * @see GST
 */
public class GSTHighlighter {

	protected Preprocess preprocessor = new DummyPreprocess();
	protected Tokenizer tokenizer = new WordTokenizer();
	
	private int minimumTileLength = 3;
	private String openingDelimiter = "<span class=\"highlighted\">";
	private String closingDelimiter = "</span>";

	public GSTHighlighter() {
		super();
	}

	public GSTHighlighter(Preprocess preprocessor){
		setPreprocessor(preprocessor);
	}
	
	public String produceHighlightedText(String haystack, String needle) {
		StringBuilder highlightOutput = new StringBuilder();;
		
		// instantiat gst
		GST gst = createGST(haystack);
		gst.match(needle);
		
		int last = 0;
		for(GSTTile tile : gst.getTiles()){
			// for left column
			last = addTile(last, haystack, tile, gst.getTokenizedHaystack(), highlightOutput);
		}
		
		// add last part
		addTile(last, haystack, null, gst.getTokenizedHaystack(), highlightOutput);
		
		return highlightOutput.toString().trim();
	}

	protected GST createGST(String haystack) {
		GST gst = new GST(haystack);
		gst.setMinimumTileLength(minimumTileLength);
		gst.setPreprocessingAlgorithm(preprocessor);
		gst.setTokenizer(tokenizer);
		
		return gst;
	}

	protected int addTile(int last, String text, GSTTile tile,
			String[] tokens, StringBuilder highlightOutput) {
		StringBuilder textToAdd = new StringBuilder();
		int end = (null != tile) ? tile.getStart(): tokens.length;
		
		while(last < end){
			try{
				textToAdd.append(" ")
					     .append(tokens[last]);
			} catch(IndexOutOfBoundsException e){}
			last++;
		}
		
		// add highlighted stuff?
		if(null != tile){
			textToAdd.append(" ")
					 .append(openingDelimiter)
					 .append(tile.getText())
					 .append(closingDelimiter);
	
			last += tile.getLength();
		}
	
		
		// add text to static variable
		highlightOutput.append(textToAdd);
		
		return last;
	}

	public Preprocess getPreprocessor() {
		return preprocessor;
	}

	public void setPreprocessor(Preprocess preprocessor) {
		this.preprocessor = preprocessor;
	}

	public int getMinimumTileLength() {
		return minimumTileLength;
	}

	public void setMinimumTileLength(int minimumTileLength) {
		this.minimumTileLength = minimumTileLength;
	}

	public String getOpeningDelimiter() {
		return openingDelimiter;
	}

	public void setOpeningDelimiter(String openingDelimiter) {
		this.openingDelimiter = openingDelimiter;
	}

	public String getClosingDelimiter() {
		return closingDelimiter;
	}

	public void setClosingDelimiter(String closingDelimiter) {
		this.closingDelimiter = closingDelimiter;
	}

	public Tokenizer getTokenizer() {
		return tokenizer;
	}

	public void setTokenizer(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}
	
	

}