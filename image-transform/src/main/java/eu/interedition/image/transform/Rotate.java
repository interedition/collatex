package eu.interedition.image.transform;

import com.google.common.base.Objects;

import javax.annotation.Nullable;
import javax.media.jai.JAI;
import javax.media.jai.operator.TransposeDescriptor;
import javax.media.jai.operator.TransposeType;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

/**
 * Rotates an image.
 * <p/>
 * The image is rotated around the center by the <code>angle</code> given in degrees [0, 360]
 * clockwise. Image size and aspect ratio are likely to change.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author <a href="mailto:robcast@mail.berlios.de">Robert Casties</a>
 */
public class Rotate extends BaseTransform {

    private final double angle;

    /**
     * Constructor.
     *
     * @param angle rotation angle in degrees
     */
    public Rotate(double angle) {
        super();
        this.angle = angle;
    }

    /**
     * Constructor.
     *
     * @param quality
     * @param angle   rotation angle in degrees
     */
    public Rotate(int quality, double angle) {
        super(quality);
        this.angle = angle;
    }

    public RenderedImage apply(@Nullable RenderedImage input) {
        // optimized rotation by right angles
        TransposeType transposeType = null;
        if (Math.abs(angle - 0) < EPSILON || Math.abs(angle - 360) < EPSILON) {
            return input;
        } else if (Math.abs(angle - 90) < EPSILON) {
            transposeType = TransposeDescriptor.ROTATE_90;
        } else if (Math.abs(angle - 180) < EPSILON) {
            transposeType = TransposeDescriptor.ROTATE_180;
        } else if (Math.abs(angle - 270) < EPSILON) {
            transposeType = TransposeDescriptor.ROTATE_270;
        }
        if (transposeType != null) {
            final ParameterBlock transposeParameters = new ParameterBlock();
            transposeParameters.addSource(input);
            transposeParameters.add(transposeType);
            return JAI.create("transpose", transposeParameters);
        }

        // normal rotation
        final ParameterBlock rotateParameters = new ParameterBlock();
        rotateParameters.addSource(input);
        rotateParameters.add(input.getWidth() / 2.0f);
        rotateParameters.add(input.getHeight() / 2.0f);
        rotateParameters.add((float) Math.toRadians(angle));
        rotateParameters.add(interpolation);
        return JAI.create("rotate", rotateParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(angle, quality);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Rotate) {
            final Rotate other = (Rotate) obj;
            return (angle == other.angle) && (quality == other.quality);
        }
        return super.equals(obj);
    }
}
