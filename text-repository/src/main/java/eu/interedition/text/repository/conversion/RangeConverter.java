package eu.interedition.text.repository.conversion;

import com.google.common.base.Preconditions;
import eu.interedition.text.Range;
import org.springframework.core.convert.converter.Converter;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RangeConverter implements Converter<String, Range>{
  @Override
  public Range convert(String source) {
    long start = 0;
    long end = 0;

    final String[] components = source.trim().split(",");
    if (components.length > 0) {
      end = toLong(components[0]);
    }
    if (components.length > 1) {
      start = end;
      end = start + toLong(components[1]);
    }

    return new Range(start, end);
  }

  private static long toLong(String str) {
    final long value = Long.valueOf(str);
    Preconditions.checkArgument(value >= 0);
    return value;
  }
}
