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

import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;

/**
 *  Apparatus element serializing to the output format specified in ticket #6. 
 *  
 */

public class SegmentColumn2 {

  private final Map<String, Phrase> _phrases;
  private final List<Segment> _witnesses;

  public SegmentColumn2(final List<Segment> witnesses) {
    this._phrases = Maps.newHashMap();
    this._witnesses = witnesses;
  }

  public String toXML() {
    // group together similar phrases
    final Multimap<String, String> renderedPhraseToWitnessID = Multimaps.newArrayListMultimap();
    for (final Entry<String, Phrase> entry : _phrases.entrySet()) {
      renderedPhraseToWitnessID.put(entry.getValue().toString(), entry.getKey());
    }
    // There is no app tag needed!
    if (renderedPhraseToWitnessID.keySet().size() == 1 && !hasEmptyCells()) {
      return renderedPhraseToWitnessID.keys().iterator().next();
    }

    // add the empty sigli to the multimap
    final Set<String> emptySigli = getEmptyCells();
    for (final String sigil : emptySigli) {
      renderedPhraseToWitnessID.put("", sigil);
    }

    final Map<String, String> renderSigli = renderSigli(renderedPhraseToWitnessID);
    return renderTheAppTag(renderSigli);
  }

  private Map<String, String> renderSigli(final Multimap<String, String> renderedPhraseToWitnessID) {
    // convert the multimap to a normal map  (by rendering the multiple sigli to a single string)
    final Map<String, String> sigliToRenderedPhrase = Maps.newLinkedHashMap();
    for (final String renderedPhrase : renderedPhraseToWitnessID.keySet()) {
      final Collection<String> sigli = renderedPhraseToWitnessID.get(renderedPhrase);
      final String renderedSigli = renderSigli(sigli);
      sigliToRenderedPhrase.put(renderedSigli, renderedPhrase);
    }
    return sigliToRenderedPhrase;
  }

  private String renderTheAppTag(final Map<String, String> sigliPhrase) {
    // do the actual rendering
    final StringBuilder xml = new StringBuilder("<app>");
    final List<String> keys = Lists.newArrayList(sigliPhrase.keySet());
    Collections.sort(keys);
    for (final String sigli : keys) {
      xml.append("<rdg wit=\"").append(sigli).append('"');
      final String renderedPhrase = sigliPhrase.get(sigli);
      if (renderedPhrase.isEmpty()) {
        xml.append("/>");
      } else
        xml.append('>').append(renderedPhrase).append("</rdg>");

    }
    xml.append("</app>");

    return xml.toString();

  }

  private Set<String> getEmptyCells() {
    final Set<String> sigliInTable = Sets.newLinkedHashSet();
    for (final Segment witness1 : _witnesses) {
      sigliInTable.add(witness1.id);
    }
    final Set<String> emptySigli = Sets.newLinkedHashSet(sigliInTable);
    emptySigli.removeAll(_phrases.keySet());
    return emptySigli;
  }

  private boolean hasEmptyCells() {
    return _witnesses.size() != _phrases.size();
  }

  private String renderSigli(final Collection<String> sigli) {
    final StringBuilder b = new StringBuilder();
    String delimiter = "";
    for (final String sigil : sigli) {
      b.append(delimiter);
      b.append("#");
      b.append(sigil);
      delimiter = " ";
    }
    return b.toString();
  }

  // NOTE: This is old code, should not be needed 
  public void addWord(final Segment witness2, final Word word) {
    final Phrase existingPhrase = _phrases.get(witness2.id);
    if (existingPhrase == null) {
      final Phrase phrase = new Phrase(witness2, word, word, null);
      _phrases.put(witness2.id, phrase);
      return;
    }

    final Phrase newPhrase = new Phrase(witness2, existingPhrase.getFirstWord(), word, null);
    _phrases.put(witness2.id, newPhrase);
  }

  public void addPhrase(final String witnessid, final Phrase phrase) {
    _phrases.put(witnessid, phrase);
  }
}
