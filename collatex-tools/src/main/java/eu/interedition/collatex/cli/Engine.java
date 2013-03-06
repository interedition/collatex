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
  List<URL> witnessResources;
  List<URLWitness> witnesses;
  XPathExpression tokenXPath;

  Function<String, Iterable<String>> tokenizer;
  Function<String, String> normalizer;
  Comparator<Token> comparator;
  CollationAlgorithm collationAlgorithm;
  VariantGraph variantGraph;
  boolean joined = false;

  String outputFormat;
  PrintWriter out;
  File outFile = null;
  PrintWriter log = new PrintWriter(System.out);
  boolean errorOccurred = false;

  Engine configure(CommandLine commandLine) throws XPathExpressionException, ParseException {
    if (commandLine.hasOption("s")) {
      System.out.println(Arrays.asList(commandLine.getOptionValues("s")));
    }

    this.inputCharset = Charset.forName(commandLine.getOptionValue("ie", "UTF-8"));
    this.xmlMode = commandLine.hasOption("xml");
    this.tokenXPath = XPathFactory.newInstance().newXPath().compile(commandLine.getOptionValue("xp", "//w"));

    this.tokenizer = SimplePatternTokenizer.BY_WS_AND_PUNCT;
    this.normalizer = SimpleTokenNormalizers.LC_TRIM_WS_PUNCT;
    this.comparator = new EqualityTokenComparator();

    final String algorithm = commandLine.getOptionValue("a", "dekker").toLowerCase();
    if ("needleman-wunsch".equals(algorithm)) {
      this.collationAlgorithm = CollationAlgorithmFactory.needlemanWunsch(this.comparator);
    } else if ("medite".equals(algorithm)) {
      this.collationAlgorithm = CollationAlgorithmFactory.medite(this.comparator);
    } else {
      this.collationAlgorithm = CollationAlgorithmFactory.dekker(this.comparator);
    }

    this.variantGraph = new JungVariantGraph();

    this.joined = !commandLine.hasOption("t");

    this.outputFormat = commandLine.getOptionValue("f", "tei").toLowerCase();
    this.out = new PrintWriter(System.out);

    final String[] witnessSpecs = commandLine.getArgs();
    this.witnessResources = Lists.newArrayListWithExpectedSize(witnessSpecs.length);
    for (String witnessSpec : witnessSpecs) {
      witnessResources.add(argumentToResource(witnessSpec));
    }
    if (witnessResources.size() < 2) {
      throw new ParseException("At least 2 witnesses must be given");
    }

    this.witnesses = Lists.newArrayListWithExpectedSize(witnessResources.size());
    for (URL witnessURL : witnessResources) {
      this.witnesses.add(new URLWitness("w" + (witnesses.size() + 1), witnessURL));
    }

    return this;
  }

  Engine read() throws IOException, XPathExpressionException {
    for (URLWitness witness : witnesses) {
      witness.read(tokenizer, normalizer, inputCharset, (xmlMode ? tokenXPath : null));
    }
    return this;
  }

  Engine collate() {
    for (SimpleWitness witness : witnesses) {
      collationAlgorithm.collate(variantGraph, witness);
    }
    if (joined) {
      VariantGraph.JOIN.apply(variantGraph);
    }
    return this;
  }

  void write() throws IOException {
    final SimpleVariantGraphSerializer serializer = new SimpleVariantGraphSerializer(variantGraph);
    if ("csv".equals(outputFormat)) {
      serializer.toCsv(out);
    } else if ("dot".equals(outputFormat)) {
      serializer.toDot(out);
    } else if("graphml".equals(outputFormat)) {
      serializer.toGraphML(out);
    } else {
      serializer.toTEI(out);
    }
  }

  Engine log(String str) {
    log.write(str);
    return this;
  }

  void error(String str, Throwable t) {
    errorOccurred = true;
    log("Error: ").log(str).log("\n").log(t.getMessage()).log("\n");
  }

  void help() {
    new HelpFormatter().printHelp(log, 78, "collatex [<options>] <witness_1> <witness_2> [[<witness_3>] ...]", "", OPTIONS, 2, 4, "");
  }

  URL argumentToResource(String arg) throws ParseException {
    try {
      final File witnessFile = new File(arg);
      if (witnessFile.exists()) {
        return witnessFile.toURI().normalize().toURL();
      } else {
         return new URL(arg);
      }
    } catch (MalformedURLException urlEx) {
      throw new ParseException("Invalid resource specified: " + arg);
    }
  }

  public static void main(String... args) {
    final Engine engine = new Engine();
    try {
      final CommandLine commandLine = new GnuParser().parse(OPTIONS, args);
      if (commandLine.hasOption("h")) {
        engine.help();
        return;
      }
      engine.configure(commandLine).read().collate().write();
    } catch (ParseException e) {
      engine.error("Command Line Arguments Incorrect", e);
      engine.log("\n").help();
    } catch (IllegalArgumentException e) {
      engine.error("Illegal Argument", e);
    } catch (IOException e) {
      engine.error("I/O Error", e);
    } catch (XPathExpressionException e) {
      engine.error("XPath Error", e);
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
    OPTIONS.addOption("t", "tokenized", false, "consecutive matches of tokens will *not* be joined to segments");
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
    if (errorOccurred && (outFile != null) && outFile.isFile()) {
      outFile.delete();
    }
  }
}
