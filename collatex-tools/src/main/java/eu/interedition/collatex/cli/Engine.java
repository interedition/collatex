/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.cli;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.jung.JungVariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimplePatternTokenizer;
import eu.interedition.collatex.simple.SimpleTokenNormalizers;
import eu.interedition.collatex.simple.SimpleVariantGraphSerializer;
import eu.interedition.collatex.simple.SimpleWitness;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Engine implements Closeable {

  Charset inputCharset;
  boolean xmlMode;
  List<URL> witnessURLs;
  List<URLWitness> witnesses;
  XPathExpression tokenXPath;
  Function<String, Iterable<String>> tokenizer;
  Function<String, String> normalizer;
  Comparator<Token> comparator;
  CollationAlgorithm collationAlgorithm;
  VariantGraph variantGraph;
  PrintWriter out;
  File outFile = null;
  PrintWriter log = new PrintWriter(System.out);
  boolean errorState;

  boolean configure(CommandLine commandLine) throws XPathExpressionException, ParseException {
    if (commandLine.hasOption("s")) {
      System.out.println(Arrays.asList(commandLine.getOptionValues("s")));
    }

    if (commandLine.hasOption("h")) {
      return false;
    }

    this.inputCharset = Charset.forName(commandLine.getOptionValue("ie", "UTF-8"));
    this.xmlMode = commandLine.hasOption("xml");
    this.tokenXPath = XPathFactory.newInstance().newXPath().compile(commandLine.getOptionValue("xp", "//w"));

    this.tokenizer = SimplePatternTokenizer.BY_WS_AND_PUNCT;
    this.normalizer = SimpleTokenNormalizers.LC_TRIM_WS_PUNCT;
    this.comparator = new EqualityTokenComparator();
    this.collationAlgorithm = CollationAlgorithmFactory.medite(this.comparator);

    this.variantGraph = new JungVariantGraph();

    this.out = new PrintWriter(System.out);

    final String[] witnessSpecs = commandLine.getArgs();
    this.witnessURLs = Lists.newArrayListWithExpectedSize(witnessSpecs.length);
    for (String witnessSpec : witnessSpecs) {
      try {
        witnessURLs.add(new URL(witnessSpec));
      } catch (MalformedURLException urlEx) {
        try {
          witnessURLs.add(new File(witnessSpec).toURI().normalize().toURL());
        } catch (MalformedURLException fileUrlEx) {
          throw new ParseException("Invalid witness: " + witnessSpec);
        }
      }
    }
    if (witnessURLs.size() < 2) {
      throw new ParseException("At least 2 witnesses must be given to compare");
    }
    this.witnesses = Lists.newArrayListWithExpectedSize(witnessURLs.size());

    return true;
  }

  Engine read() throws IOException, XPathExpressionException {
    for (URL witnessURL : witnessURLs) {
      witnesses.add(new URLWitness("w" + (witnesses.size() + 1), witnessURL));
    }
    for (URLWitness witness : witnesses) {
      witness.read(tokenizer, normalizer, inputCharset, (xmlMode ? tokenXPath : null));
    }
    return this;
  }

  Engine collate() {
    for (SimpleWitness witness : witnesses) {
      collationAlgorithm.collate(variantGraph, witness);
    }
    return this;
  }

  void write() throws IOException {
    new SimpleVariantGraphSerializer(VariantGraph.JOIN.apply(variantGraph)).toCsv(out);
  }

  Engine log(String str) {
    log.write(str);
    return this;
  }

  void error(String str, Throwable t) {
    errorState = true;
    log("Error: <").log(str).log(">\n").log(t.getMessage()).log("\n");
  }

  void help() {
    new HelpFormatter().printHelp(log, 78, "collatex [<options>] <witness_1> <witness_2> [[<witness_3>] ...]", "", OPTIONS, 2, 4, "");
  }

  public static void main(String... args) {
    final Engine engine = new Engine();
    try {
      if (engine.configure(new GnuParser().parse(OPTIONS, args))) {
        engine.read().collate().write();
        return;
      }
      engine.help();
    } catch (ParseException e) {
      engine.error("Command line error", e);
    } catch (IllegalArgumentException e) {
      engine.error("Illegal Argument", e);
    } catch (IOException e) {
      engine.error("I/O error", e);
    } catch (XPathExpressionException e) {
      engine.error("XPath error", e);
    } finally {
      Closeables.closeQuietly(engine);
    }
  }

  static final Options OPTIONS = new Options();

  static {
    OPTIONS.addOption("h", "help", false, "print usage instructions (which your are looking at right now)");
    OPTIONS.addOption("o", "output", true, "output file; '-' for standard output (default)");
    OPTIONS.addOption("ie", "input-encoding", true, "charset to use for decoding non-XML witnesses; default: UTF-8");
    OPTIONS.addOption("oe", "output-encoding", true, "charset to use for encoding the output; default: UTF-8");
    OPTIONS.addOption("xml", "xml-mode", false, "witnesses are treated as XML documents");
    OPTIONS.addOption("xp", "xpath", true, "XPath 1.0 expression evaluating to tokens of XML witnesses; default: '//w'");
    OPTIONS.addOption("a", "algorithm", true, "progressive alignment algorithm to use 'dekker' (default), 'medite', 'needleman-wunsch'");
    OPTIONS.addOption("f", "format", true, "result/output format: 'csv', 'dot', 'graphml', 'tei'");
    OPTIONS.addOption("s", "script", true, "ECMA/JavaScript resource with functions to be plugged into the alignment algorithm");
  }

  @Override
  public void close() throws IOException {
    try {
      if (out != null) {
        out.flush();
      }
      if (log != null) {
        log.flush();
      }
    } finally {
      Closeables.closeQuietly(out);
      Closeables.closeQuietly(log);
    }
    if (errorState && (outFile != null) && outFile.isFile()) {
      outFile.delete();
    }
  }
}
