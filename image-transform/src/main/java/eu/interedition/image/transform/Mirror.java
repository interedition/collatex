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

import javax.annotation.Nullable;
import javax.media.jai.JAI;
import javax.media.jai.operator.TransposeDescriptor;
import javax.media.jai.operator.TransposeType;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

/**
 * Mirrors an image.
 * <p/>
 * The mirror axis goes through the center of the image and is rotated by <code>angle</code>
 * degrees. Currently only horizontal and vertical mirroring (0 and 90 degrees) are supported.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author <a href="mailto:robcast@mail.berlios.de">Robert Casties</a>
 */
public class Mirror extends BaseTransform {

    private final TransposeType transposeType;

    /**
     * Constructor.
     *
     * @param angle angle of mirror axis
     */
    public Mirror(float angle) {
        super();
        // only mirroring by right angles
        if (Math.abs(angle) < EPSILON || Math.abs(angle - 180) < EPSILON || Math.abs(angle - 360) < EPSILON) {
            transposeType = TransposeDescriptor.FLIP_HORIZONTAL;
        } else if (Math.abs(angle - 90) < EPSILON || Math.abs(angle - 270) < EPSILON) {
            transposeType = TransposeDescriptor.FLIP_VERTICAL;
        } else {
            throw new IllegalArgumentException(Float.toString(angle));
        }
    }

    public RenderedImage apply(@Nullable RenderedImage input) {
        final ParameterBlock parameters = new ParameterBlock();
        parameters.addSource(input);
        parameters.add(transposeType);
        return JAI.create("transpose", parameters);
    }

    @Override
    public int hashCode() {
        return transposeType.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Mirror) {
            return transposeType.equals(((Mirror) obj).transposeType);
        }
        return super.equals(obj);
    }
}
