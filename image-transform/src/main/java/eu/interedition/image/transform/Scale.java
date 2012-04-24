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
import javax.media.jai.BorderExtender;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.ParameterBlockJAI;
import java.awt.*;
import java.awt.image.RenderedImage;

/**
 * Scales an image.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author <a href="mailto:robcast@mail.berlios.de">Robert Casties</a>
 */
public class Scale extends BaseTransform {
    private final float factor;

    /**
     * Constructor.
     *
     * @param factor scaling factor
     */
    public Scale(float factor) {
        super();
        this.factor = factor;
    }

    /**
     * Constructor.
     *
     * @param quality
     * @param factor  scaling factor
     */
    public Scale(int quality, float factor) {
        super(quality);
        this.factor = factor;
    }

    public RenderedImage apply(@Nullable RenderedImage input) {
        // "SubsampleBinaryToGray" for downscaling BW
        if ((factor < 1) && (input.getColorModel().getPixelSize() == 1) && (quality > 0)) {
            return scaleBlackAndWhite(input, factor);
        }

        // blur and "Scale" for downscaling color images
        if ((factor <= 0.5) && (quality > 1)) {
            // don't blur more than 3
            final int blur = Math.min((int) Math.floor(1 / factor), 3);
            return scale(blur(input, blur), factor);
        }

        // "Scale" for the rest
        return scale(input, factor);
    }

    private RenderedImage scale(RenderedImage input, float scale) {
        ParameterBlockJAI param = new ParameterBlockJAI("Scale");
        param.addSource(input);
        param.setParameter("xScale", scale);
        param.setParameter("yScale", scale);
        param.setParameter("interpolation", interpolation);
        return JAI.create("Scale", param, new RenderingHints(JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(BorderExtender.BORDER_COPY)));
    }

    private RenderedImage scaleBlackAndWhite(RenderedImage input, float scale) {
        final ParameterBlockJAI parameters = new ParameterBlockJAI("SubsampleBinaryToGray");
        parameters.addSource(input);
        parameters.setParameter("xScale", scale);
        parameters.setParameter("yScale", scale);
        return JAI.create("SubsampleBinaryToGray", parameters, new RenderingHints(JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(BorderExtender.BORDER_COPY)));
    }

    private RenderedImage blur(RenderedImage input, int radius) {
        final int kernelLength = Math.max(radius, 2);
        final int kernelSize = kernelLength * kernelLength;
        final float[] kern = new float[kernelSize];
        final float f = 1f / kernelSize;
        for (int i = 0; i < kernelSize; i++) {
            kern[i] = f;
        }
        final KernelJAI blur = new KernelJAI(kernelLength, kernelLength, kern);
        final ParameterBlockJAI parameters = new ParameterBlockJAI("Convolve");
        parameters.addSource(input);
        parameters.setParameter("kernel", blur);
        return JAI.create("Convolve", parameters, new RenderingHints(JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(BorderExtender.BORDER_COPY)));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(factor, quality);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Scale) {
            final Scale other = (Scale) obj;
            return (factor == other.factor) && (quality == other.quality);
        }
        return super.equals(obj);
    }
}
