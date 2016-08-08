package eu.interedition.collatex.util;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;

public interface AlignmentTable extends List<SortedMap<Witness, Set<Token>>> {
}
