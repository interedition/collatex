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

package eu.interedition.collatex.rest.examples;

import java.io.IOException;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

public class ExampleClientResource {

  public static void main(String[] args) throws IOException, ResourceException {
    // Define our Restlet client resources.  
    ClientResource itemsResource = new ClientResource("http://localhost:8182/firstResource/items");
    ClientResource itemResource = null;

    // Create a new item  
    Item item = new Item("item1", "this is an item.");
    Representation r = itemsResource.post(getRepresentation(item));
    if (itemsResource.getStatus().isSuccess()) {
      itemResource = new ClientResource(r.getIdentifier());
    }

    if (itemResource != null) {
      // Prints the representation of the newly created resource.  
      get(itemResource);

      // Prints the list of registered items.  
      get(itemsResource);

      // Update the item  
      item.setDescription("This is an other description");

      itemResource.put(getRepresentation(item));

      // Prints the list of registered items.  
      get(itemsResource);

      // delete the item  
      itemResource.delete();

      // Print the list of registered items.  
      get(itemsResource);
    }
  }

  /** 
   * Prints the resource's representation. 
   *  
   * @param clientResource 
   *            The Restlet client resource. 
   * @throws IOException 
   * @throws ResourceException 
   */
  public static void get(ClientResource clientResource) throws IOException, ResourceException {
    clientResource.get();
    if (clientResource.getStatus().isSuccess() && clientResource.getResponseEntity().isAvailable()) {
      clientResource.getResponseEntity().write(System.out);
    }
  }

  /** 
   * Returns the Representation of an item. 
   *  
   * @param item 
   *            the item. 
   *  
   * @return The Representation of the item. 
   */
  public static Representation getRepresentation(Item item) {
    // Gathering informations into a Web form.  
    Form form = new Form();
    form.add("name", item.getName());
    form.add("description", item.getDescription());
    return form.getWebRepresentation();
  }

}
