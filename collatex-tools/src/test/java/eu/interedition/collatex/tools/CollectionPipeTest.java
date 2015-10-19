package eu.interedition.collatex.tools;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class CollectionPipeTest {
    final static Path fixturesBaseDir = fixturesBasePath();

    protected static Path fixturesBasePath() {
        String projPath = System.getProperty("user.dir");
        Path path = Paths.get(projPath, "src", "test", "fixtures");

        return path;
    }

    protected static Path fixturesFilePath(String id) {
        Path path = Paths.get(fixturesBaseDir.toString(), id + ".json");

        return path;
    }

    @Rule public final SystemOutRule systemOut = new SystemOutRule().enableLog().mute();

    @Test
    public void collates2Witnesses() throws Exception {
        String[] args = { "--format", "csv", fixturesFilePath("base-2w").toString() };
        final CommandLine commandLine = new GnuParser().parse(CollateX.OPTIONS, args);
        CollationPipe.start(commandLine);

        String stdout = systemOut.getLog();
        String lines[] = stdout.split("\\r?\\n");

        assertEquals(lines[0], "doc1,doc2");
        assertEquals(lines[1], "A,A");
        assertEquals(lines[lines.length - 1], "Z,Z");
    }

    @Test
    public void collates4Witnesses() throws Exception {
        String[] args = { "--format", "csv", fixturesFilePath("base-4w").toString() };
        final CommandLine commandLine = new GnuParser().parse(CollateX.OPTIONS, args);
        CollationPipe.start(commandLine);

        String stdout = systemOut.getLog();
        String lines[] = stdout.split("\\r?\\n");

        assertEquals(lines[0], "doc1,doc2,doc3,doc4");
        assertEquals(lines[1], "A,A,A,A");
        assertEquals(lines[lines.length - 1], "Z,Z,Z,Z");
    }

    @Test(expected = Exception.class)
    public void complainsIfNoWitnessesAreProvided() throws Exception {
        String[] args = { "--format", "csv" };
        final CommandLine commandLine = new GnuParser().parse(CollateX.OPTIONS, args);
        CollationPipe.start(commandLine);
    }
}
