package eu.interedition.collatex.subst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * Created by ronalddekker on 01/05/16.
 */
// T is the incoming type; in our case WitnessTreeNode
// U Is the intermediate container;
// V is the resulting container
public class GroupOnPredicateCollector<T> implements Collector<T, List<List<T>>, List<List<T>>> {

    private final BiPredicate<T, T> predicate;

    public GroupOnPredicateCollector(BiPredicate<T, T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public Supplier<List<List<T>>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<List<T>>, T> accumulator() {
        return (lists, t) -> {
            if (lists.isEmpty()) {
                List<T> container = new ArrayList<>();
                container.add(t);
                lists.add(container);
            } else {
                List<T> lastList = lists.get(lists.size()-1);
                T lastItem = lastList.get(lastList.size() - 1);
                if (this.predicate.test(lastItem, t)) {
                    lastList.add(t);
                } else {
                    List<T> container = new ArrayList<>();
                    container.add(t);
                    lists.add(container);
                }
            }
        };
    }

    @Override
    public BinaryOperator<List<List<T>>> combiner() {
        return (t1, t2) -> {
            throw new UnsupportedOperationException("not possible!");
        };
    }

    @Override
    public Function<List<List<T>>, List<List<T>>> finisher() {
        return o -> o;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }
}
