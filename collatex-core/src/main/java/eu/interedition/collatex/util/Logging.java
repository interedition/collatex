package eu.interedition.collatex.util;

import com.google.common.collect.Sets;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Logging {
  public static void configureLogging(String... loggers) {
    final Logger root = Logger.getLogger("");
    for (Handler rootHandler : root.getHandlers()) {
      root.removeHandler(rootHandler);
    }

    final Level logLevel = ((System.getProperty("interedition.debug") != null) ? Level.FINER : Level.INFO);
    root.addHandler(new StandardOutHandler(logLevel));

    final Set<String> interestingLoggers = Sets.newHashSet(Arrays.asList(loggers));
    if (!logLevel.equals(Level.INFO)) {
      interestingLoggers.add("eu.interedition.collatex");
    }

    for (String interesting : interestingLoggers) {
      Logger.getLogger(interesting).setLevel(logLevel);
    }
  }

  public static void dumpLogConfig() {
    final SortedSet<String> configuredLoggers = new TreeSet<String>();
    for (Enumeration<String> loggerNames = LogManager.getLogManager().getLoggerNames(); loggerNames.hasMoreElements(); ) {
      configuredLoggers.add(loggerNames.nextElement());
    }

    final Logger out = Logger.getLogger(Logging.class.getName());
    for (String logger : configuredLoggers) {
      final Logger instance = Logger.getLogger(logger);
      out.info("Logger config: '" + logger + "' :: " + instance.getLevel() + " [ delegate: "
              + instance.getUseParentHandlers() + " ] [ #handlers: " + instance.getHandlers().length
              + " ]");
    }
  }

  private static class StandardOutHandler extends ConsoleHandler {
    private StandardOutHandler(Level level) {
      super();
      setOutputStream(System.out);
      setFormatter(new SimpleLogFormatter());
      setLevel(level);
    }
  }

  private static class SimpleLogFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
      StringBuilder msg = new StringBuilder();

      msg.append(String.format("[%60.60s][%7s]: ", record.getLoggerName(), record.getLevel()));

      final String message = record.getMessage();
      final Object[] parameters = record.getParameters();
      msg.append(parameters == null ? message : MessageFormat.format(message, parameters));

      msg.append("\n");
      if (record.getThrown() != null) {
        try {
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          record.getThrown().printStackTrace(pw);
          pw.close();
          msg.append(sw.toString());
        } catch (Exception ex) {
        }
      }
      return msg.toString();
    }

  }
}
