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
package eu.interedition.text;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import eu.interedition.text.util.SQL;
import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Comparator;
import java.util.Set;

@Cacheable
@Entity
@Table(name = "interedition_name", uniqueConstraints = @UniqueConstraint(name = "qname_constraint", columnNames = {"local_name", "ns"}))
public class Name implements Comparable<Name> {
  protected long id;
  protected URI namespace;
  protected String localName;

  public Name() {
  }

  public Name(URI namespace, String localName) {
    this.namespace = namespace;
    this.localName = localName;
  }

  public Name(QName name) {
    final String nsStr = Strings.emptyToNull(name.getNamespaceURI());
    this.namespace = (nsStr == null ? null : URI.create(nsStr));
    this.localName = name.getLocalPart();
  }

  @Id
  @GeneratedValue(generator = "idGenerator")
  @GenericGenerator(name = "idGenerator",
          strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
          parameters = {
                  @Parameter(name = "optimizer", value = "hilo"),
                  @Parameter(name = "increment_size", value = "10")
          }
  )
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @Transient
  public URI getNamespace() {
    return namespace;
  }

  public void setNamespace(URI namespace) {
    this.namespace = namespace;
  }

  @Column(name = "ns", length = 128)
  public String getNamespaceURI() {
    return (namespace == null ? null : namespace.toString());
  }

  public void setNamespaceURI(String namespaceURI) {
    this.namespace = (namespaceURI == null ? null : URI.create(namespaceURI));
  }

  @Column(name = "local_name", nullable = false, length = 64)
  public String getLocalName() {
    return localName;
  }

  public void setLocalName(String localName) {
    this.localName = localName;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Name) {
      final Name other = (Name) obj;
      return Objects.equal(localName, other.localName) && Objects.equal(namespace, other.namespace);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(localName, namespace);
  }

  @Override
  public int compareTo(Name o) {
    return COMPARATOR.compare(this, o);
  }

  @Override
  public String toString() {
    return (namespace == null ? localName : new StringBuilder("{").append(namespace).append("}").append(localName).toString());
  }

  public static Name fromString(String str) {
    if (!str.startsWith("{")) {
      return new Name(null, str);
    }

    final int namespaceEnd = str.indexOf("}");
    Preconditions.checkArgument(namespaceEnd >= 1 && namespaceEnd < str.length() - 1);
    if (namespaceEnd == 1) {
      return new Name(null, str);
    } else {
      return new Name(URI.create(str.substring(1, namespaceEnd)), str.substring(namespaceEnd + 1));
    }
  }

  public static Set<Name> get(Session session, Set<Name> names) {
    final Set<Name> result = Sets.newHashSet();
    if (names.isEmpty()) {
      return result;
    }

    final Set<Name> toFind = Sets.newHashSet(names);
    final Disjunction or = Restrictions.disjunction();
    for (Name name : toFind) {
      final Conjunction and = Restrictions.conjunction();
      and.add(Restrictions.eq("localName", name.getLocalName()));

      final URI ns = name.getNamespace();
      and.add(ns == null ? Restrictions.isNull("namespaceURI") : Restrictions.eq("namespaceURI", ns.toString()));

      or.add(and);
    }

    for (Name name : SQL.iterate(session.createCriteria(Name.class).add(or), Name.class)) {
      toFind.remove(name);
      result.add(name);
    }
    for (Name name : toFind) {
      result.add((Name) session.merge(name));
    }
    return result;
  }

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

}
