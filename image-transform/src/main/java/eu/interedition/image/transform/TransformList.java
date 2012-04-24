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

import javax.annotation.Nullable;
import java.awt.image.RenderedImage;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author <a href="mailto:robcast@mail.berlios.de">Robert Casties</a>
 */
public class TransformList extends LinkedList<Function<RenderedImage, RenderedImage>> implements Function<RenderedImage, RenderedImage> {

    public TransformList() {
        super();
    }

    public TransformList(Collection<? extends Function<RenderedImage, RenderedImage>> c) {
        super(c);
    }

    public RenderedImage apply(@Nullable RenderedImage input) {
        for (Function<RenderedImage, RenderedImage> transform : this) {
            input = transform.apply(input);
        }
        return input;
    }
}
