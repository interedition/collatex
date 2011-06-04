package eu.interedition.collatex.cocoon;

import com.google.common.collect.Lists;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IRow;
import eu.interedition.collatex2.interfaces.IWitness;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CollateXTransformer extends AbstractSAXTransformer {

    private CollateXEngine engine = new CollateXEngine();
    private List<IWitness> witnesses = Lists.newArrayList();
    private String sigil;

    public CollateXTransformer() {
        super();
        this.defaultNamespaceURI = "http://interedition.eu/collatex/ns/1.0";
    }

    public void startTransformingElement(String uri, String name, String raw, Attributes attr) throws ProcessingException,
            IOException, SAXException {
        if ("collation".equals(name)) {
            witnesses.clear();
            sigil = null;
        } else if ("witness".equals(name)) {
            sigil = attr.getValue("sigil");
            if (sigil == null) {
                sigil = "w" + (witnesses.size() + 1);
            }
            startTextRecording();
        }
    }

    @Override
    public void endTransformingElement(String uri, String name, String raw) throws ProcessingException, IOException, SAXException {
        if ("collation".equals(name)) {
            ignoreHooksCount++;
            sendStartPrefixMapping();
            sendStartElementEventNS("alignment", EMPTY_ATTRIBUTES);
            if (!witnesses.isEmpty()) {
                final IAlignmentTable alignmentTable = engine.align(witnesses.toArray(new IWitness[witnesses.size()]));
                for (IRow row : alignmentTable.getRows()) {
                    final AttributesImpl rowAttrs = new AttributesImpl();
                    rowAttrs.addAttribute(namespaceURI, "sigil", "sigil", "CDATA", row.getSigil());
                    sendStartElementEventNS("row", rowAttrs);
                    for (ICell cell: row) {
                        sendStartElementEventNS("cell", EMPTY_ATTRIBUTES);
                        if (!cell.isEmpty()) {
                            sendTextEvent(cell.getToken().getContent());
                        }
                        sendEndElementEventNS("cell");

                    }
                    sendEndElementEventNS("row");
                }
            }
            sendEndElementEventNS("alignment");
            sendEndPrefixMapping();
            ignoreHooksCount--;
        } else if ("witness".equals(name)) {
            witnesses.add(engine.createWitness(sigil, endTextRecording()));
        }
    }
}