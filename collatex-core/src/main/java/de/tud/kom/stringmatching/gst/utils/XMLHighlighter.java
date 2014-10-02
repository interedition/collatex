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
import de.tud.kom.stringutils.preprocessing.Preprocess;

/**
 * A utility class that allows you to highlight the results of a GST XML comparison (see {@link GST#useXMLMode()}).
 * 
 * <p>
 * Matching tiles will be wrapped with custom tags (by default &lt;span class=\"highlighted\"&gt;)
 * and if the text contains xml tags it is made sure to use proper nesting.
 * </p>
 * 
 * @author Arno Mittelbach
 * @see GSTHighlighter
 * @see GST
 */
public class XMLHighlighter extends GSTHighlighter {

	/**
	 * Create a new XML Highlighter with default processing.  
	 */
	public XMLHighlighter(){
	}

	/**
	 * Create a new XML Highlighter with the specified processing.  
     *
	 * @param preprocessor
	 */
	public XMLHighlighter(Preprocess preprocessor){
		super(preprocessor);
	}
	
	@Override
	protected GST createGST(String haystack) {
		GST gst = super.createGST(haystack);
		gst.useXMLMode();
		
		return gst;
	}
	
	@Override
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
			textToAdd.append(" ").append(getOpeningDelimiter());
			int mode = 0;
			String tmp = "";
			for(char c : tile.getText().toCharArray()){
				switch(mode){
				case 0:  // nothing happened
					if(c == '<'){
						mode = 1;
						last++; // we found a tag so we have to add one as tags are not included in the tile's length
					} else
						textToAdd.append(c);
					break;
					
				case 1: // we might see an opening or closing tag
					if( c != '>')
						tmp += c;
					else {
						mode = 0;
						textToAdd.append(getClosingDelimiter())
								 .append("<")
						         .append(tmp)
						         .append(">")
						         .append(getOpeningDelimiter());
						tmp = "";
					}
					break;
				}
			}
			textToAdd.append(getClosingDelimiter());
	
			last += tile.getLength();
		}
	
		
		// add text to static variable
		highlightOutput.append(textToAdd);
	
		
		return last;
	}
	
}
