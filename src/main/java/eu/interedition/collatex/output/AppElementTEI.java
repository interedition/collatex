package eu.interedition.collatex.output;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sd_editions.collatex.match.views.AppElement;
import com.sd_editions.collatex.match.views.Element;

import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

/**
 *  Apparatus element serializing to the output format specified in ticket #6. 
 *  
 *  TODO This should probably merged with {@link AppElement}, but doing so immediately would
 *       break support for the old output format
 */
public class AppElementTEI extends Element {

  private final Phrase base;
  private final Phrase witness;

  private final Map<String, Phrase> phrases;

  public AppElementTEI(Phrase _base, Phrase _witness) {
    this.base = _base;
    this.witness = _witness;
    this.phrases = Maps.newHashMap();
  }

  public Phrase getBase() {
    return base;
  }

  public Phrase getWitness() {
    return witness;
  }

  @Override
  public String toXML() {
    Set<String> results = Sets.newLinkedHashSet();
    Collection<Phrase> values = phrases.values();
    for (Phrase p : values) {
      results.add(p.toString());
    }
    // detect the lemma
    if (results.size() == 1) {
      return results.iterator().next();
    }

    throw new RuntimeException("App is not yet implemented!");

    //    StringBuilder xml = new StringBuilder("<app>");
    //    if (base == null) {
    //      xml.append(base.toString());
    //    } else {
    //
    //      xml.append("<rdg wit=\"#").append(base.getWitness().id).append('"');
    //      if (base.toString().isEmpty())
    //        xml.append("/>");
    //      else
    //        xml.append(">").append(base.toString()).append("</rdg>");
    //
    //      xml.append("<rdg wit=\"#").append(witness.getWitness().id).append('"');
    //      if (witness.toString().isEmpty())
    //        xml.append("/>");
    //      else
    //        xml.append('>').append(witness.toString()).append("</rdg>");
    //
    //    }
    //    xml.append("</app>");
    //    return xml.toString();
  }

  public void addWord(Witness witness2, Word word) {
    Phrase existingPhrase = phrases.get(witness2.id);
    if (existingPhrase == null) {
      Phrase phrase = new Phrase(witness2, word, word);
      phrases.put(witness2.id, phrase);
      return;
    }

    Phrase newPhrase = new Phrase(witness2, existingPhrase.getFirstWord(), word);
    phrases.put(witness2.id, newPhrase);
  }
}
