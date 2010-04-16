package eu.interedition.collatex2.output;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Apparatus element serializing to the output format specified in ticket #6.
 * 
 */

public class TeiParallelSegmentationExporter {

  private final ParallelSegmentationApparatus apparatus;

  public TeiParallelSegmentationExporter(ParallelSegmentationApparatus apparatus) {
    this.apparatus = apparatus;
  }

  public String toTeiXML() {
    final StringBuilder result = new StringBuilder(); // FIXME initialize length
    result.append("<collation>");
    result.append("<seg>");
    String delimiter = "";
    for (final ApparatusEntry entry : apparatus.getEntries()) {
      result.append(delimiter); // FIXME can we just introduce whitespace
      // here!?
      result.append(convertEntry(entry));
      delimiter = " ";
    }
    result.append("</seg>");
    result.append("</collation>");
    return result.toString();
  }

  private String convertEntry(ApparatusEntry entry) {
    // group together similar phrases
    final Multimap<String, String> contentToSigel = Multimaps.newArrayListMultimap();
    List<String> sigli = entry.getSigli();
    for (String sigel : sigli) {
      contentToSigel.put(entry.getPhrase(sigel).getContent(), sigel);
    }
    // There is no apparatus tag needed!
    if (contentToSigel.keySet().size() == 1 && !entry.hasEmptyCells()) {
      return contentToSigel.keys().iterator().next();
    }
    return renderAppTag(renderSigli(contentToSigel));
  }

  private String renderAppTag(Map<String, String> sigliPhrase) {
    // do the actual rendering
    final StringBuilder xml = new StringBuilder("<app>");
    final List<String> keys = Lists.newArrayList(sigliPhrase.keySet());
    Collections.sort(keys);
    for (final String sigli : keys) {
      xml.append("<rdg wit=\"").append(sigli).append('"');
      final String renderedPhrase = sigliPhrase.get(sigli);
      if (renderedPhrase.isEmpty()) {
        xml.append("/>");
      } else {
        xml.append('>').append(renderedPhrase).append("</rdg>");
      }
    }
    xml.append("</app>");
    return xml.toString();
  }

  private Map<String, String> renderSigli(Multimap<String, String> renderedContentToSigel) {
    // convert the multimap to a normal map (by rendering the multiple sigli to
    // a single string)
    final Map<String, String> sigliToRenderedPhrase = Maps.newLinkedHashMap();
    for (final String renderedPhrase : renderedContentToSigel.keySet()) {
      final Collection<String> sigli = renderedContentToSigel.get(renderedPhrase);
      final String renderedSigli = renderSigli(sigli);
      sigliToRenderedPhrase.put(renderedSigli, renderedPhrase);
    }
    return sigliToRenderedPhrase;
  }

  private String renderSigli(Collection<String> sigli) {
    final List<String> sortedSigli = Lists.newArrayList(sigli);
    Collections.sort(sortedSigli);
    final StringBuilder b = new StringBuilder();
    String delimiter = "";
    for (final String sigel : sortedSigli) {
      b.append(delimiter);
      b.append("#");
      b.append(sigel);
      delimiter = " ";
    }
    return b.toString();
  }
}
