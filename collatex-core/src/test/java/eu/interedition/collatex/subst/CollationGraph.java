package eu.interedition.collatex.subst;

import static java.util.stream.Collectors.joining;

import org.assertj.core.util.Sets;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.Tuple;
import eu.interedition.collatex.subst.EditGraphAligner.EditGraphTableLabel;

public class CollationGraph {
    private Driver neo4j;
    private String name;
    private List<WitnessNode> witnessNodes = new ArrayList<>();
    private List<Witness> witnesses = new ArrayList<>();

    public CollationGraph(String name, Driver neo4j) {
        this.neo4j = neo4j;
        createRoot(name);
    }

    private void createRoot(String name) {
        this.name = name;
        runInSessionTransaction(tx -> tx.run("merge (c:Collation {name:{name}})", Values.parameters("name", name)));
    }

    public Witness addWitness(String sigil, String xml) {
        AtomicInteger counter = new AtomicInteger(1);
        WitnessNode wn = WitnessNode.createTree(sigil, xml);
        this.witnessNodes.add(wn);
        String t0Id = tokenId(sigil, 1);
        Set<String> cypherStatements = new TreeSet<>();
        // cypherStatements.add("create (:Token{id:\"" + t0Id + "\",data:\"\"})");
        // cypherStatements.add(createNextRelationBetween(t0Id, tokenId(sigil, 1), sigil));
        List<EditGraphTableLabel> labels = EditGraphAligner.createLabels(wn);
        String prevId = "";
        Map<WitnessNode, Integer> elementMap = new HashMap<>();
        for (EditGraphTableLabel label : labels) {
            int index = counter.getAndIncrement();
            String tokenId = tokenId(sigil, index);
            cypherStatements.add("create " + toTokenNode(label, tokenId, index));
            if (!label.startElements.isEmpty()) {
                if (label.containsStartOrOperand() || label.containsStartOrOperator()) {
                    cypherStatements.add(createNextRelationBetween(prevId, tokenId, sigil));
                }
            } else {
                if (!prevId.isEmpty()) {
                    cypherStatements.add(createNextRelationBetween(prevId, tokenId, sigil));
                }
            }
            addAnnotations(cypherStatements, sigil, elementMap, label, tokenId);
            prevId = tokenId;
        }

        runInSessionTransaction(tx -> {
            // System.out.println(Joiner.on("\n").join(cypherStatements));
            cypherStatements.forEach(cs -> tx.run(cs));
            tx.run("match (c:Collation{name:{name}}), (t0:Token{id:{t0Id}}) create (c)-[:WITNESS]->(w:Witness{sigil:{sigil}})-[:FIRST_TOKEN]->(t0)", //
                    Values.parameters(//
                            "name", this.name, //
                            "sigil", sigil, //
                            "t0Id", t0Id//
            ));
        });
        LayeredWitness myWitness = new LayeredWitness(sigil);
        this.witnesses.add(myWitness);
        return myWitness;
    }

    public List<Witness> getWitnesses() {
        return witnesses;
    }

    private void addAnnotations(Set<String> cypherStatements, String sigil, Map<WitnessNode, Integer> elementMap, EditGraphTableLabel label, String tokenId) {
        label.startElements.stream().forEach(se -> {
            elementMap.putIfAbsent(se, elementMap.size());
            Integer annotationIndex = elementMap.get(se);
            String id = annotationId(sigil, annotationIndex);
            cypherStatements.add("create (:Annotation{id:\"" + id + "\", title:\"" + se.data + "\"})");
            cypherStatements.add("match (t:Token{id:\"" + tokenId + "\"}), (a:Annotation{id:\"" + id + "\"}) merge (a)-[:BEGINS_AT]->(t)");
        });
        label.endElements.stream().forEach(se -> {
            Integer annotationId = elementMap.get(se);
            String id = annotationId(sigil, annotationId);
            cypherStatements.add("match (t:Token{id:\"" + tokenId + "\"}), (a:Annotation{id:\"" + id + "\"}) merge (a)-[:ENDS_AT]->(t)");
        });
    }

    private String annotationId(String sigil, Integer annotationId) {
        return MessageFormat.format("annotation-{0}-{1,number,000}", sigil, annotationId);
    }

    private String createNextRelationBetween(String substStart, String tokenId, String sigil) {
        return "match (t0:Token{id:\"" + substStart + "\"}), (t1:Token{id:\"" + tokenId + "\"}) merge (t0)-[:NEXT{witness:\"" + sigil + "\"}]->(t1)";
    }

    public void addWitness0(String sigil, String xml) {
        AtomicInteger counter = new AtomicInteger(1);
        WitnessNode wn = WitnessNode.createTree(sigil, xml);
        this.witnessNodes.add(wn);
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
                .map(l -> toTokenNode(l, tokenId(sigil, counter.get()), counter.getAndIncrement()))//
                .collect(joining("-[:NEXT{witness:{sigil},layer:\"main\"}]->"));
        return cypher;
    }

    public void collate() {
        if (this.witnessNodes.size() != 2) {
            throw new RuntimeException("At the moment, ony collation with 2 witnesses is possible.");
        }
        EditGraphAligner ega = new EditGraphAligner(this.witnessNodes.get(0), this.witnessNodes.get(1));
        Set<Tuple<String>> matches = new HashSet<>();
        Map<String, AtomicInteger> counters = new HashMap<>();
        this.witnessNodes.stream()//
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
        return MessageFormat.format("{0}:{1}:{2,number,00}", this.name, sigil1, i1);
    }

    private String toTokenNode(EditGraphTableLabel label, String id, int index) {
        return "(:Token{id:\"" + id + "\",data:\"" + label.text.data + "\",index:" + index + ",layer:\"" + label.layer + "\"})";
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

    public List<SortedMap<Witness, Set<Token>>> asTable() {
        List<SortedMap<Witness, Set<Token>>> alignmentTable = new ArrayList<>();
        Map<String, List<LayerToken>> tokensPerWitness = new HashMap<>();
        runInSessionTransaction(tx -> {
            witnessNodes.stream().map(WitnessNode::getSigil).forEach(s -> {
                StatementResult result = tx.run("match (w:Witness{sigil:\"" + s + "\"})-[:FIRST_TOKEN|NEXT*]->(t:Token) return t.id, t.index, t.data, t.layer");
                List<LayerToken> witnessTokens = new ArrayList<>();
                result.forEachRemaining(r -> {
                    String id = r.get("t.id").asString();
                    Integer index = r.get("t.index").asInt();
                    String value = r.get("t.data").asString();
                    String layer = r.get("t.layer").asString();
                    Integer matchIndex = matchIndex(tx, id);
                    witnessTokens.add(new LayerToken(s, id, value, index, matchIndex, layer));
                });
                tokensPerWitness.put(s, witnessTokens);
            });

            String sigilA = witnessNodes.get(0).getSigil();
            Iterator<TokenGroup> tokensA = group(tokensPerWitness.get(sigilA));

            String sigilB = witnessNodes.get(1).getSigil();
            Iterator<TokenGroup> tokensB = group(tokensPerWitness.get(sigilB));

            TokenGroup tokenGroupA = tokensA.next();
            TokenGroup tokenGroupB = tokensB.next();

            while (tokenGroupA != null && tokenGroupB != null) {
                boolean groupsInSameMatchState = (tokenGroupA.isMatched() && tokenGroupB.isMatched()) //
                        || (!tokenGroupA.isMatched() && !tokenGroupB.isMatched());
                final SortedMap<Witness, Set<Token>> row = new TreeMap<>(Witness.SIGIL_COMPARATOR);
                if (groupsInSameMatchState) {
                    tokenGroupA = useGroup(sigilA, tokensA, tokenGroupA, row);
                    tokenGroupB = useGroup(sigilB, tokensB, tokenGroupB, row);

                } else if (tokenGroupA.isMatched()) {
                    tokenGroupB = useGroup(sigilB, tokensB, tokenGroupB, row);

                } else {
                    tokenGroupA = useGroup(sigilA, tokensA, tokenGroupA, row);
                }
                alignmentTable.add(row);
            }

            final SortedMap<Witness, Set<Token>> row = new TreeMap<>(Witness.SIGIL_COMPARATOR);
            if (tokenGroupA != null && tokenGroupB == null) {
                useGroup(sigilA, tokensA, tokenGroupA, row);
                alignmentTable.add(row);

            } else if (tokenGroupB != null && tokenGroupA == null) {
                useGroup(sigilB, tokensB, tokenGroupB, row);
                alignmentTable.add(row);
            }

        });
        return alignmentTable;
    }

    private TokenGroup useGroup(String sigil, Iterator<TokenGroup> tokensIterator, TokenGroup tokenGroup, final SortedMap<Witness, Set<Token>> row) {
        row.put(new LayeredWitness(sigil), tokenGroup.getTokenSet());
        return tokensIterator.hasNext() ? tokensIterator.next() : null;
    }

    enum State {
        initial, match, mismatch
    }

    private Iterator<TokenGroup> group(List<LayerToken> list) {
        List<TokenGroup> groupList = new ArrayList<>();
        State lastState = State.initial;
        Integer lastMatchIndex = 0;
        for (LayerToken t : list) {
            State state = t.hasMatch() ? State.match : State.mismatch;
            TokenGroup group = null;
            boolean addToCurrentGroup = state.equals(State.mismatch) && lastState.equals(State.mismatch)//
                    || ((state.equals(State.match) && lastState.equals(State.match)) //
                            && t.getMatchIndex() == lastMatchIndex + 1//
                    );
            if (addToCurrentGroup) { //
                group = groupList.get(groupList.size() - 1);
            } else {
                group = new TokenGroup(t.hasMatch());
                groupList.add(group);
            }
            group.addToken(t);
            lastState = state;
            if (t.hasMatch()) {
                lastMatchIndex = t.getMatchIndex();
            }
        }
        return groupList.iterator();
    }

    private Integer matchIndex(Transaction tx, String id) {
        StatementResult result = tx.run("match (t:Token{id:{id}})-[:MATCHES]-(t1) return t1.index", Values.parameters("id", id));
        if (result.hasNext()) {
            return result.next().get(0).asInt();
        }
        return -1;
    }

    public static class TokenGroup {
        private Set<Token> tokenSet = Sets.newHashSet();
        private Boolean matched;

        public TokenGroup(Boolean matched) {
            this.setMatched(matched);
        }

        public Set<Token> getTokenSet() {
            return tokenSet;
        }

        public void addToken(Token token) {
            this.tokenSet.add(token);
        }

        public Boolean isMatched() {
            return matched;
        }

        public void setMatched(Boolean matched) {
            this.matched = matched;
        }
    }

}
