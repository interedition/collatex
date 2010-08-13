package eu.interedition.collatex2.graphvizrestlet;

import java.io.FileWriter;
import java.io.IOException;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class SVGResource extends ServerResource {
  private static final String INPUT_DOT = "input.dot";

  public SVGResource() {
    getVariants().add(new Variant(MediaType.IMAGE_SVG));
  }

  @Override
  public Representation get(Variant variant) throws ResourceException {
    try {
      Form query = getQuery();
      String dot = query.getFirst("dot").getValue();
      FileWriter toFile = new FileWriter(INPUT_DOT, false);
      toFile.write(dot);
      toFile.close();
      //      String[] cmd = { "cmd", "/c", "mvn" };
      String[] cmd = { "/bin/sh", "-c", "dot -GRankdir=LR -Gid=VariantGraph -Tsvg " + INPUT_DOT };
      Process p = Runtime.getRuntime().exec(cmd);
      return new SVGRepresentation(p.getInputStream());
      //      StringBuffer result = new StringBuffer("");
      //      String s = "";
      //      BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
      //      while ((s = in.readLine()) != null) {
      //        result.append(s);
      //      }
      //      in.close();
      //      return new StringRepresentation(result);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
