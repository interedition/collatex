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
