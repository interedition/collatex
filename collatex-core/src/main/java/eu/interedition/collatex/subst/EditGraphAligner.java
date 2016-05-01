package eu.interedition.collatex.subst;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * Created by ronalddekker on 30/04/16.
 * This is a special version of the edit graph aligner that can handle witnesses with substitutions in them.
 */
public class EditGraphAligner {

    public static List<List<WitnessTree.WitnessNodeEvent>> createLabels(WitnessTree.WitnessNode wit_a) {
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
        return lists;
    }




    public EditGraphAligner() {

   }

    public void align() {

    }


}
