package de.tud.kom.stringmatching.shinglecloud;

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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import de.tud.kom.stringmatching.shinglecloud.Shingle.ShingleType;
import de.tud.kom.stringutils.preprocessing.DummyPreprocess;
import de.tud.kom.stringutils.preprocessing.Preprocess;
import de.tud.kom.stringutils.tokenization.Tokenizer;
import de.tud.kom.stringutils.tokenization.WordTokenizer;



/**
 * Groups shingles together and provides access to the list of shingles.
 * The ShingleList allows to iterate over the set of shingles in the order
 * that they were added to the list.
 * 
 * @author Arno Mittelbach
 *
 */
public class ShingleList implements Iterable<Shingle> {

	/**
	 * Used for internal processing to determine what has been added to this list.
	 * 
	 * @author Arno Mittelbach
	 */
	private enum ShingleListShingleType{
		Shingle,
		Text,
		Group,
		GroupBegin,
		GroupEnd,
		MagicMatcher
	}
	
	/**
	 * Determines whether we are currently processing a group.
	 * @see beginShingleGroup 
	 */
	private int inGroup = 0;
	
	/**
	 * Stores the preprocessing algorithm that is used in this ShingleList
	 */
	private Preprocess preprocessingAlgorithm;
	
	private Tokenizer tokenizer = new WordTokenizer();
	
	/**
	 * Stores the n-gram size that this object uses
	 */
	private int nGramSize = 3;
	
	/**
	 * Stores the shingles that were added to this shingle set
	 */
	private List<Shingle> shingleList = new ArrayList<Shingle>();
	
	/**
	 * Stores a list with unprocessed content. This can either be Shingles, Strings or an Object array.
	 */
	private List<Object[]> unprocessedContentList = new LinkedList<Object[]>();
	
	/**
	 * Tells whether or not this ShingleList has been compiled
	 */
	private boolean compiled = false;

	private Set<String> magicWords = new HashSet<String>();
	
	/**
	 * Creates a new ShingleList with no preprocessing algorithm.
	 */
	public ShingleList() {
		preprocessingAlgorithm = new DummyPreprocess();
	}
	
	/**
	 * Creates a new ShingleList with a specified preprocessing algorithm.
	 * 
	 * <p>
	 * Every input text passed to this ShingleList is first preprocessed
	 * using the specified algorithm.
	 * </p>
	 * 
	 * 
	 * @param preprocessingAlgorithm
	 */
	public ShingleList(Preprocess preprocessAlgorithm) {
		this.preprocessingAlgorithm = preprocessAlgorithm;
	}
	
	/**
	 * Adds a shingle to the list of shingles
	 * 
	 * @param shingle
	 * 
	 * @throws IllegalStateException If the object has already been compiled no new shingles can be added.
	 */
	public void addShingle(Shingle shingle) {
		if(isCompiled())
			throw new IllegalStateException();
		
		unprocessedContentList.add(new Object[]{ShingleListShingleType.Shingle,  shingle});
	}
	
	/**
	 * Adds a Magic shingle that matches anything
	 * {@link ShingleType}
	 */
	public void addMagicMatcher() {
		if(isCompiled())
			throw new IllegalStateException();
		
		unprocessedContentList.add(new Object[]{ShingleListShingleType.MagicMatcher});
	}
	
	/**
	 * Adds some text that is to be shingled and added to this list of shingles.
	 * @param text
	 * 
	 * @throws IllegalStateException If the object has already been compiled no new shingles can be added.
	 */
	public void addTextToShingle(String text)
	{
		if(isCompiled())
			throw new IllegalStateException("List has already been compiled.");
		
		unprocessedContentList.add(new Object[]{ShingleListShingleType.Text, text});
	}
	
	/**
	 * Adds some text that is to be shingled and that will be wrapped by two group shingles.
	 * 
	 * @param input The text that is enclosed in the newly created group.
	 * @param groupID The id the group will be identified with.
	 * 
	 * @throws IllegalStateException If the object has already been compiled no new shingles can be added.
	 */
	public void addShingleGroup(String input, String groupID) {
		if(isCompiled())
			throw new IllegalStateException();
		
		unprocessedContentList.add(new Object[]{ShingleListShingleType.Group,  input, groupID});
	}
	
	/**
	 * Starts a new shingle group identified with the specified id.
	 * @param groupID The id the group will be identified with.
	 */
	public void beginShingleGroup(String groupID){
		if(isCompiled())
			throw new IllegalStateException();
	
		unprocessedContentList.add(new Object[]{ShingleListShingleType.GroupBegin, groupID});
		
		inGroup++;
	}
	
	/**
	 * Ends a shingle group.
	 */
	public void endShingleGroup(){
		if(isCompiled())
			throw new IllegalStateException();

		if(inGroup <= 0)
			throw new IllegalStateException("A group has not been opened.");
		
		unprocessedContentList.add(new Object[]{ShingleListShingleType.GroupEnd});
		
		inGroup--;
	}
	
	/**
	 * Converts the given information to a list of shingles.
	 * 
	 * <p>
	 * After compile has been called no new shingles can
	 * be added to this object.
	 * </p>
	 * <p>
	 * This method should usually not be called directly.
	 * </p>
	 * <p>
	 * All nested groups have to be closed before the ShingleList can be compiled!
	 * </p>
	 */
	public void compile() {
		if(inGroup != 0)
			throw new IllegalStateException("Uneven number of group shingles.");
		
		// used to store nested group names
		Stack<String> nestedGroups = new Stack<String>();
		
		// used to keep track of the current text that we have to shingle if the time is right
		String textToShingle = "";
		
		// loop over unprocessed contents
		while(unprocessedContentList.size() > 0)
		{
			// remove item from start of list
			Object[] uc = unprocessedContentList.remove(0);
			
			// if type is not Text we shingle the accumulated text
			if( uc[0] != ShingleListShingleType.Text && !textToShingle.equals("")){
				// shingle stored text and reset it
				shingleText(textToShingle);
				textToShingle = "";
			}
			
			// perform specific action
			if (uc[0] == ShingleListShingleType.Group) { // we have a named group
				// add beginning
				Shingle start = new Shingle(ShingleType.GroupStart, (String) uc[2]);
				shingleList.add(start);
				
				// add text
				shingleText((String)uc[1]);
			
				// add end
				Shingle end = new Shingle(ShingleType.GroupEnd, (String) uc[2]);
				shingleList.add(end);
			
			} else if(uc[0] == ShingleListShingleType.GroupBegin) { // we have to begin a nested Group
				// add shingle and push id on the stack
				shingleList.add(new Shingle(ShingleType.GroupStart, nestedGroups.push((String) uc[1])));
				
			} else if(uc[0] == ShingleListShingleType.GroupEnd) { // we have to end the last nested group
				// add shingle and pop group id from stack
				shingleList.add(new Shingle(ShingleType.GroupEnd, nestedGroups.pop())); 

			} else if (uc[0] == ShingleListShingleType.Text) { // we have simple input
				textToShingle += ((String) uc[1]).trim();
				
			} else if (uc[0] == ShingleListShingleType.Shingle)  { // we have a single shingle
				shingleList.add((Shingle) uc[1]);
			} else if (uc[0] == ShingleListShingleType.MagicMatcher) {
				shingleList.add(new Shingle(ShingleType.MagicMatcher));
			}
		}
		
		// there might be some text left, so
		// shingle stored text and reset it
		shingleText(textToShingle);
		textToShingle = "";
		
		// set compiled flag to true
		compiled = true;
		
		// free memory
		unprocessedContentList.clear();
	}
	
	/**
	 * 
	 * @return
	 */
	public int size(){
		return shingleList.size();
	}
	
	
	/**
	 * Preprocesses  the input string and creates shingles from it.
	 * @param string
	 */
	private void shingleText(String input) {
		String preprocessed = preprocessingAlgorithm.preprocessInput(input);
		
		String[] words = tokenizer.tokenize(preprocessed);
		
		for(int i = 0; i < words.length - nGramSize + 1; i++){
			boolean magic = false;
			String[] items = new String[nGramSize];
			for(int j = 0; j < nGramSize; j++){
				items[j] = words[i+j];
				magic = magic || isMagicWord(items[j]);
			}
			
			if(! magic){
				Shingle shingle = new Shingle(items);
				shingleList.add(shingle);
			} else
				shingleList.add(new Shingle(ShingleType.MagicMatcher));
		}
	}

	/**
	 * Tests a word whether it was used defined as magic.
	 * @param word
	 * @return
	 */
	private boolean isMagicWord(String word) {
		return magicWords.contains(word);
	}

	/**
	 * 
	 * @return Whether or not this ShingleList has been compiled.
	 */
	public boolean isCompiled() {
		return this.compiled;
	}
	
	/**
	 * 
	 * @return The preprocessing algorithm used
	 */
	public Preprocess getPreprocessingAlgorithm() {
		return preprocessingAlgorithm;
	}

	/**
	 * Can be used to set the preprocessing algorithm unless this object has already been compiled.
	 * @param preprocessingAlgorithm
	 * @throws IllegalStateException The preprocessing algorithm cannot be set if the object was already compiled
	 */
	public void setPreprocessingAlgorithm(Preprocess preprocessingAlgorithm) {
		if(isCompiled())
			throw new IllegalStateException();
		
		this.preprocessingAlgorithm = preprocessingAlgorithm;
	}

	/**
	 * 
	 * @return The n-gram size this object uses.
	 */
	public int getNGramSize() {
		return nGramSize;
	}

	/**
	 * Can be used to define the n-gram size that should be used by this object.
	 * @param gramSize
	 * @throws IllegalStateException The n-gram size cannot be changed after the object has been compiled. 
	 */
	public void setNGramSize(int gramSize) {
		if(isCompiled())
			throw new IllegalStateException();
		
		nGramSize = gramSize;
	}
		
	public Iterator<Shingle> iterator() {
		return shingleList.iterator();
	}

	/**
	 * Returns the shingle at position i.
	 * @param i The shingle's position.
	 * @return The shingle at position i.
	 */
	public Shingle get(int i) {
		return shingleList.get(i);
	}
	
	/**
	 * Replaces the currently set list of magic words with the submitted words.
	 * @param words
	 */
	public void setMagicWords(String[] words) {
		if(! (getPreprocessingAlgorithm() instanceof Preprocess))
			throw new IllegalStateException("No preprocessing algorithm defined");
		
		/* clear current list */
		magicWords.clear();
		
		/* preprocess words and add them to the list */
		for(int i = 0; i < words.length; i++){
			String preprocessed = getPreprocessingAlgorithm().preprocessInput(words[i]);
			magicWords.add(preprocessed);
		}
	}

	/**
	 * Returns the tokenizer used when shingling text.
	 * @return
	 */
	public Tokenizer getTokenizer() {
		return tokenizer;
	}

	/**
	 * Sets the tokenizer to be used, when shingling text.
	 * @param tokenizer
	 */
	public void setTokenizer(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}


	public String toString(){
		StringBuilder s = new StringBuilder();
		
		/* add shingles */
		for(Shingle sh : shingleList){
			if(sh.isBasicShingle())
				s.append(sh.getItems()[0] + " ");
			else if(sh.isGroupBegin())
				s.append("[[ ");
			else if(sh.isGroupEnd())
				s.append("]] ");
			else if(sh.isMagicMatcher())
				s.append("[MAGIC_MATCHER] ");
		}
		
		/* add the last one if it is a basic shingle */
		Shingle lastShingle = shingleList.get(shingleList.size() - 1);
		if(lastShingle.isBasicShingle()){
			boolean first = true;
			for(String i : lastShingle.getItems()){
				if(first)
					first = false;
				else
					s.append(i + " ");
			}
		}
		
		return s.toString();
	}

	public int getNumberOfTokens() {
		return size() + getNGramSize() - 1;
	}

}
