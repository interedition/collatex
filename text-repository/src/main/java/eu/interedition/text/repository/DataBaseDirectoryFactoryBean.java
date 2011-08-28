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
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component("dataBaseDirectory")
public class DataBaseDirectoryFactoryBean extends AbstractFactoryBean<File> {
  @Override
  public Class<?> getObjectType() {
    return File.class;
  }

  @Override
  protected File createInstance() throws Exception {
    final File tempDir = Files.createTempDir();
    logger.info("Created data directory " + tempDir);
    return tempDir;
  }

  @Override
  protected void destroyInstance(File instance) throws Exception {
    Files.deleteRecursively(instance);
  }
}
