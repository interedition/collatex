package eu.interedition.image.transform;

import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.awt.image.RenderedImage;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author <a href="mailto:robcast@mail.berlios.de">Robert Casties</a>
 */
public class TransformList extends LinkedList<Function<RenderedImage, RenderedImage>> implements Function<RenderedImage, RenderedImage> {

    public TransformList() {
        super();
    }

    public TransformList(Collection<? extends Function<RenderedImage, RenderedImage>> c) {
        super(c);
    }

    public RenderedImage apply(@Nullable RenderedImage input) {
        for (Function<RenderedImage, RenderedImage> transform : this) {
            input = transform.apply(input);
        }
        return input;
    }
}
