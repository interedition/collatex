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

package eu.interedition.collatex.web;

import eu.interedition.collatex.implementation.alignment.VariantGraphBuilder;
import eu.interedition.collatex.implementation.graph.db.VariantGraph;
import eu.interedition.collatex.implementation.graph.db.VariantGraphFactory;
import eu.interedition.collatex.interfaces.IWitness;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.SortedSet;

@Controller
@RequestMapping("/")
public class CollationController {

  @Autowired
  private VariantGraphFactory variantGraphFactory;

  @RequestMapping(method = RequestMethod.POST)
  @ResponseBody
  public VariantGraph graph(@RequestBody Collation collation) throws Exception {
    final SortedSet<IWitness> witnesses = collation.getWitnesses();

    final Transaction tx = variantGraphFactory.getDb().beginTx();
    try {
      // create
      final VariantGraph graph = variantGraphFactory.create();

      // merge
      new VariantGraphBuilder(graph).add(witnesses.toArray(new IWitness[witnesses.size()]));

      // post-process
      graph.join().rank();

      tx.success();
      return graph;
    } finally {
      tx.finish();
    }
  }

  @RequestMapping
  public String form() {
    return "collate";
  }

}
