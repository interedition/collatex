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
import eu.interedition.text.QName;

import java.net.URI;
import java.util.Comparator;

public class QNames {
  public static final Comparator<QName> COMPARATOR = new Comparator<QName>() {

    public int compare(QName o1, QName o2) {
      final URI o1Ns = o1.getNamespaceURI();
      final URI o2Ns = o2.getNamespaceURI();

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

  public static final Function<QName,String> TO_STRING = new Function<QName, String>() {
    @Override
    public String apply(QName input) {
      return QNames.toString(input);
    }
  };

  public static boolean equal(QName name1, QName name2) {
    return Objects.equal(name1.getLocalName(), name2.getLocalName())
            && Objects.equal(name1.getNamespaceURI(), name2.getNamespaceURI());
  }

  public static int hashCode(QName name) {
    return Objects.hashCode(name.getLocalName(), name.getNamespaceURI());
  }

  public static String toString(QName name) {
    final URI ns = name.getNamespaceURI();
    return "{" + (ns == null ? "" : ns) + "}" + name.getLocalName();
  }


}
