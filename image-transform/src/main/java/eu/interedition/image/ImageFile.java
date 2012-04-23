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
package eu.interedition.image;

import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.media.jai.JAI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.*;
import java.util.Iterator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author <a href="mailto:robcast@mail.berlios.de">Robert Casties</a>
 */
public class ImageFile {
    /**
     * File.
     */
    private final File file;

    /**
     * mime file type.
     */
    private String format;

    /**
     * image size in pixels.
     */
    private Rectangle imageSize;

    /**
     * Constructor with File.
     *
     * @param file
     */
    public ImageFile(File file) throws IllegalArgumentException, IOException {
        this.file = Preconditions.checkNotNull(file);
        ImageReader imageReader = null;
        try {
            imageReader = getImageReader();
            this.format = imageReader.getFormatName();
            this.imageSize = new Rectangle(imageReader.getWidth(0), imageReader.getHeight(0));
        } finally {
            dispose(imageReader);
        }
    }

    public File getFile() {
        return file;
    }

    public Rectangle getSize() {
        return imageSize;
    }

    public String getFormat() {
        return format;
    }

    public BufferedImage read() throws IOException {
        return read(imageSize, 1);
    }

    /* Load an image file into the Object. */
    public BufferedImage read(Rectangle region, int subSampling) throws IOException {
        /* FIXME: JAI imageread seems to ignore the readParam :-(
        ImageInputStream istream = (ImageInputStream) reader.getInput();
        ParameterBlockJAI pb = new ParameterBlockJAI("imageread");
        pb.setParameter("Input", istream);
        pb.setParameter("ReadParam", readParam);
        pb.setParameter("Reader", reader);
        img = JAI.create("imageread", pb);
        */

        ImageReader imageReader = null;
        try {
            imageReader = getImageReader();

            final ImageReadParam parameters = imageReader.getDefaultReadParam();
            parameters.setSourceRegion(region);
            parameters.setSourceSubsampling(subSampling, subSampling, 0, 0);

            return imageReader.read(0, parameters);
        } finally {
            dispose(imageReader);
        }
    }

    public void write(RenderedImage image) throws IOException {
        OutputStream stream = null;
        try {
            write(image, format, stream = new BufferedOutputStream(new FileOutputStream(file)));
        } finally {
            Closeables.close(stream, false);
        }
    }

    public static void write(RenderedImage image, String format, OutputStream stream) {
        final ParameterBlock parameters = new ParameterBlock();
        parameters.addSource(image);
        parameters.add(stream);
        parameters.add(format);
        JAI.create("ImageWrite", parameters);
    }

    public ImageReader getImageReader() throws IOException {
        final FileImageInputStream imageInputStream = new FileImageInputStream(file);

        final Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
        Preconditions.checkArgument(readers.hasNext(), this + " cannot be read via Java ImageIO");

        final ImageReader imageReader = readers.next();
        imageReader.setInput(imageInputStream);
        return imageReader;
    }

    public static void dispose(ImageReader imageReader) {
        if (imageReader != null) {
            imageReader.dispose();
        }
    }

    @Override
    public String toString() {
        return file.toString();
    }
}