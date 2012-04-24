package eu.interedition.text;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.util.SortedSet;

/**
 * Adresses a text segment, for example a segment, that is annotated by some {@link Annotation annotation}.
 * <p/>
 * <p/>
 * <p/>
 * Segments are adressed by start and end offsets of the characters forming the boundaries of a segment. The character pointed to by
 * the start offset is included in the segment, while the character addressed by the end offset is the first excluded from it.
 * <p/>
 * <p/>
 * <p/>
 * Offsets are counted from zero and are located in the <i>gaps</i> between characters:
 * <p/>
 * <pre>
 *   a   b   c   d   e
 * 0 | 1 | 2 | 3 | 4 | 5
 * </pre>
 * <p/>
 * In the given example, the substring "bcd" would be adressed by the segment <code>[1, 4]</code>, the whole string by the segment
 * <code>[0, 5]</code>. Note that the difference between the offsets equals the length of the segment and that "empty" segments
 * pointing in the gaps between characters are valid. So for example to point to the gap between "d" and "e", the corresponding
 * empty segment's address would be <code>[4, 4]</code>.
 * <p/>
 * <p/>
 * <p/>
 * Apart from encapsulating the offset values denoting the segment, objects of this class also have methods to apply <a href=
 * "http://www.mind-to-mind.com/library/papers/ara/core-range-algebra-03-2002.pdf" title="Nicol: Core Range Algebra (PDF)">Gavin
 * Nicols' Core Range Algebra</a>. These methods like {@link #encloses(TextRange)} or {@link #hasOverlapWith(TextRange)} define
 * relationships between text segments, which can be used for example to filter sets of range annotations.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 * @see CharSequence#subSequence(int, int)
 */
public class TextRange implements Comparable<TextRange> {
  public static final TextRange NULL = new TextRange(0, 0);

  /**
   * The start offset of the segment (counted from zero, inclusive).
   */
  protected long start;

  /**
   * The end offset of the segment (counted from zero, exclusive).
   */
  protected long end;

  public TextRange() {
  }

  /**
   * Creates a text segment address.
   *
   * @param start start offset
   * @param end   end offset
   * @throws IllegalArgumentException if <code>start</code> or <code>end</code> or lower than zero, or if <code>start</code> is greather than
   *                                  <code>end</code>
   */
  public TextRange(long start, long end) {
    if (start < 0 || end < 0 || start > end) {
      throw new IllegalArgumentException(toString(start, end));
    }
    this.start = start;
    this.end = end;
  }

  /**
   * Copy constructor.
   *
   * @param b the segment address to be copied
   */
  public TextRange(TextRange b) {
    this(b.start, b.end);
  }

  public long getStart() {
    return start;
  }

  public void setStart(long start) {
    this.start = start;
  }

  public long getEnd() {
    return end;
  }

  public void setEnd(long end) {
    this.end = end;
  }

  /**
   * The length of the adressed segment.
   *
   * @return the length (difference between start and end offset)
   */
  public long length() {
    return end - start;
  }

  /**
   * <i>a.start &lt;= b.start and a.end &gt;= b.end</i>
   *
   * @param b b range
   * @return <code>true</code>/<code>false</code>
   */
  public boolean encloses(TextRange b) {
    return (start <= b.start) && (end >= b.end);
  }

  /**
   * <i>a.start = b.start and a.end &gt; b.end</i>
   *
   * @param b b range
   * @return <code>true</code>/<code>false</code>
   */
  public boolean enclosesWithSuffix(TextRange b) {
    return (start == b.start) && (end > b.end);
  }

  /**
   * <i>a.start &lt; b.start and a.end = b.end</i>
   *
   * @param b b range
   * @return <code>true</code>/<code>false</code>
   */
  public boolean enclosesWithPrefix(TextRange b) {
    return (start < b.start) && (end == b.end);
  }

  /**
   * <i>(a <> b) and a.start &gt; b.start and a.end &lt;= b.end</i>
   *
   * @param b b range
   * @return <code>true</code>/<code>false</code>
   */
  public boolean fitsWithin(TextRange b) {
    return !equals(b) && (start >= b.start) && (end <= b.end);
  }

  /**
   * <i>overlap(a, b) &gt; 0</i>
   *
   * @param b b range
   * @return <code>true</code>/<code>false</code>
   */
  public boolean hasOverlapWith(TextRange b) {
    final TextRange overlap = overlap(b);
    return (overlap != null) && (overlap.length() > 0);
  }

  /**
   * Yields the overlapping segment of this and another segment.
   *
   * @param b another segment
   * @return <i>[max(a.start, b.start), min(a.end, b.end)]</i>
   */
  public TextRange intersectionWith(TextRange b) {
    return new TextRange(Math.max(start, b.start), Math.min(end, b.end));
  }

  /**
   * <i>min(a.end, b.end) - max(a.start, b.start)</i>
   *
   * @param b b range
   * @return length of overlap
   */
  public TextRange overlap(TextRange b) {
    final long start = Math.max(this.start, b.start);
    final long end = Math.min(this.end, b.end);
    return ((end - start) >= 0 ? new TextRange(start, end) : null);
  }

  /**
   * <i>b.start &gt;= a.end</i>
   *
   * @param b b range
   * @return <code>true</code>/<code>false</code>
   */
  public boolean precedes(TextRange b) {
    return b.start >= end;
  }

  /**
   * <i>a.start &gt;= (b.end - 1)</i>
   *
   * @param b b range
   * @return <code>true</code>/<code>false</code>
   */
  public boolean follows(TextRange b) {
    return (start >= (b.end - 1));
  }

  public TextRange shift(long delta) {
    return new TextRange(start + delta, end + delta);
  }

  /**
   * Orders segments, first by start offset, then by the reverse order of the end offsets.
   *
   * @see Comparable#compareTo(Object)
   */
  public int compareTo(TextRange o) {
    final long result = (start == o.start ? o.end - end : start - o.start);
    return (result < 0 ? -1 : (result > 0 ? 1 : 0));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(start, end);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof TextRange)) {
      return super.equals(obj);
    }

    TextRange b = (TextRange) obj;
    return (this.start == b.start) && (this.end == b.end);
  }

  /**
   * Creates a string representation of an offset pair.
   *
   * @param start start offset
   * @param end   end offset
   * @return string representation
   */
  public static String toString(long start, long end) {
    return "[" + start + ", " + end + "]";
  }

  @Override
  public String toString() {
    return toString(start, end);
  }

  public SortedSet<TextRange> substract(TextRange subtrahend) {
    Preconditions.checkArgument(hasOverlapWith(subtrahend));

    final SortedSet<TextRange> remainders = Sets.newTreeSet();
    if (fitsWithin(subtrahend)) {
      return remainders;
    }
    if (enclosesWithPrefix(subtrahend)) {
      remainders.add(new TextRange(subtrahend.start, end));
    } else if (enclosesWithSuffix(subtrahend)) {
      remainders.add(new TextRange(start, subtrahend.end));
    } else {
      remainders.add(new TextRange(start, subtrahend.start));
      remainders.add(new TextRange(subtrahend.end, end));
    }

    return remainders;
  }

}
