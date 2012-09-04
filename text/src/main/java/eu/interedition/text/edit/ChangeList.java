package eu.interedition.text.edit;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import eu.interedition.text.TextRange;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ChangeList implements Iterable<Change> {
  protected final LinkedList<Change> changes = Lists.newLinkedList();
  protected final long prevLength;
  protected long nextLength = 0;

  public ChangeList(long prevLength) {
    this.prevLength = prevLength;
  }

  public ChangeList(long prevLength, Iterable<? extends Change> c) {
    this(prevLength);
    append(c);
  }

  public ChangeList(long prevLength, Change... changes) {
    this(prevLength);
    append(changes);
  }

  @Override
  public Iterator<Change> iterator() {
    return Iterators.unmodifiableIterator(changes.iterator());
  }


  public long prevLength() {
    return prevLength;
  }

  public long nextLength() {
    return nextLength;
  }

  public ChangeList combineWith(ChangeList other) {
    if (nextLength != other.prevLength) {
      throw new IllegalArgumentException("Cannot combine " + other + " with " + this);
    }
    return new ChangeList(prevLength, changes);
  }

  public Iterable<Change> getRemovals() {
    return new Iterable<Change>() {
      private Iterable<Change> retains = Iterables.filter(changes, Change.RETAIN_PREDICATE);
      @Override
      public Iterator<Change> iterator() {
        return new AbstractIterator<Change>() {

          private Iterator<Change> it = retains.iterator();
          private TextRange last = null;

          @Override
          protected Change computeNext() {
            while (it.hasNext()) {
              final TextRange next = it.next().getRange();
              TextRange removed = null;
              if (last == null && next.getStart() > 0) {
                removed = new TextRange(0, next.getStart());
              }
              if (last != null && last.getEnd() < next.getStart()) {
                removed = new TextRange(last.getEnd(), next.getStart());
              }
              last = next;
              if (removed != null) {
                return new Change(null, removed, Change.Type.DELETE);
              }
            }
            if (last == null) {
              last = new TextRange(0, prevLength);
              return (last.length() > 0 ? new Change(null, last, Change.Type.DELETE) : endOfData());
            } else if (last.getEnd() < prevLength) {
              last = new TextRange(last.getEnd(), prevLength);
              return new Change(null, last, Change.Type.DELETE);
            }

            return endOfData();
          }
        };
      }
    };
  }

  public ChangeList insert(String content) {
    append(new Change(content, new TextRange(nextLength, nextLength + content.length()), Change.Type.INSERT));
    return this;
  }

  public ChangeList retain(long start, long end) {
    append(new Change(null, new TextRange(start, end), Change.Type.RETAIN));
    return this;
  }

  public ChangeList append(Iterable<? extends Change> changes) {
    Change lastChange = null;
    boolean lastIsRetain = false;
    for (Change toAppend : changes) {
      final boolean toAppendIsRetain = (toAppend.getType() == Change.Type.RETAIN);
      if (lastIsRetain && toAppendIsRetain) {
        if (lastChange.getRange().getEnd() >= toAppend.getRange().getStart()) {
          // change lists have to be compact
          throw new IllegalArgumentException(this + " + " + toAppend);
        }
      }
      if (toAppendIsRetain) {
        final TextRange retain = toAppend.getRange();
        if (retain.getStart() >= prevLength || retain.getEnd() > prevLength) {
          throw new IllegalArgumentException(this + " + " + toAppend);
        }
      }
      this.changes.add(toAppend);
      this.nextLength += toAppend.length();

      lastIsRetain = toAppendIsRetain;
      lastChange = toAppend;
    }
    return this;
  }

  public ChangeList append(Change... changes) {
    return append(Arrays.asList(changes));
  }

  @Override
  public String toString() {
    return new StringBuilder("{|").append(prevLength).append("| ==> ").append(Iterables.toString(changes)).append("}").toString();
  }
}
