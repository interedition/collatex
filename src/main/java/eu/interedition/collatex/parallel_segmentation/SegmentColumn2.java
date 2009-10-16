package eu.interedition.collatex.parallel_segmentation;

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

import eu.interedition.collatex.alignment.Phrase;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

/**
 *  Apparatus element serializing to the output format specified in ticket #6. 
 *  
 */

public class SegmentColumn2 {

  private final Map<String, Phrase> _phrases;
  private final List<Witness> _witnesses;

  public SegmentColumn2(List<Witness> witnesses) {
    this._phrases = Maps.newHashMap();
    this._witnesses = witnesses;
  }

  public String toXML() {
    // group together similar phrases
    Multimap<String, String> renderedPhraseToWitnessID = Multimaps.newArrayListMultimap();
    for (Entry<String, Phrase> entry : _phrases.entrySet()) {
      renderedPhraseToWitnessID.put(entry.getValue().toString(), entry.getKey());
    }
    // There is no app tag needed!
    if (renderedPhraseToWitnessID.keySet().size() == 1 && !hasEmptyCells()) {
      return renderedPhraseToWitnessID.keys().iterator().next();
    }

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

  }

  private Set<String> getEmptyCells() {
    Set<String> sigliInTable = Sets.newLinkedHashSet();
    for (Witness witness1 : _witnesses) {
      sigliInTable.add(witness1.id);
    }
    Set<String> emptySigli = Sets.newLinkedHashSet(sigliInTable);
    emptySigli.removeAll(_phrases.keySet());
    return emptySigli;
  }

  private boolean hasEmptyCells() {
    return _witnesses.size() != _phrases.size();
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
    Phrase existingPhrase = _phrases.get(witness2.id);
    if (existingPhrase == null) {
      Phrase phrase = new Phrase(witness2, word, word);
      _phrases.put(witness2.id, phrase);
      return;
    }

    Phrase newPhrase = new Phrase(witness2, existingPhrase.getFirstWord(), word);
    _phrases.put(witness2.id, newPhrase);
  }
}
