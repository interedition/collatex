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
//import java.util.Arrays;
//import java.util.List;
//
//import com.sd_editions.collatex.permutations.NonMatch;
//import com.sd_editions.collatex.permutations.Word;
//
//public class MagicTable {
//  private final Column[] columnsArr;
//
//  public MagicTable() {
//    columnsArr = new Column[100]; // FIXME use maximum number of words per witness -- but need witness here
//
//  }
//
//  public String toXML() {
//    StringBuilder builder = new StringBuilder();
//    builder.append("<xml>");
//    List<Column> columns = Arrays.asList(columnsArr);
//    String separator = "";
//    for (Column column : columns) {
//      if (column != null) { // TODO filter away empty columns
//        // TODO in a separate step!
//        builder.append(separator);
//        column.toXML(builder);
//        separator = " ";
//      }
//    }
//    builder.append("</xml>");
//    return builder.toString();
//  }
//
//  public void setMatch(Word matchedWord) {
//    // TODO to decide here that it is a match column is far too soon!
//    columnsArr[matchedWord.position * 2 + 1] = new MatchColumn(matchedWord);
//  }
//
//  public void setNonMatch(NonMatch nonMatch) {
//    // I probably need to convert the non matches into words here.. and put them in separate columns
//    // TODO to decide here that it is a match column is far too soon!
//
//    columnsArr[nonMatch.getBase().getStartPosition() * 2] = new AppColumn(nonMatch.getBase(), nonMatch.getWitness());
//    //  // FIXME somehow propagate Bram's witness id here after merge
//
//  }
//}
