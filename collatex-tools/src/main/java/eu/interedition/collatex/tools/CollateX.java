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

import org.apache.commons.cli.*;
import org.xml.sax.SAXException;

import javax.script.ScriptException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CollateX {

    public static final PrintWriter ERROR_LOG = new PrintWriter(System.err);

    public static void main(String... args) {
        try {
            final CommandLine commandLine = new GnuParser().parse(OPTIONS, args);
            if (commandLine.hasOption("h")) {
                help();
            } else if (commandLine.hasOption("S")) {
                CollationServer.start(commandLine);
            } else {
                CollationPipe.start(commandLine);
            }
        } catch (ParseException e) {
            error("Error while parsing command line arguments (-h for usage instructions)", e);
        } catch (IllegalArgumentException e) {
            error("Illegal argument", e);
        }  catch (IOException e) {
            error("I/O error", e);
        } catch (SAXException e) {
            error("XML error", e);
        } catch (XPathExpressionException e) {
            error("XPath error", e);
        } catch (ScriptException | PluginScript.PluginScriptExecutionException e) {
            error("Script error", e);
        } catch (Throwable t) {
            error("Unexpected error", t);
        } finally {
            ERROR_LOG.flush();
        }
    }

    private static void help() {
        new HelpFormatter().printHelp(ERROR_LOG, 78, "collatex [<options>]\n (<json_input> | <witness_1> <witness_2> [[<witness_3>] ...])", "", OPTIONS, 2, 4, "");
    }

    public static void error(String str, Throwable t) {
        ERROR_LOG.println(str);
        ERROR_LOG.println(t.getMessage());
    }

    static final Options OPTIONS = new Options();

    static {
        OPTIONS.addOption("h", "help", false, "print usage instructions");

        OPTIONS.addOption("o", "output", true, "output file; '-' for standard output (default)");
        OPTIONS.addOption("ie", "input-encoding", true, "charset to use for decoding non-XML witnesses; default: UTF-8");
        OPTIONS.addOption("oe", "output-encoding", true, "charset to use for encoding the output; default: UTF-8");
        OPTIONS.addOption("xml", "xml-mode", false, "witnesses are treated as XML documents");
        OPTIONS.addOption("xp", "xpath", true, "XPath 1.0 expression evaluating to tokens of XML witnesses; default: '//text()'");
        OPTIONS.addOption("a", "algorithm", true, "progressive alignment algorithm to use 'dekker' (default), 'medite', 'needleman-wunsch'");
        OPTIONS.addOption("t", "tokenized", false, "consecutive matches of tokens will *not* be joined to segments");
        OPTIONS.addOption("f", "format", true, "result/output format: 'json', 'csv', 'dot', 'graphml', 'tei'");
        OPTIONS.addOption("s", "script", true, "ECMA/JavaScript resource with functions to be plugged into the alignment algorithm");

        OPTIONS.addOption("S", "http", false, "start RESTful HTTP server");
        OPTIONS.addOption("cp", "context-path", true, "URL base/context path of the service, default: '/'");
        OPTIONS.addOption("dot", "dot-path", true, "path to Graphviz 'dot', auto-detected by default");
        OPTIONS.addOption("p", "port", true, "HTTP port to bind server to, default: 7369");
        OPTIONS.addOption("mpc", "max-parallel-collations", true, "maximum number of collations to perform in parallel, default: 2");
        OPTIONS.addOption("mcs", "max-collation-size", true, "maximum number of characters (counted over all witnesses) to perform collations on, default: unlimited");

    }
}
