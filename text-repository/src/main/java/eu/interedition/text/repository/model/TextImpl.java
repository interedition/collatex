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
package eu.interedition.text.repository.model;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import eu.interedition.text.rdbms.RelationalText;

import java.util.Date;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextImpl extends RelationalText {
  private TextCollection collection;
  private Date created;
  private Date updated;
  private String title;
  private String summary;
  private String author;

  public TextImpl(Type type) {
    super(type, "", 0);

  }
  public TextImpl(Type type, long length, byte[] digest, long id) {
    super(type, length, digest, id);
    this.created = this.updated = new Date();
  }

  public TextImpl(RelationalText other) {
    super(other);
  }

  public TextImpl(TextImpl other) {
    super(other);
    this.collection = other.collection;
    this.created = other.created;
    this.updated = other.updated;
    this.title = other.title;
    this.summary = other.summary;
    this.author = other.author;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public Date getUpdated() {
    return updated;
  }

  public void setUpdated(Date updated) {
    this.updated = updated;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public TextCollection getCollection() {
    return collection;
  }

  public void setCollection(TextCollection collection) {
    this.collection = collection;
  }

  public String getDescription() {
    final StringBuilder desc = new StringBuilder();
    if (title != null) {
      if (author != null) {
        desc.append(author).append(": ");
      }
      desc.append(title);
    } else {
      desc.append("Text #").append(getId());
    }
    return desc.toString();
  }

  public boolean isWithoutMetadata() {
    return Iterables.all(Lists.newArrayList(title, summary, author), Predicates.isNull());
  }
}
