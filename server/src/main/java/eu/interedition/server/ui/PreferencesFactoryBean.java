package eu.interedition.server.ui;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

import java.util.prefs.Preferences;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
public class PreferencesFactoryBean extends AbstractFactoryBean<Preferences> {

  @Override
  public Class<?> getObjectType() {
    return Preferences.class;
  }

  @Override
  protected Preferences createInstance() throws Exception {
    return Preferences.userNodeForPackage(getClass());
  }
}
