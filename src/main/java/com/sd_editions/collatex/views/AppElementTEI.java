package com.sd_editions.collatex.views;

import com.sd_editions.collatex.match.views.AppElement;
import com.sd_editions.collatex.match.views.Element;
import com.sd_editions.collatex.permutations.Gap;

/**
 *  Apparatus element serializing to the output format specified in ticket #6. 
 *  
 *  TODO This should probably merged with {@link AppElement}, but doing so immediately would
 *       break support for the old output format
 */
public class AppElementTEI extends Element {

  public Gap getBase() {
    return base;
  }

  public Gap getWitness() {
    return witness;
  }

  private final Gap base;
  private final Gap witness;

  public AppElementTEI(Gap _base, Gap _witness) {
    this.base = _base;
    this.witness = _witness;
  }

  @Override
  public String toXML() {
    StringBuilder xml = new StringBuilder("<app>");
    if (base == null) {
      xml.append(base.toString());
    } else {

      xml.append("<rdg wit=\"#").append(base.getWitness().id).append('"');
      if (base.toString().isEmpty())
        xml.append("/>");
      else
        xml.append(">").append(base.toString()).append("</rdg>");

      xml.append("<rdg wit=\"#").append(witness.getWitness().id).append('"');
      if (witness.toString().isEmpty())
        xml.append("/>");
      else
        xml.append('>').append(witness.toString()).append("</rdg>");

    }
    xml.append("</app>");
    return xml.toString();
  }
}
