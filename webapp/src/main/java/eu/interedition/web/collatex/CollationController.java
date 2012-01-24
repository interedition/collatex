/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.web.collatex;

import eu.interedition.collatex.graph.GraphFactory;
import eu.interedition.collatex.graph.VariantGraph;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/collate")
public class CollationController implements InitializingBean {
  private static final Logger LOG = LoggerFactory.getLogger(CollationController.class);
  private static final int TWO_HOURS = 7200000;

  @Autowired
  private ScheduledExecutorService taskScheduler;

  @Autowired
  private GraphFactory graphFactory;

  @RequestMapping
  public String redirectToConsole() {
    return "redirect:/collate/console";
  }

  @RequestMapping(method = RequestMethod.POST)
  @ResponseBody
  public VariantGraph graph(@RequestBody Collation collation) throws Exception {
    final Transaction tx = graphFactory.getDatabase().beginTx();
    try {
      // create
      final VariantGraph graph = graphFactory.newVariantGraph();

      // merge
      collation.getAlgorithm().collate(graph, collation.getWitnesses());

      // post-process
      graph.join().rank();

      tx.success();
      return graph;
    } finally {
      tx.finish();
    }
  }

  @RequestMapping("/console")
  public String console() {
    return "collate/console";
  }

  @RequestMapping("/darwin")
  public String darwinExample() {
    return "collate/darwin-example";
  }

  @RequestMapping("/apidocs")
  public String apiDocs() {
    return "collate/apidocs";
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    taskScheduler.scheduleWithFixedDelay(new Runnable() {
      @Override
      public void run() {
        final Transaction tx = graphFactory.getDatabase().beginTx();
        try {
          LOG.debug("Purging graphs older than 2 hours");
          graphFactory.deleteGraphsOlderThan(System.currentTimeMillis() - TWO_HOURS);
          tx.success();
        } finally {
          tx.finish();
        }
      }
    }, 0, TWO_HOURS, TimeUnit.MILLISECONDS);
  }
}
