package eu.interedition.text.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Properties;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ConfigurationViewModelProvider extends HandlerInterceptorAdapter {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationViewModelProvider.class);

  private Properties configuration;

  @Required
  public void setConfiguration(Properties configuration) {
    this.configuration = configuration;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    if (modelAndView == null) {
      return;
    }

    if (modelAndView.isReference() && modelAndView.getViewName().startsWith("redirect:")) {
      return;
    }

    if (!modelAndView.isReference() && RedirectView.class.isAssignableFrom(modelAndView.getView().getClass())) {
      return;
    }

    modelAndView.addObject("config", configuration);
  }


}
