package eu.interedition.collatex.subst;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import eu.interedition.collatex.subst.EditGraphAligner.Score.Type;

/**
 * Created by ronalddekker on 30/04/16.
 * This is a special version of the edit graph aligner that can handle witnesses with substitutions in them.
 */
public class EditGraphAligner {
    final static LayerDefinition layerDefinition = LayerDefinition.getDefault();
    final static Set<String> orOperatorNames = layerDefinition.getOrOperators().stream().map(OrOperator::getName).collect(toSet());

    final List<EditGraphTableLabel> labelsWitnessB;
    final List<EditGraphTableLabel> labelsWitnessA;
    final Score[][] cells;

    public static List<EditGraphTableLabel> createLabels(WitnessNode wit_a) {
        Stream<WitnessNode.WitnessNodeEvent> nodeEventStream = wit_a.depthFirstNodeEventStream();
        // I want to group all the open , text and close events together in a group
        // I first try to do that with a reduce operation
        // but for that to work (left and right) have to be of the same type
        // we might be able to accomplish the same thing with a collect operator
        // Otherwise we have to fall back to the StreamEx extension package.
        // BiPredicate<WitnessNode.WitnessNodeEvent, WitnessNode.WitnessNodeEvent> predicate = (event1, event2) -> event1.getClass().equals(event2.getClass());
        // Two rules:
        // 1. When two text tokens follow each other we should not group them together
        // 1b. A text token followed by anything other than a close tag should not be grouped together
        // 2. When a close tag is followed by an open tag we should not group them together
        // 2b. When a close tag is followed by anything other than a close tag we should not group them together
        BiPredicate<WitnessNode.WitnessNodeEvent, WitnessNode.WitnessNodeEvent> predicate = (event1,
                event2) -> !(event1.type.equals(WitnessNode.WitnessNodeEventType.TEXT) && !event2.type.equals(WitnessNode.WitnessNodeEventType.END))
                        && !(event1.type.equals(WitnessNode.WitnessNodeEventType.END) && !event2.type.equals(WitnessNode.WitnessNodeEventType.END));

        return nodeEventStream.collect(new GroupOnPredicateCollector<>(predicate)).stream()//
                .map(list -> list.stream().collect(new LabelCollector()))//
                .collect(Collectors.toList());
    }

    public EditGraphAligner(WitnessNode a, WitnessNode b) {

        // from the witness node trees we calculate the labels
        this.labelsWitnessA = createLabels(a);
        this.labelsWitnessB = createLabels(b);

        // from the labels we map the nodes to cell coordinates
        Map<WitnessNode, Integer> nodeToXCoordinate = mapNodesToIndex(this.labelsWitnessA);
        Map<WitnessNode, Integer> nodeToYCoordinate = mapNodesToIndex(this.labelsWitnessB);

        // init cells and scorer
        this.cells = new Score[this.labelsWitnessB.size() + 1][this.labelsWitnessA.size() + 1];
        Scorer scorer = new Scorer();

        // init 0,0
        this.cells[0][0] = new Score(Type.empty, 0, 0, null, 0);

        // fill the first row with gaps
        IntStream.range(1, this.labelsWitnessA.size() + 1).forEach(x -> {
            int previousX = getPreviousCoordinateForLabel(nodeToXCoordinate, this.labelsWitnessA.get(x - 1), x - 1);
            this.cells[0][x] = scorer.gap(x, 0, this.cells[0][previousX]);
        });

        // fill the first column with gaps
        IntStream.range(1, this.labelsWitnessB.size() + 1).forEach(y -> {
            int previousY = getPreviousCoordinateForLabel(nodeToYCoordinate, this.labelsWitnessB.get(y - 1), y - 1);
            this.cells[y][0] = scorer.gap(0, y, this.cells[previousY][0]);
        });

        // fill the rest of the cells in an y by x fashion
        IntStream.range(1, this.labelsWitnessB.size() + 1).forEach(y -> {
            IntStream.range(1, this.labelsWitnessA.size() + 1).forEach(x -> {
                EditGraphTableLabel tokenB = this.labelsWitnessB.get(y - 1);
                EditGraphTableLabel tokenA = this.labelsWitnessA.get(x - 1);
                // the previous position does not have to be y-1 or x-1
                // in the case of an OR operation.. at the start of each OR operand we have to reset the counter
                // to the value before the start of the or operator.
                // most of this could be calculated beforehand and does not have to calculated again and again during the scoring

                // check whether a label (a or b) has an opening add or del
                // if so previous y and x are taken from the coordinates of the opening subst -1 (y and x)
                int previousX = getPreviousCoordinateForLabel(nodeToXCoordinate, tokenA, x - 1);
                int previousY = getPreviousCoordinateForLabel(nodeToYCoordinate, tokenB, y - 1);

                Score upperLeft = scorer.score(x, y, this.cells[previousY][previousX], tokenB, tokenA);
                Score left = scorer.gap(x, y, this.cells[y][previousX]);
                Score upper = scorer.gap(x, y, this.cells[previousY][x]);
                Score max = Collections.max(Arrays.asList(upperLeft, left, upper), (score, other) -> score.globalScore - other.globalScore);
                this.cells[y][x] = max;
                // System.err.println("[" + y + "," + x + "]:" + cells[y][x]);

                // check whether a label (a or b) has a closing subst
                // note that there can be multiple subst that end here..
                // it will be interesting to see how we handle that
                // NOTE: not only can there be a subst in both witnesses on the current position (y, x) in the table
                // It might as well occur that there are substs in subst present in either one or both witnesses.
                // There does not have to be an equal number of subst in both witnesses
                // We will have to see how that works out..

                // if tokenB as well as tokenA contain subst tags we have to do something more complicated
                // for now we detected that situation and exit

                // if (tokenA.containsEndSubst() && tokenB.containsEndSubst()) {
                // throw new UnsupportedOperationException("The witness set has a subst in both witnesses at the same time!");
                // }

                postProcessSubst(tokenA, y, x, nodeToXCoordinate, mapperA(y));
                postProcessSubst(tokenB, y, x, nodeToYCoordinate, mapperB(x));
            });
        });
    }

    private Function<Integer, Score> mapperA(int y) {
        return addDelx -> this.cells[y][addDelx];
    }

    private Function<Integer, Score> mapperB(int x) {
        return addDelY -> this.cells[addDelY][x];
    }

    private void postProcessSubst(EditGraphTableLabel editGraphTableLabel, int y, int x, Map<WitnessNode, Integer> nodeToCoordinate, Function<Integer, Score> toScoreMapper) {
        if (editGraphTableLabel.containsEndSubst()) {
            // NOTE: There can be more end subst nodes
            // here we go look for the subst again (this can be done more efficient)
            editGraphTableLabel.getEndOrperatorNodes()//
                    // Nu hebben we een end subst node te pakken
                    // nu moet ik alle bij behorende adds en dels zien te vinden..
                    // dat zijn de kinderen van de betreffende subst
                    .children()//

                    // ik moet eigenlijk filteren maar dat ga ik nu even niet doen
                    // Van alle child nodes moet ik daar dan weer de laatste child van pakken
                    .map(WitnessNode::getLastChild)//

                    // Daarna mappen we die childnodes naar cell coordinaten
                    // in het geval van token in witness A naar X coordinates
                    // in het geval van token in witness B naar Y coordinates
                    .map(nodeToCoordinate::get)//

                    // System.out.println("All the possible cell containing the scores of the options of this subst are: "+yCoordinatesWithScoresOfAddDels);
                    // now we have to find the maximum scoring cell of the possible cells..
                    // TODO; in the future we also have to set the parent coordinates correctly
                    // convert into scores;
                    .map(toScoreMapper)//
                    .max((score, other) -> score.globalScore - other.globalScore)//

                    // because it is the end of a subst; override the current score in the cell with the best score for the whole subst.
                    .ifPresent(bestScore -> this.cells[y][x] = bestScore);
        }
    }

    private int getPreviousCoordinateForLabel(Map<WitnessNode, Integer> nodeToCoordinate, EditGraphTableLabel token, int defaultCoordinate) {
        if (token.containsStartSubstOption()) {
            // start of an option (add / del)
            // every edit graph table label is associated with witness node (as the text)
            // we need to walk to the parent
            WitnessNode currentNode = token.text;
            Stream<WitnessNode> parentNodeStream = currentNode.parentNodeStream();
            Optional<WitnessNode> substOptional = parentNodeStream//
                    .filter(WitnessNode::isElement)//
                    .filter(this::isOrOperatorNode)//
                    .findFirst();
            WitnessNode substNode = substOptional//
                    .orElseThrow(() -> new RuntimeException("We found an OR operand but could not find the OR operator!"));
            // convert the substNode into index in the edit graph table
            Integer index = nodeToCoordinate.get(substNode);
            // debug
            // System.out.println("Label text on the horizontal axis >"+tokenA.text+"< maps to index "+previousX);
            return index - 1;
        }
        return defaultCoordinate;
    }

    private boolean isOrOperatorNode(WitnessNode node) {
        return orOperatorNames.contains(node.data);
    }

    private Map<WitnessNode, Integer> mapNodesToIndex(List<EditGraphTableLabel> labels) {
        // the following code has side effects. That is because Java 8 does not have an enumerate function for streams.
        Map<WitnessNode, Integer> nodesToCoordinate = new HashMap<>();

        // calculate the y and x coordinates of the witness nodes in the table (so we can map from one to the other)
        IntStream.range(0, labels.size()).forEach(index -> {
            // map index to label
            EditGraphTableLabel label = labels.get(index);
            // we take the nodes that are opened at this label and map the nodes to the index
            label.startElements.forEach(node -> nodesToCoordinate.put(node, index + 1));
            // we take the text node at this label and map it to the index
            nodesToCoordinate.put(label.text, index + 1);
        });

        return nodesToCoordinate;
    }

    // public void align() {
    //
    // }

    public List<List<WitnessNode>> getSuperWitness() {
        List<List<WitnessNode>> superwitness = getBacktrackScoreStream()//
                .flatMap(this::splitMismatches)//
                .map(this::toNodes)//
                .filter(ln -> !ln.isEmpty())//
                .collect(toList());
        Collections.reverse(superwitness);// until there's a streaming .reverse()
        superwitness = addRejectedChoices(superwitness);
        return groupNonMatchingTokensByWitness(superwitness);
    }

    private List<List<WitnessNode>> addRejectedChoices(List<List<WitnessNode>> superwitness) {
        List<List<WitnessNode>> list = Lists.newArrayList();

        Map<String, Iterator<WitnessNode>> witnessIterators = new TreeMap<>();
        Map<String, WitnessNode> currentWitnessNodes = new HashMap<>();

        Iterator<WitnessNode> witnessNodesA = witnessNodeIterator(labelsWitnessA);
        WitnessNode nextA = witnessNodesA.next();
        String sigilA = nextA.getSigil();
        witnessIterators.put(sigilA, witnessNodesA);
        currentWitnessNodes.put(sigilA, nextA);

        Iterator<WitnessNode> witnessNodesB = witnessNodeIterator(labelsWitnessB);
        WitnessNode nextB = witnessNodesB.next();
        String sigilB = nextB.getSigil();
        witnessIterators.put(sigilB, witnessNodesB);
        currentWitnessNodes.put(sigilB, nextB);

        for (List<WitnessNode> column : superwitness) {
            // add rejected choices before this column
            for (WitnessNode witnessNode : column) {
                String sigil = witnessNode.getSigil();
                WitnessNode currentNodeForThisWitness = currentWitnessNodes.get(sigil);
                while (!currentNodeForThisWitness.equals(witnessNode)) {
                    list.add(ImmutableList.of(currentNodeForThisWitness));
                    if (witnessIterators.get(sigil).hasNext()) {
                        currentWitnessNodes.put(sigil, witnessIterators.get(sigil).next());
                        currentNodeForThisWitness = currentWitnessNodes.get(sigil);
                    }
                }
            }
            list.add(column);
            column.stream()//
                    .map(WitnessNode::getSigil)//
                    .forEach(sigil -> {
                        if (witnessIterators.get(sigil).hasNext()) {
                            currentWitnessNodes.put(sigil, witnessIterators.get(sigil).next());
                        }
                    });
        }
        // add rejected choices at the end
        witnessIterators.values().forEach(iterator -> {
            while (iterator.hasNext()) {
                list.add(ImmutableList.of(iterator.next()));
            }
        });
        return list;
    }

    private Iterator<WitnessNode> witnessNodeIterator(List<EditGraphTableLabel> labels) {
        return labels.stream()//
                .map(l -> l.text)//
                .collect(toList())//
                .iterator();
    }

    private List<List<WitnessNode>> groupNonMatchingTokensByWitness(List<List<WitnessNode>> superwitness) {
        List<List<WitnessNode>> grouped = Lists.newArrayList();
        Multimap<String, List<WitnessNode>> nonMatches = LinkedListMultimap.create();
        for (List<WitnessNode> column : superwitness) {
            if (isMatch(column)) {
                addGrouped(nonMatches, grouped);
                grouped.add(column);
            } else {
                String sigil = column.get(0).getSigil();
                nonMatches.put(sigil, column);
            }
        }
        addGrouped(nonMatches, grouped);
        return grouped;
    }

    private boolean isMatch(List<WitnessNode> column) {
        return column.size() == 2;
    }

    private void addGrouped(Multimap<String, List<WitnessNode>> nonMatches, List<List<WitnessNode>> grouped) {
        Map<String, Collection<List<WitnessNode>>> nonMatchMap = nonMatches.asMap();
        nonMatchMap.keySet()//
                .stream()//
                .sorted()//
                .map(nonMatchMap::get)//
                .forEach(grouped::addAll);
        nonMatches.clear();
    }

    public List<WitnessNode> toNodes(Score score) {
        // System.err.println("score=" + score);
        List<WitnessNode> nodes = new ArrayList<>();
        WitnessNode nodeA = this.labelsWitnessA.get(score.x - 1).text;
        Optional<WitnessNode> nodeB = score.y > 0 //
                ? Optional.of(this.labelsWitnessB.get(score.y - 1).text)//
                : Optional.empty();
        switch (score.type) {
        case match:
            // diagonal
            nodes.add(nodeA);
            nodes.add(nodeB.get());
            break;

        case deletion:
            // left
            nodes.add(nodeA);
            break;

        case addition:
            // up
            nodeB.ifPresent(node -> nodes.add(node));
            break;

        default:
            break;
        }
        return nodes;
    }

    public Stream<Score> splitMismatches(Score score) {
        List<Score> list = new ArrayList<>();
        switch (score.type) {
        case mismatch:
            Score partB = new Score(Type.addition, score.x, score.y, score.parent);
            list.add(partB);
            Score partA = new Score(Type.deletion, score.x, score.y, score.parent);
            list.add(partA);
            break;

        default:
            list.add(score);
            break;
        }
        return list.stream();
    }

    public Stream<Score> getBacktrackScoreStream() {
        Iterable<Score> it = () -> new ScoreIterator(this.cells);
        return StreamSupport.stream(it.spliterator(), false);
    }

    private static class ScoreIterator implements Iterator<Score> {
        private Score[][] matrix;
        Integer y;
        Integer x;

        ScoreIterator(Score[][] matrix) {
            this.matrix = matrix;
            this.x = matrix[0].length - 1;
            this.y = matrix.length - 1;
        }

        @Override
        public boolean hasNext() {
            return !(this.x == 0 && this.y == 0);
        }

        @Override
        public Score next() {
            Score currentScore = this.matrix[this.y][this.x];
            this.x = currentScore.previousX;
            this.y = currentScore.previousY;
            return currentScore;
        }

    }

    static class LabelCollector implements java.util.stream.Collector<WitnessNode.WitnessNodeEvent, EditGraphTableLabel, EditGraphTableLabel> {
        @Override
        public Supplier<EditGraphTableLabel> supplier() {
            return EditGraphTableLabel::new;
        }

        @Override
        public BiConsumer<EditGraphTableLabel, WitnessNode.WitnessNodeEvent> accumulator() {
            return (label, event) -> {
                switch (event.type) {
                case START:
                    label.addStartEvent(event.node);
                    break;
                case END:
                    label.addEndEvent(event.node);
                    break;
                case TEXT:
                    label.addTextEvent(event.node);
                    String parentTag = event.node.parentNodeStream().iterator().next().data;
                    label.layer = parentTag.equals("wit") ? "base" : parentTag;
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown event type");
                }
            };
        }

        @Override
        public BinaryOperator<EditGraphTableLabel> combiner() {
            return (item, item2) -> {
                throw new UnsupportedOperationException();
            };
        }

        @Override
        public Function<EditGraphTableLabel, EditGraphTableLabel> finisher() {
            return (label) -> label;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.emptySet();
        }
    }

    static class EditGraphTableLabel {
        String layer;
        List<WitnessNode> startElements = new ArrayList<>();
        List<WitnessNode> endElements = new ArrayList<>();
        WitnessNode text;

        public void addStartEvent(WitnessNode node) {
            this.startElements.add(node);
        }

        public void addEndEvent(WitnessNode node) {
            this.endElements.add(node);
        }

        public void addTextEvent(WitnessNode node) {
            if (this.text != null) {
                throw new UnsupportedOperationException();
            }
            this.text = node;
        }

        @Override
        public String toString() {
            String a = this.startElements.stream().map(WitnessNode::toString).collect(Collectors.joining(", "));
            String b = this.text.toString();
            String c = this.endElements.stream().map(WitnessNode::toString).collect(Collectors.joining(", "));
            return MessageFormat.format("{0}:{1}:{2}", b, a, c);
        }

        public boolean containsStartSubstOption() {
            // find the first add or del tag...
            // Note that this implementation is from the left to right
            return containsSubstOption(this.startElements);
        }

        public boolean containsEndSubstOption() {
            // find the first add or del tag...
            // Note that this implementation is from the left to right
            return containsSubstOption(this.endElements);
        }

        private boolean containsSubstOption(List<WitnessNode> witnessNodeList) {
            return witnessNodeList.stream()//
                    .filter(this::isSubstOption)//
                    .findFirst()//
                    .isPresent();
        }

        public boolean containsStartSubst() {
            return containsOrOperator(this.startElements);
        }

        private boolean containsOrOperator(List<WitnessNode> witnessNodeList) {
            return witnessNodeList.stream()//
                    .filter(this::isOrOperator)//
                    .findFirst()//
                    .isPresent();
        }

        public boolean containsEndSubst() {
            // find the first end OR-operator tag...
            // if we want to do this completely correct it should be from right to left
            return containsOrOperator(this.endElements);
        }

        public WitnessNode getEndOrperatorNodes() {
            return this.endElements.stream()//
                    .filter(this::isOrOperator)//
                    .findFirst().orElseThrow(() -> new RuntimeException("We expected one or more OR-operator tags here!"));
        }

        private Boolean isOrOperator(WitnessNode node) {
            return node.isElement()//
                    && orOperatorNames.contains(node.data);
        }

        private Boolean isSubstOption(WitnessNode node) {
            return node.isElement()//
                    && (node.data.equals("add") || node.data.equals("del"));
        }
    }

    public static class Score {

        public static enum Type {
            match, mismatch, addition, deletion, empty
        }

        public Type type;
        int x;
        int y;
        public Score parent;
        int previousX;
        int previousY;
        public int globalScore = 0;

        public Score(Type type, int x, int y, Score parent, int i) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.previousX = parent == null ? 0 : parent.x;
            this.previousY = parent == null ? 0 : parent.y;
            this.globalScore = i;
        }

        public Score(Type type, int x, int y, Score parent) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.previousX = parent.x;
            this.previousY = parent.y;
            this.globalScore = parent.globalScore;
        }

        public int getGlobalScore() {
            return this.globalScore;
        }

        public void setGlobalScore(int globalScore) {
            this.globalScore = globalScore;
        }

        @Override
        public String toString() {
            return "[" + this.y + "," + this.x + "]:" + this.globalScore;
        }

    }

    class Scorer {
        public Score gap(int x, int y, Score parent) {
            Type type = determineType(x, y, parent);
            return new Score(type, x, y, parent, parent.globalScore - 1);
        }

        public Score score(int x, int y, Score parent, EditGraphTableLabel tokenB, EditGraphTableLabel tokenA) {
            if (tokensMatch(tokenB, tokenA)) {
                return new Score(Type.match, x, y, parent);
            }

            return new Score(Type.mismatch, x, y, parent, parent.globalScore - 2);
        }

        private boolean tokensMatch(EditGraphTableLabel tokenB, EditGraphTableLabel tokenA) {
            return normalized(tokenB).equals(normalized(tokenA));
        }

        private String normalized(EditGraphTableLabel tokenB) {
            return tokenB.text.data.toLowerCase().trim();
        }

        private Type determineType(int x, int y, Score parent) {
            if (x == parent.x) {
                return Type.addition;
            }
            if (y == parent.y) {
                return Type.deletion;
            }
            return Type.empty;
        }
    }
}
