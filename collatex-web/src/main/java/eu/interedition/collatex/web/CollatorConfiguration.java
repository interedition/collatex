package eu.interedition.collatex.web;

import eu.interedition.collatex.implementation.graph.GraphFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Configuration
@ComponentScan(basePackageClasses = CollatorConfiguration.class, includeFilters = { @ComponentScan.Filter(Service.class) }, useDefaultFilters = false)
public class CollatorConfiguration {

  @Bean
  public GraphFactory graphFactory() throws IOException {
    return new GraphFactory();
  }

  @Bean
  public ScheduledExecutorService taskScheduler() {
    return Executors.newScheduledThreadPool(42);
  }
}
