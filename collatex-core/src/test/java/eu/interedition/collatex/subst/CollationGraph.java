package eu.interedition.collatex.subst;

import static java.util.stream.Collectors.joining;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import eu.interedition.collatex.dekker.Tuple;
import eu.interedition.collatex.subst.EditGraphAligner.EditGraphTableLabel;

public class CollationGraph {

    private Driver neo4j;
    private String name;
    private List<WitnessNode> witnesses = new ArrayList<>();

    public CollationGraph(String name, Driver neo4j) {
        this.neo4j = neo4j;
        createRoot(name);
    }

    private void createRoot(String name) {
        this.name = name;
        try (Session session = this.neo4j.session()) {
            try (Transaction tx = session.beginTransaction()) {
                tx.run("merge (c:Collation {name:{name}})", Values.parameters("name", name));
                tx.success();
            }
        }
    }

    public void addWitness(String sigil, String xml) {
        AtomicInteger counter = new AtomicInteger(1);
        WitnessNode wn = WitnessNode.createTree(sigil, xml);
        witnesses.add(wn);
        List<EditGraphTableLabel> labels = EditGraphAligner.createLabels(wn);
        try (Session session = this.neo4j.session()) {
            try (Transaction tx = session.beginTransaction()) {
                String cypher = labels.stream()//
                        .map(l -> toTokenNode(l, tokenId(sigil, counter.getAndIncrement())))//
                        .collect(joining("-[:NEXT{witness:{sigil}}]->"));
                tx.run("match (c:Collation{name:{name}})\n create (c)-[:WITNESS]->(w:Witness{sigil:{sigil}})-[:FIRST_TOKEN]->" + cypher, //
                        Values.parameters(//
                                "name", this.name, //
                                "sigil", sigil));
                tx.success();
            }
        }
    }

    public void collate() {
        if (this.witnesses.size() != 2) {
            throw new RuntimeException("At the moment, ony collation with 2 witnesses is possible.");
        }
        EditGraphAligner ega = new EditGraphAligner(this.witnesses.get(0), this.witnesses.get(1));
        Set<Tuple<String>> matches = new HashSet<>();
        Map<String, AtomicInteger> counters = new HashMap<>();
        witnesses.stream()//
                .map(WitnessNode::getSigil)//
                .forEach(sigil -> counters.put(sigil, new AtomicInteger(1)));
        ega.getSuperWitness().forEach(lwn -> {
            if (lwn.size() == 2) {
                // match
                String id1 = tokenId(counters, lwn, 0);
                String id2 = tokenId(counters, lwn, 1);
                matches.add(new Tuple<>(id1, id2));
            } else {
                // addition/deletion
                String sigil = lwn.get(0).getSigil();
                counters.get(sigil).getAndIncrement();
            }
        });
        try (Session session = this.neo4j.session()) {
            try (Transaction tx = session.beginTransaction()) {
                matches.forEach(m -> {
                    tx.run("match (t1:Token{id:{id1}}), (t2:Token{id:{id2}}) create (t1)-[:MATCHES]->(t2)", //
                            Values.parameters(//
                                    "id1", m.left, //
                                    "id2", m.right));

                });
                tx.success();
            }
        }

    }

    private String tokenId(Map<String, AtomicInteger> counters, List<WitnessNode> lwn, int index) {
        String sigil1 = lwn.get(index).getSigil();
        int i1 = counters.get(sigil1).getAndIncrement();
        String id1 = tokenId(sigil1, i1);
        return id1;
    }

    private String tokenId(String sigil1, int i1) {
        String id1 = this.name + ":" + sigil1 + ":" + i1;
        return id1;
    }

    private String toTokenNode(EditGraphTableLabel label, String id) {
        return "(:Token{id:\"" + id + "\",data:\"" + label.text.data + "\"})";
    }

    public void foldMatches() {
        try (Session session = this.neo4j.session()) {
            try (Transaction tx = session.beginTransaction()) {
                tx.run("match (t1:Token)-[m:MATCHES]->(t2:Token)-[n:NEXT]->(t3) create (t1)-[n1:NEXT]->(t3) set n1 = n delete n");
                tx.run("match (t1:Token)-[m:MATCHES]->(t2:Token)<-[n:NEXT]-(t3) create (t3)-[n1:NEXT]->(t1) set n1 = n delete n");
                tx.run("match (t1:Token)-[m:MATCHES]->(t2:Token)<-[r:FIRST_TOKEN]->(w) create (w)-[r1:FIRST_TOKEN]->(t1) set r1 = r delete r");
                tx.run("match (t1:Token)-[m:MATCHES]->(t2:Token) set t1.id = t1.id+\" + \"+t2.id delete m,t2");
                tx.success();
            }
        }
    }

}
