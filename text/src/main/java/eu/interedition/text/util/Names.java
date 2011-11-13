/*
 * #%L
 * Text: A text model with range-based markup via standoff annotations.
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
package eu.interedition.text.util;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import eu.interedition.text.Name;
import eu.interedition.text.mem.SimpleName;

import java.net.URI;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Names {
  public static final Comparator<Name> COMPARATOR = new Comparator<Name>() {

    public int compare(Name o1, Name o2) {
      final URI o1Ns = o1.getNamespace();
      final URI o2Ns = o2.getNamespace();

      final String o1LocalName = o1.getLocalName();
      final String o2LocalName = o2.getLocalName();

      if (o1Ns != null && o2Ns != null) {
        final int nsComp = o1Ns.compareTo(o2Ns);
        return (nsComp == 0 ? o1LocalName.compareTo(o2LocalName) : nsComp);
      } else if (o1Ns == null && o2Ns == null) {
        return o1LocalName.compareTo(o2LocalName);
      } else {
        return (o1Ns == null ? 1 : -1);
      }
    }
  };

  public static final Function<Name,String> TO_STRING = new Function<Name, String>() {
    @Override
    public String apply(Name input) {
      return Names.toString(input);
    }
  };

  public static boolean equal(Name name1, Name name2) {
    return Objects.equal(name1.getLocalName(), name2.getLocalName())
            && Objects.equal(name1.getNamespace(), name2.getNamespace());
  }

  public static int hashCode(Name name) {
    return Objects.hashCode(name.getLocalName(), name.getNamespace());
  }


  public static String toString(Name name) {
    final URI ns = name.getNamespace();
    return "{" + (ns == null ? "" : ns) + "}" + name.getLocalName();
  }

  public static Name fromString(String nameStr) {
    final Matcher matcher = NAME_PATTERN.matcher(nameStr);
    Preconditions.checkArgument(matcher.matches(), nameStr);
    return new SimpleName(matcher.group(1), matcher.group(2));
  }

  private static final Pattern NAME_PATTERN = Pattern.compile("^\\{([^\\}]+)\\}(.+)$");
}
