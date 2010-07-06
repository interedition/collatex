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

//package com.sd_editions.collatex.output;
//
//import java.util.Collection;
//
//import com.sd_editions.collatex.permutations.Witness;
//import com.sd_editions.collatex.permutations.Word;
//
//public class MatchColumn extends Column {
//
//  private final Word matchedWord;
//
//  public MatchColumn(Word _matchedWord) {
//    this.matchedWord = _matchedWord;
//  }
//
//  @Override
//  public void toXML(StringBuilder builder) {
//    builder.append(matchedWord.original);
//  }
//
//  @Override
//  public Word getWord(Witness witness) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public void addMatch(Witness witness, Word word) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public boolean containsWitness(Witness witness) {
//    throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public Collection<Word> getWords() {
//    throw new UnsupportedOperationException();
//  }
//
//}
