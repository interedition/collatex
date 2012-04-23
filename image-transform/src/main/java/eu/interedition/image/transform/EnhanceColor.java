package eu.interedition.image.transform;


import com.google.common.base.Objects;

import javax.annotation.Nullable;
import javax.media.jai.JAI;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.Arrays;

/**
 * Manipulates the colors of an image.
 * <p/>
 * For the red, green and blue color channels all pixel values are multiplied by the
 * constant <code>m</code> and added to the constant <code>a</code>.
 * <p/>
 * Operation: p1 = (p0 * m) + a.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author <a href="mailto:robcast@mail.berlios.de">Robert Casties</a>
 */
public class EnhanceColor extends BaseTransform {

    private final double[] rgbm;
    private final double[] rgba;

    /**
     * Constructor.
     *
     * @param rgbm multiplicative constants for red, green, blue
     * @param rgba additive constant for red, green, blue
     */
    public EnhanceColor(double[] rgbm, double[] rgba) {
        rgbm = toContrast(Objects.firstNonNull(rgbm, new double[3]));
        rgba = Objects.firstNonNull(rgba, new double[3]);

        if (rgbm.length != rgba.length) {
            throw new IllegalArgumentException(Arrays.toString(rgbm) + " vs. " + Arrays.toString(rgba));
        }
        this.rgbm = rgbm;
        this.rgba = rgba;
    }

    public RenderedImage apply(@Nullable RenderedImage input) {
        final ParameterBlock parameters = new ParameterBlock();
        parameters.addSource(input);
        parameters.add(rgbm);
        parameters.add(rgba);
        return JAI.create("rescale", parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Arrays.hashCode(rgbm), Arrays.hashCode(rgba));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof EnhanceColor) {
            final EnhanceColor other = (EnhanceColor) obj;
            if (rgbm.length != other.rgbm.length) {
                return false;
            }
            for (int i = 0; i < rgbm.length; i++) {
                if (rgbm[i] != other.rgbm[i]) {
                    return false;
                }
            }
            for (int i = 0; i < rgba.length; i++) {
                if (rgba[i] != other.rgba[i]) {
                    return false;
                }
            }
            return true;
        }
        return super.equals(obj);
    }
}
