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
package eu.interedition.text.repository;

import com.google.common.io.Files;
import eu.interedition.text.rdbms.RelationalNameRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/eu/interedition/text/rdbms/repository-context.xml", "classpath:/eu/interedition/text/repository/service-context.xml"})
public abstract class AbstractTest {
  public static final String INTEREDITION_TEXT_HOME = "interedition.text.home";

  @Autowired
  protected RelationalNameRepository nameRepository;

  @BeforeClass
  public static void createTemporaryDataHome() throws IOException {
    System.setProperty(INTEREDITION_TEXT_HOME, Files.createTempDir().getCanonicalPath());
  }

  @AfterClass
  public static void deleteTemporaryDataHome() throws IOException {
    final String dataHomePath = System.getProperty(INTEREDITION_TEXT_HOME);
    if (dataHomePath != null) {
      final File dataHomeFile = new File(dataHomePath);
      if (dataHomeFile.isDirectory()) {
        Files.deleteRecursively(dataHomeFile);
      }
    }
  }

  @AfterTransaction
  public void clearNameCache() {
    nameRepository.clearCache();
  }
}
