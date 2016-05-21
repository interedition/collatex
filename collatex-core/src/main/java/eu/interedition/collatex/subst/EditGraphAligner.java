package eu.interedition.collatex.subst;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by ronalddekker on 30/04/16.
 * This is a special version of the edit graph aligner that can handle witnesses with substitutions in them.
 */
public class EditGraphAligner {

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
        BiPredicate<WitnessNode.WitnessNodeEvent, WitnessNode.WitnessNodeEvent> predicate = (event1, event2) -> !(event1.type.equals(WitnessNode.WitnessNodeEventType.TEXT) && !event2.type.equals(WitnessNode.WitnessNodeEventType.END)) && !(event1.type.equals(WitnessNode.WitnessNodeEventType.END) && !event2.type.equals(WitnessNode.WitnessNodeEventType.END));

        List<List<WitnessNode.WitnessNodeEvent>> lists = nodeEventStream.collect(new GroupOnPredicateCollector<>(predicate));
        Stream<EditGraphTableLabel> editGraphTableLabelStream = lists.stream().map(list -> list.stream().collect(new LabelCollector()));
        return editGraphTableLabelStream.collect(Collectors.toList());
    }




    public EditGraphAligner(WitnessNode a, WitnessNode b) {
        // from the witness node trees we calculate the labels
        labelsWitnessA = createLabels(a);
        labelsWitnessB = createLabels(b);

        // from the labels we map the nodes to cell coordinates
        Map<WitnessNode, Integer> nodeToXCoordinate = mapNodesToIndex(labelsWitnessA);
        Map<WitnessNode, Integer> nodeToYCoordinate = mapNodesToIndex(labelsWitnessB);

        // init cells and scorer
        cells = new Score[labelsWitnessB.size()+1][labelsWitnessA.size()+1];
        Scorer scorer = new Scorer();

        // init 0,0
        cells[0][0] = new Score(0);

        // fill the first row with gaps
        IntStream.range(1, labelsWitnessA.size()+1).forEach(x -> {
            int previousX = getPreviousCoordinateForLabel(nodeToXCoordinate, labelsWitnessA.get(x-1),x-1);
            cells[0][x] = scorer.gap(cells[0][previousX]);
        });

        // fill the first column with gaps
        IntStream.range(1, labelsWitnessB.size()+1).forEach(y -> {
            int previousY = getPreviousCoordinateForLabel(nodeToYCoordinate, labelsWitnessB.get(y-1),y-1);
            cells[y][0] = scorer.gap(cells[previousY][0]);
        });

        // fill the rest of the cells in an y by x fashion
        IntStream.range(1, labelsWitnessB.size()+1).forEach(y -> {
            IntStream.range(1, labelsWitnessA.size()+1).forEach(x -> {
                EditGraphTableLabel tokenB = labelsWitnessB.get(y - 1);
                EditGraphTableLabel tokenA = labelsWitnessA.get(x - 1);
                // the previous position does not have to be y-1 or x-1
                // in the case of an OR operation.. at the start of each OR operand we have to reset the counter
                // to the value before the start of the or operator.
                // most of this could be calculated before hand and does not have to calculated again and again during the scoring

                // check whether a label (a or b) has an opening add or del
                // if so previous y and x are taken from the coordinates of the opening subst -1 (y and x)
                int previousX = getPreviousCoordinateForLabel(nodeToXCoordinate, tokenA, x-1);
                int previousY = getPreviousCoordinateForLabel(nodeToYCoordinate, tokenB, y-1);

                Score upperLeft = scorer.score(cells[previousY][previousX], tokenB, tokenA);
                Score left = scorer.gap(cells[y][previousX]);
                Score upper = scorer.gap(cells[previousY][x]);
                Score max = Collections.max(Arrays.asList(upperLeft, left, upper), (score, other) -> score.globalScore - other.globalScore);
                cells[y][x] = max;


                // check whether a label (a or b) has a closing subst
                // note that there can be multiple subst that end here..
                // it will be interesting to see how we handle that
                // NOTE: not only can there be a subst in both witnesses on the current position (y, x) in the table
                // It might as well occur that there are substs in subst present in either one or both witnesses.
                // There does not have to be an equal number of subst in both witnesses
                // We will have to see how that works out..

                // if tokenB as well as tokenA contain subst tags we have to do something more complicated
                // for now we detected that situation and exit

                if (tokenA.containsEndSubst() && tokenB.containsEndSubst()) {
                    throw new UnsupportedOperationException("The witness set has a subst in both witnesses at the same time!");
                }



                if (tokenB.containsEndSubst()) {
                    postprocesssubstVertical(nodeToYCoordinate, y, x, tokenB);
                }

                if (tokenA.containsEndSubst()) {
                    postProcessSubstHorizontal(nodeToXCoordinate, y, x, tokenA);
                }
            });
        });
    }

    private void postProcessSubstHorizontal(Map<WitnessNode, Integer> nodeToXCoordinate, int y, int x, EditGraphTableLabel tokenA) {
        // NOTE: There can be more end subst nodes
        WitnessNode endSubstNode = tokenA.getEndSubstNodes();
        List<Integer> xCoordinatesWithScoresOfAddDels = endSubstNode.children().map(WitnessNode::getLastChild).map(nodeToXCoordinate::get).collect(Collectors.toList());
        Optional<Score> maximumScoreOptional = xCoordinatesWithScoresOfAddDels.stream().map(addDelx -> cells[y][addDelx]).max((score, other) -> score.globalScore - other.globalScore);
        Score bestScore = maximumScoreOptional.get();
        cells[y][x] = bestScore;
    }

    private void postprocesssubstVertical(Map<WitnessNode, Integer> nodeToYCoordinate, int y, int x, EditGraphTableLabel tokenB) {
        // here we go look for the subst again (this can be done more efficient)
        WitnessNode endSubstNodes = tokenB.getEndSubstNodes();
        // Nu hebben we een end subst node te pakken
        // nu moet ik alle bij behorende adds en dels zien te vinden..
        // dat zijn de kinderen van de betreffende subst

//                    // Debug code
//                    Stream<WitnessNode> childNodes = endSubstNodes.children();
//                    childNodes.forEach(System.out::println);

        Stream<WitnessNode> childNodes = endSubstNodes.children();
        // ik moet eigenlijk filteren maar dat ga ik nu even niet doen
        // Van alle child nodes moet ik daar dan weer de laatste child van pakken
        // Daarna mappen we die childnodes naar cell coordinaten
        // in het geval van token in witness B naar Y coordinates
        List<Integer> yCoordinatesWithScoresOfAddDels = childNodes.map(WitnessNode::getLastChild).map(nodeToYCoordinate::get).collect(Collectors.toList());
//                    System.out.println("All the possible cell containing the scores of the options of this subst are: "+yCoordinatesWithScoresOfAddDels);
        // now we have to find the maximum scoring cell of the possible cells..
        // TODO; in the future we also have to set the parent coordinates correctly
        // convert into scores;
        Optional<Score> maximumScoreOptional = yCoordinatesWithScoresOfAddDels.stream().map(addDelY -> cells[addDelY][x]).max((score, other) -> score.globalScore - other.globalScore);
        Score bestScore = maximumScoreOptional.get();
        // because it is the end of a subst; override the current score in the cell with the best score for the whole subst.
        cells[y][x] = bestScore;
    }

    private int getPreviousCoordinateForLabel(Map<WitnessNode, Integer> nodeToCoordinate, EditGraphTableLabel token, int defaultCoordinate) {
        if (token.containsStartAddOrDel()) {
            // start of an option (add / del)
            // every edit graph table label is associated with witness node (as the text)
            // we need to walk to the parent
            WitnessNode currentNode = token.text;
            Stream<WitnessNode> parentNodeStream = currentNode.parentNodeStream();
            Optional<WitnessNode> substOptional = parentNodeStream.filter(node -> node.data.equals("subst")).findFirst();
            if (!substOptional.isPresent()) {
                throw new RuntimeException("We found an OR operand but could not find the OR operator!");
            }
            WitnessNode substNode = substOptional.get();
            // convert the substNode into index in the edit graph table
            Integer index = nodeToCoordinate.get(substNode);
            defaultCoordinate = index - 1;
            // debug
            // System.out.println("Label text on the horizontal axis >"+tokenA.text+"< maps to index "+previousX);
        }
        return defaultCoordinate;
    }

    private Map<WitnessNode, Integer> mapNodesToIndex(List<EditGraphTableLabel> labels) {
        // the following code has side effects. That is because Java 8 does not have an enumerate function for streams.
        Map<WitnessNode, Integer> nodesToCoordinate = new HashMap<>();

        // calculate the y and x coordinates of the witness nodes in the table (so we can map from one to the other)
        IntStream.range(0, labels.size()).forEach(index -> {
            // map index to label
            EditGraphTableLabel label = labels.get(index);
            // we take the nodes that are opened at this label and map the nodes to the index
            label.startElements.forEach(node -> nodesToCoordinate.put(node, index+1));
            // we take the text node at this label and map it to the index
            nodesToCoordinate.put(label.text, index+1);
        });

        return nodesToCoordinate;
    }

    public void align() {

    }


    private static class LabelCollector implements java.util.stream.Collector<WitnessNode.WitnessNodeEvent, EditGraphTableLabel, EditGraphTableLabel> {
        @Override
        public Supplier<EditGraphTableLabel> supplier() {
            return EditGraphTableLabel::new;
        }

        @Override
        public BiConsumer<EditGraphTableLabel, WitnessNode.WitnessNodeEvent> accumulator() {
            return (label, event) ->  {
                switch (event.type) {
                    case START:
                        label.addStartEvent(event.node);
                        break;
                    case END:
                        label.addEndEvent(event.node);
                        break;
                    case TEXT:
                        label.addTextEvent(event.node);
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
        List<WitnessNode> startElements = new ArrayList<>();
        private List<WitnessNode> endElements = new ArrayList<>();
        WitnessNode text;

        public void addStartEvent(WitnessNode node) {
            startElements.add(node);
        }

        public void addEndEvent(WitnessNode node) {
            endElements.add(node);
        }

        public void addTextEvent(WitnessNode node) {
            if (text != null) {
                throw new UnsupportedOperationException();
            }
            this.text = node;
        }

        @Override
        public String toString() {
            String a = startElements.stream().map(WitnessNode::toString).collect(Collectors.joining(", "));
            String b = text.toString();
            String c = endElements.stream().map(WitnessNode::toString).collect(Collectors.joining(", "));
            return b+":"+a+":"+c;
        }

        public boolean containsStartAddOrDel() {
            // find the first add or del tag...
            // Note that this implementation is from the left to right
            Optional<WitnessNode> witnessNode = startElements.stream().filter(node -> node.data.equals("add") || node.data.equals("del")).findFirst();
            return witnessNode.isPresent();
        }

        public boolean containsEndSubst() {
            // find the first end subst tag...
            // if we want to do this completely correct it should be from right to left
            Optional<WitnessNode> witnessNode = endElements.stream().filter(node -> node.data.equals("subst")).findFirst();
            return witnessNode.isPresent();
        }

        public WitnessNode getEndSubstNodes() {
            Optional<WitnessNode> witnessNode = endElements.stream().filter(node -> node.data.equals("subst")).findFirst();
            if (!witnessNode.isPresent()) {
                throw new RuntimeException("We expected one or more subst tags here!");
            }
            return witnessNode.get();
        }
    }

    public class Score {
        //TODO: set parent
        int globalScore = 0;

        public Score(int i) {
            this.globalScore = i;
        }

        public Score(Score parent) {
            this.globalScore = parent.globalScore;
        }
    }

    private class Scorer {
        public Score gap(Score parent) {
            return new Score(parent.globalScore -1);
        }

        public Score score(Score parent, EditGraphTableLabel tokenB, EditGraphTableLabel tokenA) {
            if (tokenB.text.data.equals(tokenA.text.data)) {
                return new Score(parent);
            } else {
                return new Score(parent.globalScore - 2);
            }
        }
    }
}
