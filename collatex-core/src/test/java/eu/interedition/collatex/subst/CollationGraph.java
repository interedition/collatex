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
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

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
        runInSessionTransaction(tx -> tx.run("merge (c:Collation {name:{name}})", Values.parameters("name", name)));
    }

    public void addWitness(String sigil, String xml) {
        AtomicInteger counter = new AtomicInteger(1);
        WitnessNode wn = WitnessNode.createTree(sigil, xml);
        this.witnesses.add(wn);
        String t0Id = tokenId(sigil, 0);
        Set<String> cypherStatements = new TreeSet<>();
        cypherStatements.add("create (:Token{id:\"" + t0Id + "\",data:\"\"})");
        cypherStatements.add(createNextRelationBetween(t0Id, tokenId(sigil, 1), sigil, "option"));
        List<EditGraphTableLabel> labels = EditGraphAligner.createLabels(wn);
        List<String> choiceEndIds = Lists.newArrayList();
        String substStart = t0Id;
        String prevId = t0Id;
        Map<WitnessNode, Integer> elementMap = new HashMap<>();
        for (EditGraphTableLabel label : labels) {
            String tokenId = tokenId(sigil, counter.getAndIncrement());
            cypherStatements.add("create " + toTokenNode(label, tokenId));
            if (!label.startElements.isEmpty()) {
                if (label.containsStartSubst()) {
                    substStart = prevId;
                }
                if (label.containsStartSubstOption()) {
                    if (!tokenId.equals(substStart)) {
                        cypherStatements.add(createNextRelationBetween(substStart, tokenId, sigil, "option"));
                    }
                }
            } else {
                if (!prevId.isEmpty()) {
                    cypherStatements.add(createNextRelationBetween(prevId, tokenId, sigil, "main"));
                }
            }
            if (label.containsEndSubstOption()) {
                choiceEndIds.add(tokenId);
            }
            if (label.containsEndSubst()) {
                String nextTokenId = tokenId(sigil, counter.get());
                System.out.println(choiceEndIds);
                choiceEndIds.forEach(//
                        optionEndTokenId -> cypherStatements.add(createNextRelationBetween(optionEndTokenId, nextTokenId, sigil, "option"))//
                );
                substStart = "";
                choiceEndIds.clear();
            }
            addAnnotations(cypherStatements, sigil, elementMap, label, tokenId);
            prevId = tokenId;
        }

        runInSessionTransaction(tx -> {
            System.out.println(Joiner.on("\n").join(cypherStatements));
            cypherStatements.forEach(cs -> tx.run(cs));
            tx.run("match (c:Collation{name:{name}}), (t0:Token{id:{t0Id}}) create (c)-[:WITNESS]->(w:Witness{sigil:{sigil}})-[:FIRST_TOKEN]->(t0)", //
                    Values.parameters(//
                            "name", this.name, //
                            "sigil", sigil, //
                            "t0Id", t0Id//
            ));
        });
    }

    private void addAnnotations(Set<String> cypherStatements, String sigil, Map<WitnessNode, Integer> elementMap, EditGraphTableLabel label, String tokenId) {
        label.startElements.stream().filter(e -> !e.data.equals("subst")).forEach(se -> {
            elementMap.putIfAbsent(se, elementMap.size());
            Integer annotationIndex = elementMap.get(se);
            String id = annotationId(sigil, annotationIndex);
            cypherStatements.add("create (:Annotation{id:\"" + id + "\", title:\"" + se.data + "\"})");
            cypherStatements.add("match (t:Token{id:\"" + tokenId + "\"}), (a:Annotation{id:\"" + id + "\"}) merge (a)-[:BEGINS_AT]->(t)");
        });
        label.endElements.stream().filter(e -> !e.data.equals("subst")).forEach(se -> {
            Integer annotationId = elementMap.get(se);
            String id = annotationId(sigil, annotationId);
            cypherStatements.add("match (t:Token{id:\"" + tokenId + "\"}), (a:Annotation{id:\"" + id + "\"}) merge (a)-[:ENDS_AT]->(t)");
        });
    }

    private String annotationId(String sigil, Integer annotationId) {
        return "annotation-" + sigil + "-" + annotationId;
    }

    private String createNextRelationBetween(String substStart, String tokenId, String sigil, String layer) {
        // layer = "main";
        return "match (t0:Token{id:\"" + substStart + "\"}), (t1:Token{id:\"" + tokenId + "\"}) merge (t0)-[:NEXT{witness:\"" + sigil + "\",layer:\"" + layer + "\"}]->(t1)";
    }

    public void addWitness0(String sigil, String xml) {
        AtomicInteger counter = new AtomicInteger(1);
        WitnessNode wn = WitnessNode.createTree(sigil, xml);
        this.witnesses.add(wn);
        List<EditGraphTableLabel> labels = EditGraphAligner.createLabels(wn);
        runInSessionTransaction(tx -> {
            String cypher = witnessAsCypher(sigil, counter, labels);
            tx.run("match (c:Collation{name:{name}})\n create (c)-[:WITNESS]->(w:Witness{sigil:{sigil}})-[:FIRST_TOKEN]->" + cypher, //
                    Values.parameters(//
                            "name", this.name, //
                            "sigil", sigil));
        });
    }

    private String witnessAsCypher(String sigil, AtomicInteger counter, List<EditGraphTableLabel> labels) {
        String cypher = labels.stream()//
                .map(l -> toTokenNode(l, tokenId(sigil, counter.getAndIncrement())))//
                .collect(joining("-[:NEXT{witness:{sigil},layer:\"main\"}]->"));
        return cypher;
    }

    public void collate() {
        if (this.witnesses.size() != 2) {
            throw new RuntimeException("At the moment, ony collation with 2 witnesses is possible.");
        }
        EditGraphAligner ega = new EditGraphAligner(this.witnesses.get(0), this.witnesses.get(1));
        Set<Tuple<String>> matches = new HashSet<>();
        Map<String, AtomicInteger> counters = new HashMap<>();
        this.witnesses.stream()//
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
        runInSessionTransaction(tx -> matches.forEach(m -> {
            tx.run("match (t1:Token{id:{id1}}), (t2:Token{id:{id2}}) create (t1)-[:MATCHES]->(t2)", //
                    Values.parameters("id1", m.left, "id2", m.right));
        }));

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
        runInSessionTransaction(tx -> {
            tx.run("match (t1:Token)-[m:MATCHES]->(t2:Token)-[n:NEXT]->(t3) create (t1)-[n1:NEXT]->(t3) set n1 = n delete n");
            tx.run("match (t1:Token)-[m:MATCHES]->(t2:Token)<-[n:NEXT]-(t3) create (t3)-[n1:NEXT]->(t1) set n1 = n delete n");
            tx.run("match (t1:Token)-[m:MATCHES]->(t2:Token)<-[r:FIRST_TOKEN]->(w) create (w)-[r1:FIRST_TOKEN]->(t1) set r1 = r delete r");
            tx.run("match (t1:Token)-[m:MATCHES]->(t2:Token) set t1.id = t1.id+\" + \"+t2.id delete m,t2");
        });
    }

    public void runInSessionTransaction(Consumer<Transaction> consumer) {
        try (Session session = this.neo4j.session()) {
            try (Transaction tx = session.beginTransaction()) {
                consumer.accept(tx);
                tx.success();
            }
        }

    }

    public void joinNonVariantTokens() {

    }

}
