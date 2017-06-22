package eu.interedition.collatex.util;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by bramb on 22-6-2017.
 */
public class StreamUtil {
    public static <S> Stream<S> stream(Iterator<S> input) {
        return stream(input, false);
    }

    public static <S> Stream<S> parallelStream(Iterator<S> input) {
        return stream(input, true);
    }

    private static <S> Stream<S> stream(Iterator<S> input, boolean parallel) {
        Iterable<S> it = () -> input;
        return StreamSupport.stream(it.spliterator(), parallel);
    }

    public static <S> Stream<S> stream(Iterable<S> input) {
        return stream(input, false);
    }

    public static <S> Stream<S> parallelStream(Iterable<S> input) {
        return stream(input, true);
    }

    private static <S> Stream<S> stream(Iterable<S> input, boolean parallel) {
        return StreamSupport.stream(input.spliterator(), parallel);
    }

}
