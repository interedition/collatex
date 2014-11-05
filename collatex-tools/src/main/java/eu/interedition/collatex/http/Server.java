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

package eu.interedition.collatex.http;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.StaticHttpHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Server {

  public static void main(String... args) {
    try {
      final CommandLine commandLine = new GnuParser().parse(OPTIONS, args);
      if (commandLine.hasOption("h")) {
        new HelpFormatter().printHelp("collatex-server [<options> ...]\n", OPTIONS);
        return;
      }


      final Collator collator = new Collator(
              Integer.parseInt(commandLine.getOptionValue("mpc", "2")),
              Integer.parseInt(commandLine.getOptionValue("mcs", "0")),
              commandLine.getOptionValue("dot", null)
      );
      final String staticPath = System.getProperty("collatex.static.path", "");
      final HttpHandler httpHandler = staticPath.isEmpty() ? new CLStaticHttpHandler(Server.class.getClassLoader(), "/static/") {
        @Override
        protected void onMissingResource(Request request, Response response) throws Exception {
          collator.service(request, response);
        }
      } : new StaticHttpHandler(staticPath.replaceAll("/+$", "") + "/") {
        @Override
        protected void onMissingResource(Request request, Response response) throws Exception {
          collator.service(request, response);
        }
      };


      final NetworkListener httpListener = new NetworkListener("http", "0.0.0.0", Integer.parseInt(commandLine.getOptionValue("p", "7369")));

      final CompressionConfig compressionConfig = httpListener.getCompressionConfig();
      compressionConfig.setCompressionMode(CompressionConfig.CompressionMode.ON);
      compressionConfig.setCompressionMinSize(860); // http://webmasters.stackexchange.com/questions/31750/what-is-recommended-minimum-object-size-for-gzip-performance-benefits
      compressionConfig.setCompressableMimeTypes("application/javascript", "application/json", "application/xml", "text/css", "text/html", "text/javascript", "text/plain", "text/xml");

      final HttpServer httpServer = new HttpServer();
      httpServer.addListener(httpListener);
      httpServer.getServerConfiguration().addHttpHandler(httpHandler, commandLine.getOptionValue("cp", "").replaceAll("/+$", "") + "/*");

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        if (LOG.isLoggable(Level.INFO)) {
          LOG.info("Stopping HTTP server");
        }
        httpServer.shutdown();
      }));

      httpServer.start();

      Thread.sleep(Long.MAX_VALUE);
    } catch (Throwable t) {
      LOG.log(Level.SEVERE, "Error while parsing command line", t);
      System.exit(1);
    }
  }

  static final Logger LOG = Logger.getLogger(Server.class.getName());
  static final Options OPTIONS = new Options();

  static {
    OPTIONS.addOption("cp", "context-path", true, "URL base/context path of the service, default: '/'");
    OPTIONS.addOption("dot", "dot-path", true, "path to Graphviz 'dot', auto-detected by default");
    OPTIONS.addOption("h", "help", false, "prints usage instructions");
    OPTIONS.addOption("p", "port", true, "HTTP port to bind server to, default: 7369");
    OPTIONS.addOption("mpc", "max-parallel-collations", true, "maximum number of collations to perform in parallel, default: 2");
    OPTIONS.addOption("mcs", "max-collation-size", true, "maximum number of characters (counted over all witnesses) to perform collations on, default: unlimited");
  }
}
