package eu.interedition.image.transform;

import com.google.common.base.Objects;

import javax.annotation.Nullable;
import javax.media.jai.JAI;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

/**
 * Crops an image.
 * <p/>
 * Cuts out a region of the size <code>width</code> x <code>height</code> at
 * the offset <code>x</code>, <code>y</code>.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author <a href="mailto:robcast@mail.berlios.de">Robert Casties</a>
 */
public class Crop extends BaseTransform {
    private final float x;
    private final float y;
    private final float width;
    private final float height;

    /**
     * Constructor.
     *
     * @param x      X offset of crop region
     * @param y      Y offset of crop region
     * @param width  width of crop region
     * @param height height of crop region
     */
    public Crop(float x, float y, float width, float height) {
        super();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public RenderedImage apply(@Nullable RenderedImage input) {
        final ParameterBlock parameters = new ParameterBlock();
        parameters.addSource(input);
        parameters.add(x);
        parameters.add(y);
        parameters.add(width);
        parameters.add(height);
        return JAI.create("crop", parameters);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Crop) {
            final Crop other = (Crop) obj;
            return (x == other.x) && (y == other.y) && (width == other.width) && (height == other.height);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(x, y, width, height);
    }
}
