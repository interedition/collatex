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
