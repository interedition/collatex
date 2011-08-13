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
