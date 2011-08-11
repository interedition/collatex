package eu.interedition.text.repository;

import com.google.common.base.Preconditions;
import eu.interedition.text.Range;
import org.springframework.core.convert.converter.Converter;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RangeConverter implements Converter<String, Range>{
  @Override
  public Range convert(String source) {
    int start = 0;
    int end = 0;

    final String[] components = source.trim().split(",");
    if (components.length > 0) {
      end = toInt(components[0]);
    }
    if (components.length > 1) {
      start = end;
      end = start + toInt(components[1]);
    }

    return new Range(start, end);
  }

  private static int toInt(String str) {
    final int value = Integer.valueOf(str);
    Preconditions.checkArgument(value >= 0);
    return value;
  }
}
