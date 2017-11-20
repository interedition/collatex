/*
 * Copyright (c) 2015 The Interedition Development Group.
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

package eu.interedition.collatex.tools;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class CollationPipe {

    public static void start(CommandLine commandLine) throws Exception {
        List<SimpleWitness> witnesses = null;
        Function<String, Stream<String>> tokenizer = SimplePatternTokenizer.BY_WS_OR_PUNCT;
        Function<String, String> normalizer = SimpleTokenNormalizers.LC_TRIM_WS;
        Comparator<Token> comparator = new EqualityTokenComparator();
        CollationAlgorithm collationAlgorithm = null;
        boolean joined = true;

        final String[] witnessSpecs = commandLine.getArgs();
        final InputStream[] inputStreams = new InputStream[witnessSpecs.length];
        for (int wc = 0, wl = witnessSpecs.length; wc < wl; wc++) {
            try {
                inputStreams[wc] = argumentToInputStream(witnessSpecs[wc]);
            } catch (MalformedURLException urlEx) {
                throw new ParseException("Invalid resource: " + witnessSpecs[wc]);
            }
        }

        if (inputStreams.length < 1) {
            throw new ParseException("No input resource(s) given");
        } else if (inputStreams.length < 2) {
            try (InputStream inputStream = inputStreams[0]) {
                final SimpleCollation collation = JsonProcessor.read(inputStream);
                witnesses = collation.getWitnesses();
                collationAlgorithm = collation.getAlgorithm();
                joined = collation.isJoined();
            }
        }

        final String script = commandLine.getOptionValue("s");
        try {
            final PluginScript pluginScript = (script == null
                ? PluginScript.read("<internal>", new StringReader(""))
                : PluginScript.read(argumentToInput(script)));

            tokenizer = Optional.ofNullable(pluginScript.tokenizer()).orElse(tokenizer);
            normalizer = Optional.ofNullable(pluginScript.normalizer()).orElse(normalizer);
            comparator = Optional.ofNullable(pluginScript.comparator()).orElse(comparator);
        } catch (IOException e) {
            throw new ParseException("Failed to read script '" + script + "' - " + e.getMessage());
        }

        switch (commandLine.getOptionValue("a", "").toLowerCase()) {
            case "needleman-wunsch":
                collationAlgorithm = CollationAlgorithmFactory.needlemanWunsch(comparator);
                break;
            case "medite":
                collationAlgorithm = CollationAlgorithmFactory.medite(comparator, SimpleToken.TOKEN_MATCH_EVALUATOR);
                break;
            case "gst":
                collationAlgorithm = CollationAlgorithmFactory.greedyStringTiling(comparator, 2);
                break;
            default:
                collationAlgorithm = Optional.ofNullable(collationAlgorithm).orElse(CollationAlgorithmFactory.dekker(comparator));
                break;
        }

        if (witnesses == null) {
            final Charset inputCharset = Charset.forName(commandLine.getOptionValue("ie", StandardCharsets.UTF_8.name()));
            final boolean xmlMode = commandLine.hasOption("xml");
            final XPathExpression tokenXPath = XPathFactory.newInstance().newXPath().compile(commandLine.getOptionValue("xp", "//text()"));

            witnesses = new ArrayList<>(inputStreams.length);
            for (int wc = 0, wl = inputStreams.length; wc < wl; wc++) {
                try (InputStream stream = inputStreams[wc]) {
                    final String sigil = "w" + (wc + 1);
                    if (!xmlMode) {
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, inputCharset));
                        final StringWriter writer = new StringWriter();
                        final char[] buf = new char[1024];
                        while (reader.read(buf) != -1) {
                            writer.write(buf);
                        }
                        witnesses.add(new SimpleWitness(sigil, writer.toString(), tokenizer, normalizer));
                    } else {
                        final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                        final Document document = documentBuilder.parse(stream);
                        document.normalizeDocument();

                        final SimpleWitness witness = new SimpleWitness(sigil);
                        final NodeList tokenNodes = (NodeList) tokenXPath.evaluate(document, XPathConstants.NODESET);
                        final List<Token> tokens = new ArrayList<>(tokenNodes.getLength());
                        for (int nc = 0; nc < tokenNodes.getLength(); nc++) {
                            final String tokenText = tokenNodes.item(nc).getTextContent();
                            tokens.add(new SimpleToken(witness, tokenText, normalizer.apply(tokenText)));
                        }
                        witness.setTokens(tokens);
                        witnesses.add(witness);
                    }
                }
            }
        }

        final VariantGraph variantGraph = new VariantGraph();
        collationAlgorithm.collate(variantGraph, witnesses);

        if (joined && !commandLine.hasOption("t")) {
            VariantGraph.JOIN.apply(variantGraph);
        }

        final String output = commandLine.getOptionValue("o", "-");
        final Charset outputCharset = Charset.forName(commandLine.getOptionValue("oe", StandardCharsets.UTF_8.name()));
        final String outputFormat = commandLine.getOptionValue("f", "json").toLowerCase();

        try (PrintWriter out = argumentToOutput(output, outputCharset)) {
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
                            // ignored
                        }
                    }
                }
            } else {
                JsonProcessor.write(variantGraph, out);
            }
        }
    }

    private static URL argumentToInput(String arg) throws MalformedURLException {
        final File witnessFile = new File(arg);
        if (witnessFile.exists()) {
            return witnessFile.toURI().normalize().toURL();
        } else {
            return new URL(arg);
        }
    }

    private static InputStream argumentToInputStream(String arg) throws IOException {
        if ("-".equals(arg)) {
	    return System.in;
        }
        final File witnessFile = new File(arg);
        if (witnessFile.exists()) {
            return witnessFile.toURI().normalize().toURL().openStream();
        } else {
            return new URL(arg).openStream();
        }
    }

    private static PrintWriter argumentToOutput(String arg, Charset outputCharset) throws ParseException, IOException {
        if ("-".equals(arg)) {
            return new PrintWriter(new OutputStreamWriter(System.out, outputCharset));
        }

        final File outFile = new File(arg);
        try {
            return new PrintWriter(Files.newBufferedWriter(outFile.toPath(), outputCharset));
        } catch (FileNotFoundException e) {
            throw new ParseException("Output file '" + outFile + "' not found");
        }
    }
}
