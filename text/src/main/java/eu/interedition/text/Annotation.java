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

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 */
@Entity
@Table(name = "interedition_annotation")
public class Annotation {

  protected long id;
  protected Name name;
  protected Set<TextTarget> targets;
  protected byte[] data;
  protected JsonNode dataNode;

  public Annotation() {
  }

  public Annotation(Name name, TextTarget target, JsonNode data) {
    setName(name);
    setTarget(target);
    setData(data);
  }

  @Id
  @GeneratedValue(generator = "idGenerator")
  @GenericGenerator(name = "idGenerator",
          strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
          parameters = {
                  @Parameter(name = "optimizer", value = "hilo"),
                  @Parameter(name = "increment_size", value = "100")
          }
  )
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "name_id", nullable = false)
  public Name getName() {
    return name;
  }

  public void setName(Name name) {
    this.name = name;
  }

  @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "interedition_annotation_target", joinColumns = @JoinColumn(name = "annotation_id", nullable = false))
  public Set<TextTarget> getTargets() {
    return targets;
  }

  public void setTargets(Set<TextTarget> targets) {
    this.targets = targets;
  }

  @Transient
  public TextTarget getTarget() {
    return Iterables.getOnlyElement(getTargets());
  }

  public void setTarget(TextTarget target) {
    setTargets(Sets.newHashSet(target));
  }
  public Iterable<TextTarget> of(Text text) {
    return Iterables.filter(getTargets(), TextTarget.of(text));
  }

  @Lob
  @Column(name = "data", length = 65535)
  public byte[] getRawData() {
    return data;
  }

  public void setRawData(byte[] rawData) {
    this.data = rawData;
  }

  @Transient
  public JsonNode getData() {
    if (dataNode == null) {
      try {
        dataNode = (data == null || data.length == 0 ? EMPTY_DATA_NODE : JSON.readTree(new ByteArrayInputStream(data)));
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }
    return dataNode;
  }

  public void setData(JsonNode data) {
    this.dataNode = data;
    try {
      this.data = (data == null ? EMPTY_DATA : JSON.writeValueAsBytes(data));
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  protected Objects.ToStringHelper toStringHelper() {
    return Objects.toStringHelper(this).addValue(getName()).addValue(getData());
  }

  @Override
  public boolean equals(Object obj) {
    if (id != 0 && obj != null && obj instanceof Annotation) {
      return id == ((Annotation) obj).id;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return (id == 0 ? super.hashCode() : Objects.hashCode(id));
  }

  @Override
  public String toString() {
    return toStringHelper().addValue(getId()).toString();
  }

  public static Iterable<Annotation> create(Session session, Iterable<Annotation> annotations) {
    final Map<Name, Name> resolvedNames = Maps.newHashMap();
    for (Name name : Name.get(session, Sets.newHashSet(Iterables.transform(annotations, Annotation.NAME)))) {
      resolvedNames.put(name, name);
    }

    final List<Annotation> created = Lists.newArrayList();
    for (Annotation annotation : annotations) {
      annotation.setName(Preconditions.checkNotNull(resolvedNames.get(annotation.getName())));
      created.add((Annotation) session.merge(annotation));
    }
    return created;
  }

  public static Iterable<Annotation> create(Session session, Annotation... annotations) {
    return create(session, Arrays.asList(annotations));
  }

  public static Ordering<Annotation> orderByText(final Text text) {
    return Ordering.from(new Comparator<Annotation>() {
      @Override
      public int compare(Annotation o1, Annotation o2) {
        final Iterator<TextTarget> o1It = Sets.newTreeSet(o1.of(text)).iterator();
        final Iterator<TextTarget> o2It = Sets.newTreeSet(o2.of(text)).iterator();
        while (o1It.hasNext() && o2It.hasNext()) {
          final int result = o1It.next().compareTo(o2It.next());
          if (result != 0) {
            return result;
          }
        }
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
      }
    });
  }

  public static final ObjectMapper JSON = new ObjectMapper();

  private static final JsonNode EMPTY_DATA_NODE = JSON.createObjectNode();
  private static final byte[] EMPTY_DATA = new byte[0];

  public static final Function<Annotation, Name> NAME = new Function<Annotation, Name>() {
    public Name apply(Annotation input) {
      return input.getName();
    }
  };
}
