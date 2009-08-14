package eu.interedition.collatex.output;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
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
  private final AppAlignmentTable appAlignmentTable;

  public AppElementTEI(AppAlignmentTable _appAlignmentTable1, Phrase _base, Phrase _witness) {
    this.appAlignmentTable = _appAlignmentTable1;
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
    // group together similar phrases
    Multimap<String, String> renderedPhraseToWitnessID = Multimaps.newArrayListMultimap();
    for (Entry<String, Phrase> entry : phrases.entrySet()) {
      renderedPhraseToWitnessID.put(entry.getValue().toString(), entry.getKey());
    }
    // There is no app tag needed!
    if (renderedPhraseToWitnessID.keySet().size() == 1 && !hasEmptyCells()) {
      return renderedPhraseToWitnessID.keys().iterator().next();
    }
    //Set<String> results = Sets.newLinkedHashSet();
    //    Collection<Phrase> values = phrases.values();
    //    for (Phrase p : values) {
    //      results.add(p.toString());
    //    }
    // detect the lemma
    //    if (results.size() == 1) {
    //      return results.iterator().next();
    //    }

    // this was just for debug purposes
    //    for (String renderedPhrase : renderedPhraseToWitnessID.keySet()) {
    //      Collection<String> sigli = renderedPhraseToWitnessID.get(renderedPhrase);
    //      System.out.println(sigli.toString() + ":" + renderedPhrase);
    //    }

    // add the empty sigli to the multimap
    Set<String> emptySigli = getEmptyCells();
    for (String sigil : emptySigli) {
      renderedPhraseToWitnessID.put("", sigil);
    }

    Map<String, String> renderSigli = renderSigli(renderedPhraseToWitnessID);
    return renderTheAppTag(renderSigli);
  }

  private Map<String, String> renderSigli(Multimap<String, String> renderedPhraseToWitnessID) {
    // convert the multimap to a normal map  (by rendering the multiple sigli to a single string)
    Map<String, String> sigliToRenderedPhrase = Maps.newLinkedHashMap();
    for (String renderedPhrase : renderedPhraseToWitnessID.keySet()) {
      Collection<String> sigli = renderedPhraseToWitnessID.get(renderedPhrase);
      String renderedSigli = renderSigli(sigli);
      sigliToRenderedPhrase.put(renderedSigli, renderedPhrase);
    }
    return sigliToRenderedPhrase;
  }

  private String renderTheAppTag(Map<String, String> sigliPhrase) {
    // do the actual rendering
    StringBuilder xml = new StringBuilder("<app>");
    List<String> keys = Lists.newArrayList(sigliPhrase.keySet());
    Collections.sort(keys);
    for (String sigli : keys) {
      xml.append("<rdg wit=\"").append(sigli).append('"');
      String renderedPhrase = sigliPhrase.get(sigli);
      if (renderedPhrase.isEmpty()) {
        xml.append("/>");
      } else
        xml.append('>').append(renderedPhrase).append("</rdg>");

    }
    xml.append("</app>");

    return xml.toString();

    //    if (base == null) {
    //      xml.append(base.toString());
    //    } else {
    //

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

  private Set<String> getEmptyCells() {
    List<Witness> witnesses = appAlignmentTable.getWitnesses();
    Set<String> sigliInTable = Sets.newLinkedHashSet();
    for (Witness witness : witnesses) {
      sigliInTable.add(witness.id);
    }
    Set<String> emptySigli = Sets.newLinkedHashSet(sigliInTable);
    emptySigli.removeAll(phrases.keySet());
    return emptySigli;
  }

  private boolean hasEmptyCells() {
    return appAlignmentTable.getWitnesses().size() != phrases.size();
  }

  private String renderSigli(Collection<String> sigli) {
    StringBuilder b = new StringBuilder();
    String delimiter = "";
    for (String sigil : sigli) {
      b.append(delimiter);
      b.append("#");
      b.append(sigil);
      delimiter = " ";
    }
    return b.toString();
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
