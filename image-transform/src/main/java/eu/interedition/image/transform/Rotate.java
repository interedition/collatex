/*
 * #%L
 * Image Transformation: Utilities for transforming digital images.
 * %%
 * Copyright (C) 2010 - 2012 The Interedition Development Group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
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
