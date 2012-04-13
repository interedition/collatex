/*
 * #%L
 * Text Repository: Datastore for texts based on Interedition's model.
 * %%
 * Copyright (C) 2010 - 2011 The Interedition Development Group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.interedition.web.io;

import com.google.common.base.Preconditions;
import eu.interedition.text.TextRange;
import eu.interedition.text.TextTarget;
import org.springframework.core.convert.converter.Converter;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RangeConverter implements Converter<String, TextRange> {
  @Override
  public TextRange convert(String source) {
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

    return new TextRange(start, end);
  }

  private static long toLong(String str) {
    final long value = Long.valueOf(str);
    Preconditions.checkArgument(value >= 0);
    return value;
  }
}
