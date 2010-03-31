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
