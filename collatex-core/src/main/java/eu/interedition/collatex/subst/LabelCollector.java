package eu.interedition.collatex.subst;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public class LabelCollector implements java.util.stream.Collector<WitnessNode.WitnessNodeEvent, EditGraphTableLabel, EditGraphTableLabel> {
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
