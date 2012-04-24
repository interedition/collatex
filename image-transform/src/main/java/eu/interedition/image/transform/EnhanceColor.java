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
