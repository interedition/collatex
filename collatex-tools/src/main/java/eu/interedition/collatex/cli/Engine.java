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
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.io.CollateXModule;
import eu.interedition.collatex.io.Collation;
import eu.interedition.collatex.jung.JungVariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimplePatternTokenizer;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleTokenNormalizers;
import eu.interedition.collatex.simple.SimpleVariantGraphSerializer;
import eu.interedition.collatex.simple.SimpleWitness;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.xml.sax.SAXException;

import javax.script.ScriptException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Engine implements Closeable {

  Charset inputCharset;
  boolean xmlMode;
  List<URL> inputResources;
  List<SimpleWitness> witnesses;
  XPathExpression tokenXPath;

  Function<String, Iterable<String>> tokenizer;
  Function<String, String> normalizer;
  Comparator<Token> comparator;
  CollationAlgorithm collationAlgorithm;
  VariantGraph variantGraph;
  boolean joined = false;

  String outputFormat;
  Charset outputCharset;
  PrintWriter out;
  File outFile = null;
  PrintWriter log = new PrintWriter(System.err);
  boolean errorOccurred = false;

  ObjectMapper objectMapper = new ObjectMapper().withModule(new CollateXModule());

  Engine configure(CommandLine commandLine) throws XPathExpressionException, ParseException, ScriptException, IOException {
    this.inputCharset = Charset.forName(commandLine.getOptionValue("ie", "UTF-8"));
    this.xmlMode = commandLine.hasOption("xml");
    this.tokenXPath = XPathFactory.newInstance().newXPath().compile(commandLine.getOptionValue("xp", "//text()"));

    final String script = commandLine.getOptionValue("s");
    try {
      final PluginScript pluginScript = (script == null
              ? PluginScript.read("<internal>", new StringReader(""))
              : PluginScript.read(argumentToResource(script)));

      this.tokenizer = Objects.firstNonNull(pluginScript.tokenizer(), SimplePatternTokenizer.BY_WS_AND_PUNCT);
      this.normalizer = Objects.firstNonNull(pluginScript.normalizer(), SimpleTokenNormalizers.LC_TRIM_WS_PUNCT);
      this.comparator = Objects.firstNonNull(pluginScript.comparator(), new EqualityTokenComparator());
    } catch (IOException e) {
      throw new ParseException("Failed to read script '" + script + "' - " + e.getMessage());
    }

    final String algorithm = commandLine.getOptionValue("a", "dekker").toLowerCase();
    if ("needleman-wunsch".equals(algorithm)) {
      this.collationAlgorithm = CollationAlgorithmFactory.needlemanWunsch(this.comparator);
    } else if ("medite".equals(algorithm)) {
      this.collationAlgorithm = CollationAlgorithmFactory.medite(this.comparator, SimpleToken.TOKEN_MATCH_EVALUATOR);
    } else {
      this.collationAlgorithm = CollationAlgorithmFactory.dekker(this.comparator);
    }

    this.variantGraph = new JungVariantGraph();

    this.joined = !commandLine.hasOption("t");

    this.outputFormat = commandLine.getOptionValue("f", "json").toLowerCase();

    outputCharset = Charset.forName(commandLine.getOptionValue("oe", "UTF-8"));
    final String output = commandLine.getOptionValue("o", "-");
    if (!"-".equals(output)) {
      try {
        this.outFile = new File(output);
        this.out = new PrintWriter(Files.newWriter(this.outFile, outputCharset));
      } catch (FileNotFoundException e) {
        throw new ParseException("Output file '" + outFile + "' not found");
      }
    } else {
      this.out = new PrintWriter(new OutputStreamWriter(System.out, outputCharset));
    }


    final String[] witnessSpecs = commandLine.getArgs();
    this.inputResources = Lists.newArrayListWithExpectedSize(witnessSpecs.length);
    for (String witnessSpec : witnessSpecs) {
      inputResources.add(argumentToResource(witnessSpec));
    }
    if (inputResources.size() < 1) {
      throw new ParseException("No input resource(s) given");
    }


    return this;
  }

  Engine read() throws IOException, XPathExpressionException, SAXException {
    if (inputResources.size() < 2) {
      this.witnesses = objectMapper.readValue(inputResources.get(0), Collation.class).getWitnesses();
    } else {
      this.witnesses = Lists.newArrayListWithExpectedSize(inputResources.size());
      for (URL witnessURL : inputResources) {
        this.witnesses.add(new URLWitness("w" + (witnesses.size() + 1), witnessURL)
                .read(tokenizer, normalizer, inputCharset, (xmlMode ? tokenXPath : null)));
      }
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
    } else if ("graphml".equals(outputFormat) || "tei".equals(outputFormat)) {
      XMLStreamWriter xml = null;
      try {
        xml = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
        xml.writeStartDocument(outputCharset.name(), "1.0");
        if ("graphml".equals(outputFormat)) {
          serializer.toGraphML(xml);
        } else {
          serializer.toTEI(xml);
        }
        xml.writeEndDocument();
      } catch (XMLStreamException e) {
        throw new IOException(e);
      } finally {
        if (xml != null) {
          try {
            xml.close();
          } catch (XMLStreamException e) {
            throw new IOException(e);
          }
        }
      }
    } else {
      objectMapper.writer().writeValue(out, variantGraph);
    }
  }

  Engine log(String str) {
    log.write(str);
    return this;
  }

  void error(String str, Throwable t) {
    errorOccurred = true;
    log(str).log("\n").log(t.getMessage()).log("\n");
  }

  void help() {
    new HelpFormatter().printHelp(log, 78, "collatex [<options>]\n (<json_input> | <witness_1> <witness_2> [[<witness_3>] ...])", "", OPTIONS, 2, 4, "");
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
      throw new ParseException("Invalid resource: " + arg);
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
      engine.error("Error while parsing command line arguments", e);
      engine.log("\n").help();
    } catch (IllegalArgumentException e) {
      engine.error("Illegal argument", e);
    } catch (IOException e) {
      engine.error("I/O error", e);
    } catch (SAXException e) {
      engine.error("XML error", e);
    } catch (XPathExpressionException e) {
      engine.error("XPath error", e);
    } catch (ScriptException e) {
      engine.error("Script error", e);
    } catch (PluginScript.PluginScriptExecutionException e) {
      engine.error("Script error", e);
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
    OPTIONS.addOption("xp", "xpath", true, "XPath 1.0 expression evaluating to tokens of XML witnesses; default: '//text()'");
    OPTIONS.addOption("a", "algorithm", true, "progressive alignment algorithm to use 'dekker' (default), 'medite', 'needleman-wunsch'");
    OPTIONS.addOption("t", "tokenized", false, "consecutive matches of tokens will *not* be joined to segments");
    OPTIONS.addOption("f", "format", true, "result/output format: 'json', 'csv', 'dot', 'graphml', 'tei'");
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
