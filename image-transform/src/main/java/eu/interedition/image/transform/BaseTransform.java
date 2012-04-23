package eu.interedition.image.transform;

import com.google.common.base.Function;

import javax.media.jai.Interpolation;
import java.awt.image.RenderedImage;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author <a href="mailto:robcast@mail.berlios.de">Robert Casties</a>
 */
public abstract class BaseTransform implements Function<RenderedImage, RenderedImage> {
    /**
     * Epsilon for float comparisons.
     */
    protected static final double EPSILON = 1e-5;

    protected final int quality;
    protected final Interpolation interpolation;

    protected BaseTransform() {
        this(1);
    }

    protected BaseTransform(int quality) {
        this.quality = quality;

        if (this.quality > 1) {
            interpolation = Interpolation.getInstance(Interpolation.INTERP_BICUBIC);
        } else if (this.quality == 1) {
            interpolation = Interpolation.getInstance(Interpolation.INTERP_BILINEAR);
        } else {
            interpolation = Interpolation.getInstance(Interpolation.INTERP_NEAREST);
        }
    }

    /**
     * Calculate "contrast" values.
     */
    protected static double[] toContrast(double[] rgbm) {
        double[] contrastMult = new double[3];
        for (int i = 0; i < 3; i++) {
            contrastMult[i] = toContrast(rgbm[i]);
        }
        return contrastMult;
    }

    /**
     * Calculate "contrast" value (c=2^x).
     */
    protected static double toContrast(double d) {
        return Math.pow(2.0d, d);
    }


}
