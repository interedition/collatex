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
package eu.interedition.text.rdbms;

import com.google.common.collect.Sets;
import eu.interedition.text.AbstractTextTest;
import eu.interedition.text.Name;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NameTest extends AbstractTextTest {
  final SortedSet<Name> TEST_NAMES = Sets.newTreeSet(Sets.newHashSet(
          new Name(null, "noNamespaceName"),
          new Name(TEST_NS, "namespacedName")
  ));

  @Autowired
  private SessionFactory sessionFactory;

  @Test
  public void getNames() {
    Assert.assertEquals(2, Name.get(sessionFactory.getCurrentSession(), TEST_NAMES).size());
  }
}
