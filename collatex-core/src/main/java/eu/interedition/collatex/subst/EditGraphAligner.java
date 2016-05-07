package eu.interedition.collatex.subst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by ronalddekker on 30/04/16.
 * This is a special version of the edit graph aligner that can handle witnesses with substitutions in them.
 */
public class EditGraphAligner {

    public static List<EditGraphTableLabel> createLabels(WitnessTree.WitnessNode wit_a) {
        Stream<WitnessTree.WitnessNodeEvent> nodeEventStream = wit_a.depthFirstNodeEventStream();
        // I want to group all the open , text and close events together in a group
        // I first try to do that with a reduce operation
        // but for that to work (left and right) have to be of the same type
        // we might be able to accomplish the same thing with a collect operator
        // Otherwise we have to fall back to the StreamEx extension package.
        // BiPredicate<WitnessTree.WitnessNodeEvent, WitnessTree.WitnessNodeEvent> predicate = (event1, event2) -> event1.getClass().equals(event2.getClass());
        // Two rules:
        // 1. When two text tokens follow each other we should not group them together
        // 1b. A text token followed by anything other than a close tag should not be grouped together
        // 2. When a close tag is followed by an open tag we should not group them together
        // 2b. When a close tag is followed by anything other than a close tag we should not group them together
        BiPredicate<WitnessTree.WitnessNodeEvent, WitnessTree.WitnessNodeEvent> predicate = (event1, event2) -> {
            if (event1.getClass().equals(WitnessTree.WitnessNodeTextEvent.class) && !event2.getClass().equals(WitnessTree.WitnessNodeEndElementEvent.class)) {
                return false;
            }
            if (event1.getClass().equals(WitnessTree.WitnessNodeEndElementEvent.class) && !event2.getClass().equals(WitnessTree.WitnessNodeEndElementEvent.class)) {
                return false;
            }
            return true;
        };

        List<List<WitnessTree.WitnessNodeEvent>> lists = nodeEventStream.collect(new GroupOnPredicateCollector<>(predicate));
        Stream<EditGraphTableLabel> editGraphTableLabelStream = lists.stream().map(list -> list.stream().collect(new LabelCollector()));
        return editGraphTableLabelStream.collect(Collectors.toList());
    }




    public EditGraphAligner() {

   }

    public void align() {

    }


    private static class LabelCollector implements java.util.stream.Collector<WitnessTree.WitnessNodeEvent, EditGraphTableLabel, EditGraphTableLabel> {
        @Override
        public Supplier<EditGraphTableLabel> supplier() {
            return () -> new EditGraphTableLabel();
        }

        @Override
        public BiConsumer<EditGraphTableLabel, WitnessTree.WitnessNodeEvent> accumulator() {
            return (label, event) ->  {
                if (event instanceof WitnessTree.WitnessNodeStartElementEvent) {
                    label.addStartEvent((WitnessTree.WitnessNodeStartElementEvent)event);
                } else if (event instanceof WitnessTree.WitnessNodeEndElementEvent) {
                    label.addEndEvent((WitnessTree.WitnessNodeEndElementEvent)event);
                } else if (event instanceof WitnessTree.WitnessNodeTextEvent) {
                    label.addTextEvent((WitnessTree.WitnessNodeTextEvent)event);
                } else {
                    throw new UnsupportedOperationException();
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
        private List<WitnessTree.WitnessNodeStartElementEvent> startElements = new ArrayList<>();
        private List<WitnessTree.WitnessNodeEndElementEvent> endElements = new ArrayList<>();
        private WitnessTree.WitnessNodeTextEvent text;

        public void addStartEvent(WitnessTree.WitnessNodeStartElementEvent event) {
            startElements.add(event);
        }

        public void addEndEvent(WitnessTree.WitnessNodeEndElementEvent event) {
            endElements.add(event);
        }

        public void addTextEvent(WitnessTree.WitnessNodeTextEvent event) {
            if (text != null) {
                throw new UnsupportedOperationException();
            }
            this.text = event;
        }

        @Override
        public String toString() {
            String a = startElements.stream().map(element -> element.toString()).collect(Collectors.joining(", "));
            String b = text.toString();
            String c = endElements.stream().map(element -> element.toString()).collect(Collectors.joining(", "));
            return a+":"+b+":"+c;
        }
    }
}
