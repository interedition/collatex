package eu.interedition.image.transform;

import com.google.common.base.Objects;

import javax.annotation.Nullable;
import javax.media.jai.JAI;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

/**
 * Enhances brightness and contrast of an image.
 * <p/>
 * Contrast is enhanced by multiplying the pixel value with the constant
 * <code>mult</code>. Brightness is enhanced by adding the constant
 * <code>add</code> to the pixel value.
 * <p/>
 * Operation: p1 = (p0 * mult) + add.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author <a href="mailto:robcast@mail.berlios.de">Robert Casties</a>
 */
public class Enhance extends BaseTransform {

    private final double mult;
    private final double add;

    /**
     * Constructor.
     *
     * @param mult multiplicative constant for contrast enhancement
     * @param add  additive constant for brightness enhancement
     */
    public Enhance(double mult, double add) {
        super();
        this.mult = toContrast(mult);
        this.add = add;
    }

    public RenderedImage apply(@Nullable RenderedImage input) {
        final ParameterBlock parameters = new ParameterBlock();
        parameters.addSource(input);
        parameters.add(new double[]{mult});
        parameters.add(new double[]{add});
        return JAI.create("rescale", parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mult, add);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Enhance) {
            final Enhance other = (Enhance) obj;
            return (mult == other.mult) && (add == other.add);
        }
        return super.equals(obj);
    }
}
