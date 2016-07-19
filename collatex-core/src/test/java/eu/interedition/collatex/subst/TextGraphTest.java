package eu.interedition.collatex.subst;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;

public class TextGraphTest {
    private static Driver neo4j;

    @BeforeClass
    public static void beforeClass() {
        neo4j = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "NEO4J"));
    }

    @AfterClass
    public static void afterClass() throws Exception {
        neo4j.close();
    }

    @Test
    public void testexample1() {
        String xml_a = "<wit n=\"1\"><subst><del>Apparently, in</del><add>So, at</add></subst> the <subst><del>beginning</del><add>outset</add></subst>, finding the <subst><del>correct</del><add>right</add></subst> word.</wit>";
        String xml_b = "<wit n=\"2\"><subst><del>Apparently, at</del><add>So, in</add></subst> <subst><del>the</del><add>this</add></subst> very beginning, finding the right word.</wit>";
        collate(xml_a, xml_b);
    }

    @Test
    public void testExample5() {
        String xml_a = "<wit n=\"1\">It is not an easy thing at all with long substitutions.</wit>";
        String xml_b = "<wit n=\"2\">It<subst><del>is not easy at all</del><add>all gets complicated</add></subst>with long substitutions.</wit>";
        collate(xml_a, xml_b);
    }

    @Test
    public void testExample6() {
        String xml_a = "<wit n=\"1\">It is not an easy thing at all with long substitutions.</wit>";
        String xml_b = "<wit n=\"2\">It<subst><del>is not</del><add>gets</add></subst><subst><del>easy</del><add>complicated</add></subst><subst><add>very quickly</add></subst>with long substitutions.</wit>";
        collate(xml_a, xml_b);
    }

    private void collate(String xml_a, String xml_b) {
        clearGraph();

        CollationGraph cg = new CollationGraph("testX", neo4j);
        cg.addWitness("A", xml_a);
        cg.addWitness("B", xml_b);
        cg.collate();
        // cg.foldMatches();
        cg.joinNonVariantTokens();
    }

    private void clearGraph() {
        try (Session session = neo4j.session()) {
            try (Transaction tx = session.beginTransaction()) {
                // remove all relations/edges
                tx.run("match (n)-[r]-() delete r");
                // remove all nodes/vertices
                tx.run("match (n) delete n");
                tx.success();
            }
        }
    }

}
