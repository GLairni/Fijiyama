package io.github.rocsg.fijiyama.common;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.*;
import org.itk.simple.Image;
import org.itk.simple.PixelIDValueEnum;
import org.itk.simple.SimpleITK;
import org.itk.simple.VectorUInt32;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.*;

import static java.lang.Math.max;
import static org.itk.simple.PixelIDValueEnum.*;

public class ConverteritkPlus {

    public static boolean verbose = false;


    public static void run() {
        String str = "C:\\\\Users\\\\loaiu\\\\Documents\\\\Etudes\\\\MAM\\\\MAM5\\\\Stage\\\\data\\\\Nouveau_dossier\\\\Test\\\\32Bits\\\\22_registered_stack_32Bits.tif";
        ImagePlus imgRef = IJ.openImage(str);
        imgRef.changes = false;
        ImagePlus transformed = convertITK2ImagePlusDirect(convertImagePlus2ImageITK(imgRef));
        transformed.setTitle("Transformed Image");
        transformed.setDisplayRange(imgRef.getDisplayRangeMin(), imgRef.getDisplayRangeMax());
        ImagePlus transformed2 = convertITK2ImagePlusDirectST(convertImagePlus2ImageITK(imgRef));
        transformed2.setTitle("Transformed Image 2");
        transformed2.setDisplayRange(imgRef.getDisplayRangeMin(), imgRef.getDisplayRangeMax());
        ImagePlus transformed3 = convertITK2ImagePlusDirect(convertImagePlus2ImageITKST(imgRef));
        transformed3.setTitle("Transformed Image 3");
        transformed3.setDisplayRange(imgRef.getDisplayRangeMin(), imgRef.getDisplayRangeMax());
        ImagePlus transformed4 = convertITK2ImagePlusDirectST(convertImagePlus2ImageITKST(imgRef));
        transformed4.setDisplayRange(imgRef.getDisplayRangeMin(), imgRef.getDisplayRangeMax());
        transformed4.setTitle("Transformed Image 4");
        imgRef.show();
        transformed.show();
        transformed2.show();
        transformed3.show();
        transformed4.show();
        // wait for the user to close the windows
        while (true) {
            if (!imgRef.isVisible() && !transformed.isVisible()) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        run();
        System.exit(0);
        Path directoryPath = Paths.get("C:\\\\Users\\\\loaiu\\\\Documents\\\\Etudes\\\\MAM\\\\MAM5\\\\Stage\\\\data\\\\Nouveau_dossier\\\\Test\\\\32Bits\\\\");
        try {
            Files.list(directoryPath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> processImage(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.exit(0);
    }

    private static void processImage(Path imagePath) {
        System.out.println("Processing image: " + imagePath);

        Image imageITK = SimpleITK.readImage(imagePath.toString());

        Buffer itkBuffer = imageITK.getBufferAsBuffer();

        ImagePlus originalImage = new ImagePlus(imagePath.toString());
        originalImage.setTitle("Original Image " + originalImage.getTitle());
        originalImage.show();

        ImagePlus imagePlusMT = convertITK2ImagePlusDirectST(imageITK);
        System.out.println("Buffer equals: " + Objects.equals(imageITK.getBufferAsBuffer(), itkBuffer));
        System.out.println("Just Checking conversion 2");
        imagePlusMT.setTitle("ImagePlusMT " + imagePlusMT.getTitle());
        imagePlusMT.show();

        ImagePlus imagePlus = convertITK2ImagePlusDirect(imageITK);
        System.out.println("Buffer equals: " + Objects.equals(imageITK.getBufferAsBuffer(), itkBuffer));
        System.out.println("Just Checking conversion");
        assert imagePlus != null;
        imagePlus.setTitle("ImagePlus " + imagePlus.getTitle());
        //imagePlus.show();
        // read image with ImageJ and display it
        ImagePlus imagePlus2 = new ImagePlus(imagePath.toString());

        Image img1 = convertImagePlus2ImageITK(imagePlus2);
        System.out.println("Buffer equals: " + Objects.equals(img1.getBufferAsBuffer(), itkBuffer));
        // show differences between the two buffers
        System.out.println("Buffer equals: " + Objects.equals(img1.getBufferAsBuffer(), itkBuffer));
        if (!Objects.equals(img1.getBufferAsBuffer(), itkBuffer)) {
            System.out.println("Buffers are not equal");

            // print first 10 values
            itkBuffer.rewind();
            itkBuffer.limit(10);
            System.out.println("ITK Buffer: " + itkBuffer);
            Buffer test = img1.getBufferAsBuffer();
            test.rewind();
            test.limit(10);
            System.out.println("ImageJ Buffer: " + test);
        }

        Image img2 = convertImagePlus2ImageITKST(imagePlus2);
        System.out.println("Buffer equals: " + Objects.equals(img2.getBufferAsBuffer(), itkBuffer));

        // display the image
        ImagePlus imagePlus3 = convertITK2ImagePlus(img1);
        // rename
        imagePlus3.setTitle("ImagePlus3 " + imagePlus3.getTitle());
        //imagePlus3.show();

        ImagePlus imagePlus4 = convertITK2ImagePlusDirectST(img2);
        // rename
        imagePlus4.setTitle("ImagePlus4 " + imagePlus4.getTitle());
        imagePlus4.show();
    }

    // ImagePlus to ITK Image

    /****************************************************************************/

    // Buffer Kind
    public static PixelIDValueEnum convertBufferImage2ITKEnum(ImagePlus imagePlus) {
        // if the image is a 8bit uint return sitkUInt8
        if (imagePlus.getProcessor() instanceof ByteProcessor) {
            if (imagePlus.isRGB()) {
                if (imagePlus.getBitDepth() == 8) {
                    return sitkVectorUInt8;
                }
            } else {
                if (imagePlus.getBitDepth() == 8) {
                    return sitkUInt8;
                }
            }
        } else if (imagePlus.getProcessor() instanceof ShortProcessor) {
            if (imagePlus.isRGB()) {
                if (imagePlus.getBitDepth() == 16) {
                    return sitkVectorUInt16;
                } else if (imagePlus.getBitDepth() == 32) {
                    return sitkVectorUInt32;
                } else if (imagePlus.getBitDepth() == 64) {
                    return sitkVectorUInt64;
                }
            } else {
                if (imagePlus.getBitDepth() == 16) {
                    return sitkUInt16;
                } else if (imagePlus.getBitDepth() == 32) {
                    return sitkUInt32;
                } else if (imagePlus.getBitDepth() == 64) {
                    return sitkUInt64;
                }
            }
        }
        // if it is an int
        else if (imagePlus.getProcessor() instanceof IntProcessor) {
            if (imagePlus.isRGB()) {
                if (imagePlus.getBitDepth() == 8) {
                    return sitkVectorInt8;
                } else if (imagePlus.getBitDepth() == 16) {
                    return sitkVectorInt16;
                } else if (imagePlus.getBitDepth() == 32) {
                    return sitkVectorInt32;
                } else if (imagePlus.getBitDepth() == 64) {
                    return sitkVectorInt64;
                }
            } else {
                // if 8 bit, return sitkInt8
                if (imagePlus.getBitDepth() == 8) {
                    return sitkInt8;
                } else if (imagePlus.getBitDepth() == 16) {
                    return sitkInt16;
                } else if (imagePlus.getBitDepth() == 32) {
                    return sitkInt32;
                } else if (imagePlus.getBitDepth() == 64) {
                    return sitkInt64;
                }
            }
        } else if (imagePlus.getProcessor() instanceof FloatProcessor) {
            if (imagePlus.isRGB()) {
                if (imagePlus.getBitDepth() == 32) {
                    return sitkVectorFloat32;
                } else if (imagePlus.getBitDepth() == 64) {
                    return sitkVectorFloat64;
                }
            } else {
                // if 8 bit, return sitkFloat32
                if (imagePlus.getBitDepth() == 32) {
                    return sitkFloat32;
                } else if (imagePlus.getBitDepth() == 64) {
                    return sitkFloat64;
                }
            }
        } else if (imagePlus.getProcessor() instanceof ColorProcessor) {
            if (imagePlus.getBitDepth() == 24) {
                return sitkVectorUInt8;
            } else if (imagePlus.getBitDepth() == 48) {
                return sitkVectorUInt16;
            } else {
                throw new IllegalArgumentException("Unsupported pixel type: " + imagePlus.getProcessor().getClass().getSimpleName());
            }
        } else {
            throw new IllegalArgumentException("Unsupported pixel type: " + imagePlus.getProcessor().getClass().getSimpleName());
        }
        return null;
    }

    public static Image convertImagePlus2ImageITK(ImagePlus imagePlus) {

        long[] coordinates = new long[3];
        coordinates[0] = imagePlus.getWidth();
        coordinates[1] = imagePlus.getHeight();
        coordinates[2] = imagePlus.getStackSize();
        VectorUInt32 size = new VectorUInt32(coordinates);
        PixelIDValueEnum pixelType = convertBufferImage2ITKEnum(imagePlus);
        Image imageITK = new Image(size, Objects.requireNonNull(pixelType), (imagePlus.isRGB() ? 3 : 1));

        Buffer buffer = imageITK.getBufferAsBuffer();
        buffer.rewind();

        ImageStack stack = imagePlus.getImageStack();

        if (buffer instanceof ByteBuffer) {
            if (imagePlus.isRGB()) {

                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ImageProcessor ip = stack.getProcessor(i);
                    Object data = ip.getPixels();
                    if (data instanceof int[]) {
                        int[] pixels = (int[]) data;
                        byte[] rgb = new byte[3];
                        for (int pixel : pixels) {
                            rgb[0] = (byte) ((pixel >> 16) & 0xFF);
                            rgb[1] = (byte) ((pixel >> 8) & 0xFF);
                            rgb[2] = (byte) (pixel & 0xFF);
                            ((ByteBuffer) buffer).put(rgb);
                        }
                    }
                }
            } else {
                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ImageProcessor ip = stack.getProcessor(i);
                    Object data = ip.getPixels();
                    if (data instanceof byte[]) {
                        byte[] pixels = (byte[]) data;
                        ((ByteBuffer) buffer).put(pixels);
                    }
                }
            }
        } else if (buffer instanceof ShortBuffer) {
            if (imagePlus.isRGB()) {
                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ImageProcessor ip = stack.getProcessor(i);
                    Object data = ip.getPixels();
                    if (data instanceof int[]) {
                        int[] pixels = (int[]) data;
                        short[] rgb = new short[3];
                        for (int pixel : pixels) {
                            rgb[0] = (short) ((pixel >> 16) & 0xFF);
                            rgb[1] = (short) ((pixel >> 8) & 0xFF);
                            rgb[2] = (short) (pixel & 0xFF);
                            ((ShortBuffer) buffer).put(rgb);
                        }
                    }
                }
            } else {
                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ImageProcessor ip = stack.getProcessor(i);
                    Object data = ip.getPixels();
                    if (data instanceof short[]) {
                        short[] pixels = (short[]) data;
                        ((ShortBuffer) buffer).put(pixels);
                    }
                }
            }
        } else if (buffer instanceof CharBuffer) {
            if (imagePlus.isRGB()) {

                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ImageProcessor ip = stack.getProcessor(i);
                    Object data = ip.getPixels();
                    if (data instanceof int[]) {
                        int[] pixels = (int[]) data;
                        char[] rgb = new char[3];
                        for (int pixel : pixels) {
                            rgb[0] = (char) ((pixel >> 16) & 0xFF);
                            rgb[1] = (char) ((pixel >> 8) & 0xFF);
                            rgb[2] = (char) (pixel & 0xFF);
                            ((CharBuffer) buffer).put(rgb);
                        }
                    }
                }
            } else {

                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ImageProcessor ip = stack.getProcessor(i);
                    Object data = ip.getPixels();
                    if (data instanceof short[]) {
                        short[] pixels = (short[]) data;
                        char[] pixelsChar = new char[pixels.length];
                        for (int j = 0; j < pixels.length; j++) {
                            pixelsChar[j] = (char) pixels[j];
                        }
                        ((CharBuffer) buffer).put(pixelsChar);
                    }
                }
            }
        } else if (buffer instanceof IntBuffer) {
            if (imagePlus.isRGB()) {

                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ImageProcessor ip = stack.getProcessor(i);
                    Object data = ip.getPixels();
                    if (data instanceof int[]) {
                        int[] pixels = (int[]) data;
                        int[] rgb = new int[3];
                        for (int pixel : pixels) {
                            rgb[0] = (pixel >> 16) & 0xFF;
                            rgb[1] = (pixel >> 8) & 0xFF;
                            rgb[2] = pixel & 0xFF;
                            ((IntBuffer) buffer).put(rgb);
                        }
                    }
                }
            } else {

                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ImageProcessor ip = stack.getProcessor(i);
                    Object data = ip.getPixels();
                    if (data instanceof int[]) {
                        int[] pixels = (int[]) data;
                        ((IntBuffer) buffer).put(pixels);
                    }
                }
            }
        } else if (buffer instanceof FloatBuffer) {
            if (imagePlus.isRGB()) {

                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ImageProcessor ip = stack.getProcessor(i);
                    Object data = ip.getPixels();
                    if (data instanceof int[]) {
                        int[] pixels = (int[]) data;
                        float[] rgb = new float[3];
                        for (int pixel : pixels) {
                            rgb[0] = (pixel >> 16) & 0xFF;
                            rgb[1] = (pixel >> 8) & 0xFF;
                            rgb[2] = pixel & 0xFF;
                            ((FloatBuffer) buffer).put(rgb);
                        }
                    }
                }
            } else {

                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ImageProcessor ip = stack.getProcessor(i);
                    Object data = ip.getPixels();
                    if (data instanceof float[]) {
                        float[] pixels = (float[]) data;
                        ((FloatBuffer) buffer).put(pixels);
                    }
                }
            }
        } else if (buffer instanceof DoubleBuffer) {
            if (imagePlus.isRGB()) {

                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ImageProcessor ip = stack.getProcessor(i);
                    Object data = ip.getPixels();
                    if (data instanceof int[]) {
                        int[] pixels = (int[]) data;
                        double[] rgb = new double[3];
                        for (int pixel : pixels) {
                            rgb[0] = (pixel >> 16) & 0xFF;
                            rgb[1] = (pixel >> 8) & 0xFF;
                            rgb[2] = pixel & 0xFF;
                            ((DoubleBuffer) buffer).put(rgb);
                        }
                    }
                }
            } else {

                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ImageProcessor ip = stack.getProcessor(i);
                    Object data = ip.getPixels();
                    if (data instanceof double[]) {
                        double[] pixels = (double[]) data;
                        ((DoubleBuffer) buffer).put(pixels);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported pixel type: " + buffer.getClass().getSimpleName());
        }

        // Free memory
        System.gc();

        return imageITK;
    }

    // Direct Kind
    public static Image convertImagePlus2ImageITKST(ImagePlus imagePlus) {
        long[] coordinates = new long[3];
        coordinates[0] = imagePlus.getWidth();
        coordinates[1] = imagePlus.getHeight();
        coordinates[2] = imagePlus.getStackSize();
        VectorUInt32 size = new VectorUInt32(coordinates);
        PixelIDValueEnum pixelType = convertBufferImage2ITKEnum(imagePlus);
        Image imageITK = new Image(size, Objects.requireNonNull(pixelType), (imagePlus.isRGB() ? 3 : 1));

        Buffer buffer = imageITK.getBufferAsBuffer();
        buffer.rewind();

        ImageStack stack = imagePlus.getImageStack();
        ImageProcessor ip;
        Object data = null;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);
        CountDownLatch latch = new CountDownLatch(stack.getSize());

        if (buffer instanceof ByteBuffer) {
            if (imagePlus.isRGB()) {

                int depth = stack.getSize();
                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    ip = stack.getProcessor(depthIndex);
                    data = ip.getPixels();
                    Object finalData = data;
                    completionService.submit(() -> {
                        if (finalData instanceof int[]) {
                            int[] pixels = (int[]) finalData;
                            byte[] rgb = new byte[3];
                            for (int pixel : pixels) {
                                rgb[0] = (byte) ((pixel >> 16) & 0xFF);
                                rgb[1] = (byte) ((pixel >> 8) & 0xFF);
                                rgb[2] = (byte) (pixel & 0xFF);
                                ((ByteBuffer) buffer).put(rgb);
                            }
                        }
                        latch.countDown();
                    }, null);
                }
            } else {
                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ip = stack.getProcessor(i);
                    data = ip.getPixels();
                    if (data instanceof byte[]) {
                        byte[] pixels = (byte[]) data;
                        ((ByteBuffer) buffer).put(pixels);
                    }
                    latch.countDown();
                }
            }
        } else if (buffer instanceof ShortBuffer) {
            if (imagePlus.isRGB()) {
                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ip = stack.getProcessor(i);
                    data = ip.getPixels();
                    Object finalData1 = data;
                    completionService.submit(() -> {
                        if (finalData1 instanceof int[]) {
                            int[] pixels = (int[]) finalData1;
                            short[] rgb = new short[3];
                            for (int pixel : pixels) {
                                rgb[0] = (short) ((pixel >> 16) & 0xFF);
                                rgb[1] = (short) ((pixel >> 8) & 0xFF);
                                rgb[2] = (short) (pixel & 0xFF);
                                ((ShortBuffer) buffer).put(rgb);
                            }
                        }
                        latch.countDown();
                    }, null);
                }
            } else {
                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ip = stack.getProcessor(i);
                    data = ip.getPixels();
                    if (data instanceof short[]) {
                        short[] pixels = (short[]) data;
                        ((ShortBuffer) buffer).put(pixels);
                    }
                    latch.countDown();
                }
            }
        } else if (buffer instanceof CharBuffer) {
            if (imagePlus.isRGB()) {

                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ip = stack.getProcessor(i);
                    data = ip.getPixels();
                    Object finalData = data;
                    completionService.submit(() -> {
                        if (finalData instanceof int[]) {
                            int[] pixels = (int[]) finalData;
                            char[] rgb = new char[3];
                            for (int pixel : pixels) {
                                rgb[0] = (char) ((pixel >> 16) & 0xFF);
                                rgb[1] = (char) ((pixel >> 8) & 0xFF);
                                rgb[2] = (char) (pixel & 0xFF);
                                ((CharBuffer) buffer).put(rgb);
                            }
                        }
                        latch.countDown();
                    }, null);
                }
            } else {

                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ip = stack.getProcessor(i);
                    data = ip.getPixels();
                    final Object finalData = data;
                    completionService.submit(() -> {
                        if (finalData instanceof short[]) {
                            short[] pixels = (short[]) finalData;
                            char[] pixelsChar = new char[pixels.length];
                            for (int j = 0; j < pixels.length; j++) {
                                pixelsChar[j] = (char) pixels[j];
                            }
                            ((CharBuffer) buffer).put(pixelsChar);
                        }
                        latch.countDown();
                    }, null);
                }
            }
        } else if (buffer instanceof IntBuffer) {
            if (imagePlus.isRGB()) {

                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ip = stack.getProcessor(i);
                    data = ip.getPixels();
                    Object finalData = data;
                    completionService.submit(() -> {
                        if (finalData instanceof int[]) {
                            int[] pixels = (int[]) finalData;
                            int[] rgb = new int[3];
                            for (int pixel : pixels) {
                                rgb[0] = (pixel >> 16) & 0xFF;
                                rgb[1] = (pixel >> 8) & 0xFF;
                                rgb[2] = pixel & 0xFF;
                                ((IntBuffer) buffer).put(rgb);
                            }
                        }
                        latch.countDown();
                    }, null);
                }
            } else {

                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ip = stack.getProcessor(i);
                    data = ip.getPixels();
                    if (data instanceof int[]) {
                        int[] pixels = (int[]) data;
                        ((IntBuffer) buffer).put(pixels);
                    }
                    latch.countDown();
                }

            }
        } else if (buffer instanceof FloatBuffer) {
            if (imagePlus.isRGB()) {

                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ip = stack.getProcessor(i);
                    data = ip.getPixels();
                    Object finalData = data;
                    completionService.submit(() -> {
                        if (finalData instanceof int[]) {
                            int[] pixels = (int[]) finalData;
                            float[] rgb = new float[3];
                            for (int pixel : pixels) {
                                rgb[0] = (pixel >> 16) & 0xFF;
                                rgb[1] = (pixel >> 8) & 0xFF;
                                rgb[2] = pixel & 0xFF;
                                ((FloatBuffer) buffer).put(rgb);
                            }
                        }
                        latch.countDown();
                    }, null);
                }
            } else {

                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ip = stack.getProcessor(i);
                    data = ip.getPixels();
                    if (data instanceof float[]) {
                        float[] pixels = (float[]) data;
                        ((FloatBuffer) buffer).put(pixels);
                    }
                    latch.countDown();
                }
            }
        } else if (buffer instanceof DoubleBuffer) {
            if (imagePlus.isRGB()) {

                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ip = stack.getProcessor(i);
                    data = ip.getPixels();
                    Object finalData = data;
                    completionService.submit(() -> {
                        if (finalData instanceof int[]) {
                            int[] pixels = (int[]) finalData;
                            double[] rgb = new double[3];
                            for (int pixel : pixels) {
                                rgb[0] = (pixel >> 16) & 0xFF;
                                rgb[1] = (pixel >> 8) & 0xFF;
                                rgb[2] = pixel & 0xFF;
                                ((DoubleBuffer) buffer).put(rgb);
                            }
                        }
                        latch.countDown();
                    }, null);
                }
            } else {

                int depth = stack.getSize();
                for (int i = 1; i <= depth; i++) {
                    ip = stack.getProcessor(i);
                    data = ip.getPixels();
                    if (data instanceof double[]) {
                        double[] pixels = (double[]) data;
                        ((DoubleBuffer) buffer).put(pixels);
                    }
                    latch.countDown();
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported pixel type: " + buffer.getClass().getSimpleName());
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();

        // Free memory
        System.gc();

        return imageITK;
    }


    /****************************************************************************/
    // ImageITK to ImagePlus

    /****************************************************************************/

    // BufferedImage Kind
    public static BufferedImage convertITK2BufferedImage(Image itkImage) {
        // Get image properties
        int width = (int) itkImage.getWidth();
        int height = (int) itkImage.getHeight();
        int depth = (int) itkImage.getDepth();
        int numComponents = (int) itkImage.getNumberOfComponentsPerPixel();


        Buffer buffer = itkImage.getBufferAsBuffer();
        int pixelBytes = (buffer.capacity()) / (width * height * max(1, depth) * numComponents);
        if (verbose) {
            System.out.println("Buffer type: " + buffer.getClass().getSimpleName());
            System.out.println("Pixel size: " + pixelBytes);
            System.out.println("Number of components: " + numComponents);
            System.out.println("Depth: " + depth);
            System.out.println("Image type: " + itkImage.getPixelIDTypeAsString());
        }
        BufferedImage image = null;

        // Depending on the number of components, we can convert the Buffer to different types
        if (numComponents == 1) {
            // buffer type == java.nio.ByteBuffer
            if (buffer instanceof ByteBuffer) {
                // Tableau de données pour chaque couche de profondeur
                byte[] data = new byte[width * height];
                buffer.rewind();

                // BufferedImage en prenant en compte la profondeur
                image = new BufferedImage(width, height * Math.max(depth, 1), BufferedImage.TYPE_BYTE_GRAY);
                WritableRaster raster = image.getRaster();

                // Si la profondeur est égale à 1, les données pour la première couche
                if (depth == 0) {
                    ((ByteBuffer) buffer).get(data);
                    raster.setDataElements(0, 0, width, height, data);
                } else {
                    // Pour chaque couche de profondeur, definition-les sur le raster de l'image
                    for (int i = 0; i < depth; i++) {
                        ((ByteBuffer) buffer).get(data);
                        raster.setDataElements(0, i * height, width, height, data);
                    }
                }
                return image;
            } else if (buffer instanceof ShortBuffer) {
                short[] data = new short[width * height];
                buffer.rewind();

                // Création de l'image BufferedImage avec le type approprié
                image = new BufferedImage(width, height * Math.max(depth, 1), BufferedImage.TYPE_USHORT_GRAY);
                WritableRaster raster = image.getRaster();
                if (depth == 0) {
                    ((ShortBuffer) buffer).get(data);
                    raster.setDataElements(0, 0, width, height, data);
                } else {
                    for (int i = 0; i < depth; i++) {
                        ((ShortBuffer) buffer).get(data);
                        raster.setDataElements(0, i * height, width, height, data);
                    }
                }
                return image;
            } else if (buffer instanceof CharBuffer) {
                buffer.rewind();

                Charset charset = StandardCharsets.UTF_8;
                CharBuffer charBuffer = (CharBuffer) buffer;
                ShortBuffer shortBuffer = ShortBuffer.allocate(charBuffer.capacity());
                for (int i = 0; i < charBuffer.capacity(); i++) {
                    shortBuffer.put((short) charBuffer.get());
                }
                shortBuffer.rewind();
                short[] data = new short[width * height];


                // Création de l'image BufferedImage avec le type approprié
                image = new BufferedImage(width, height * max(depth, 1), BufferedImage.TYPE_USHORT_GRAY);
                WritableRaster raster = image.getRaster();
                if (depth == 0) {
                    shortBuffer.get(data);
                    raster.setDataElements(0, 0, width, height, data);
                } else {
                    for (int i = 0; i < depth; i++) {
                        shortBuffer.get(data);
                        raster.setDataElements(0, i * height, width, height, data);
                    }
                }
                return image;
            } else if (buffer instanceof IntBuffer) {
                int[] data = new int[width * height];
                buffer.rewind();

                // Création de l'image BufferedImage avec le type approprié
                image = new BufferedImage(width, height * Math.max(depth, 1), BufferedImage.TYPE_INT_ARGB);
                WritableRaster raster = image.getRaster();
                if (depth == 0) {
                    ((IntBuffer) buffer).get(data);
                    raster.setDataElements(0, 0, width, height, data);
                } else {
                    for (int i = 0; i < depth; i++) {
                        ((IntBuffer) buffer).get(data);
                        raster.setDataElements(0, i * height, width, height, data);
                    }
                }
                return image;
            } else if (buffer instanceof FloatBuffer) {
                float[] floatData = new float[width * height * max(1, depth)];
                buffer.rewind();

                ((FloatBuffer) buffer).get(floatData);

                FloatProcessor fp = new FloatProcessor(width, height * max(1, depth), floatData);
                return fp.getBufferedImage();
            } else if (buffer instanceof DoubleBuffer) {
                double[] doubleData = new double[width * height * max(1, depth)];
                buffer.rewind();

                ((DoubleBuffer) buffer).get(doubleData);

                // Convert double array to float array
                float[] floatData = new float[doubleData.length * max(1, depth) * numComponents];
                for (int i = 0; i < doubleData.length; i++) {
                    floatData[i] = (float) doubleData[i];
                }

                FloatProcessor fp = new FloatProcessor(width, height * max(1, depth), floatData);
                ImagePlus imagePlus = new ImagePlus("Double Image", fp);

                return imagePlus.getBufferedImage();
            }
        } else if (numComponents == 3) {
            if (pixelBytes == 1) {
                byte[] data = new byte[width * height * numComponents];
                buffer.rewind();

                // Création de l'image BufferedImage avec le type approprié
                image = new BufferedImage(width, height * Math.max(depth, 1), BufferedImage.TYPE_3BYTE_BGR);
                WritableRaster raster = image.getRaster();

                if (depth == 0) {
                    ((ByteBuffer) buffer).get(data);
                    raster.setDataElements(0, 0, width, height, data);
                    /*int[] rgb = new int[width * height];
                    for (int i = 0; i < width * height; i++) {
                        int r = ((ByteBuffer) buffer).get() & 0xFF;
                        int g = ((ByteBuffer) buffer).get() & 0xFF;
                        int b = ((ByteBuffer) buffer).get() & 0xFF;
                        rgb[i] = (r << 16) | (g << 8) | b;
                    }
                    image.setRGB(0, 0, width, height, rgb, 0, width);*/
                } else {
                    /*for (int i = 0; i < depth; i++) {
                        int[] rgb = new int[width * height];
                        for (int j = 0; j < width * height; j++) {
                            int r = ((ByteBuffer) buffer).get() & 0xFF;
                            int g = ((ByteBuffer) buffer).get() & 0xFF;
                            int b = ((ByteBuffer) buffer).get() & 0xFF;
                            rgb[j] = (r << 16) | (g << 8) | b;
                        }
                        image.setRGB(0, i * height, width, height, rgb, 0, width);
                    }*/
                    for (int i = 0; i < depth; i++) {
                        ((ByteBuffer) buffer).get(data);
                        raster.setDataElements(0, i * height, width, height, data);
                    }
                }
                return image;
            } else if (pixelBytes == 2) {
                short[] data = new short[width * height * numComponents];
                buffer.rewind();

                // Création de l'image BufferedImage avec le type approprié
                image = new BufferedImage(width, height * Math.max(depth, 1), BufferedImage.TYPE_USHORT_555_RGB);
                WritableRaster raster = image.getRaster();

                if (depth == 0) {
                    ((ShortBuffer) buffer).get(data);
                    raster.setDataElements(0, 0, width, height, data);
                } else {
                    for (int i = 0; i < depth; i++) {
                        ((ShortBuffer) buffer).get(data);
                        raster.setDataElements(0, i * height, width, height, data);
                    }
                }
                return image;
            } else if (pixelBytes == 4) {
                int[] data = new int[width * height * numComponents];
                buffer.rewind();

                // Création de l'image BufferedImage avec le type approprié
                image = new BufferedImage(width, height * Math.max(depth, 1), BufferedImage.TYPE_INT_ARGB);
                WritableRaster raster = image.getRaster();

                if (depth == 0) {
                    ((IntBuffer) buffer).get(data);
                    raster.setDataElements(0, 0, width, height, data);
                } else {
                    for (int i = 0; i < depth; i++) {
                        ((IntBuffer) buffer).get(data);
                        raster.setDataElements(0, i * height, width, height, data);
                    }
                }
                return image;
            } else {
                throw new IllegalArgumentException("Unsupported pixel type: " + pixelBytes);
            }
        } else {
            throw new IllegalArgumentException("Unsupported number of components: " + numComponents);
        }

        return null;

    }

    public static ImagePlus convertITK2ImagePlus(Image itkImage) {
        // Get image properties
        int width = (int) itkImage.getWidth();
        int height = (int) itkImage.getHeight();
        int depth = (int) itkImage.getDepth();
        int numComponents = (int) itkImage.getNumberOfComponentsPerPixel();

        Buffer buffer = itkImage.getBufferAsBuffer();
        int pixelBytes = (buffer.capacity()) / (width * height * max(1, depth) * numComponents);
        if (verbose) {
            System.out.println("Buffer type: " + buffer.getClass().getSimpleName());
            System.out.println("Pixel size: " + pixelBytes);
            System.out.println("Number of components: " + numComponents);
            System.out.println("Depth: " + depth);
        }
        BufferedImage image = convertITK2BufferedImage(itkImage);
        ImageStack stack = new ImageStack(width, height);
        Object data = Objects.requireNonNull(image).getRaster().getDataElements(0, 0, width, height, null);

        boolean isGray = (numComponents == 1);

        if (isGray) {
            if (depth == 0) {
                stack.addSlice("Image", data);
            } else {
                for (int i = 0; i < depth; i++) {
                    stack.addSlice("Slice " + (i + 1), data);
                    data = image.getRaster().getDataElements(0, i * height, width, height, null);
                }
            }
        } else {
            if (depth == 0) {
                ColorProcessor colorProcessor = new ColorProcessor(image);
                stack.addSlice("Image", colorProcessor.getBufferedImage().getRaster().getDataElements(0, 0, width, height, null));
            } else {
                for (int i = 0; i < depth; i++) {
                    ColorProcessor colorProcessor = new ColorProcessor(image);
                    stack.addSlice("Slice " + (i + 1), colorProcessor.getBufferedImage().getRaster().getDataElements(0, i * height, width, height, null));
                }
            }
        }
        return new ImagePlus("Image", stack);
    }

    // Direct Kind
    public static ImagePlus convertITK2ImagePlusDirect(Image itkImage) {
        // Get image properties
        int width = (int) itkImage.getWidth();
        int height = (int) itkImage.getHeight();
        int depth = (int) itkImage.getDepth();
        int numComponents = (int) itkImage.getNumberOfComponentsPerPixel();


        Buffer buffer = itkImage.getBufferAsBuffer();
        int pixelBytes = (buffer.capacity()) / (width * height * max(1, depth) * numComponents);
        if (verbose) {
            System.out.println("Buffer type: " + buffer.getClass().getSimpleName());
            System.out.println("Pixel size: " + pixelBytes);
            System.out.println("Number of components: " + numComponents);
            System.out.println("Depth: " + depth);
            System.out.println("Image type: " + itkImage.getPixelIDTypeAsString());
        }
        ImageStack stack = new ImageStack(width, height);

        // Depending on the number of components, we can convert the Buffer to different types
        if (numComponents == 1) {
            // buffer type == java.nio.ByteBuffer
            if (buffer instanceof ByteBuffer) {
                // Tableau de données pour chaque couche de profondeur
                byte[] data = new byte[width * height * max(1, depth)];
                buffer.rewind();

                ((ByteBuffer) buffer).get(data);

                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    byte[] sliceData = new byte[width * height];
                    System.arraycopy(data, depthIndex * width * height, sliceData, 0, width * height);
                    ByteProcessor bp = new ByteProcessor(width, height, sliceData, null);
                    stack.addSlice("Slice " + (depthIndex + 1), bp);
                }

                // free memory
                buffer = null;
                System.gc();

                return new ImagePlus("Byte Image", stack);
            } else if (buffer instanceof ShortBuffer) {
                short[] data = new short[width * height * max(1, depth)];
                buffer.rewind();

                ((ShortBuffer) buffer).get(data);

                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    short[] sliceData = new short[width * height];
                    System.arraycopy(data, depthIndex * width * height, sliceData, 0, width * height);
                    ShortProcessor sp = new ShortProcessor(width, height, sliceData, null);
                    stack.addSlice("Slice " + (depthIndex + 1), sp);
                }

                // free memory
                buffer = null;
                System.gc();

                return new ImagePlus("Short Image", stack);
            } else if (buffer instanceof CharBuffer) {
                buffer.rewind();

                CharBuffer charBuffer = (CharBuffer) buffer;
                ShortBuffer shortBuffer = ShortBuffer.allocate(charBuffer.capacity());
                for (int i = 0; i < charBuffer.capacity(); i++) {
                    shortBuffer.put((short) charBuffer.get());
                }
                shortBuffer.rewind();
                short[] data = new short[width * height * max(1, depth)];

                shortBuffer.get(data);
                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    short[] sliceData = new short[width * height];
                    System.arraycopy(data, depthIndex * width * height, sliceData, 0, width * height);
                    ShortProcessor sp = new ShortProcessor(width, height, sliceData, null);
                    stack.addSlice("Slice " + (depthIndex + 1), sp);
                }

                // free memory
                buffer = null;
                System.gc();

                return new ImagePlus("Char Image", stack);
            } else if (buffer instanceof IntBuffer) {
                int[] data = new int[width * height * max(1, depth)];
                buffer.rewind();

                ((IntBuffer) buffer).get(data);


                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    int[] sliceData = new int[width * height];
                    System.arraycopy(data, depthIndex * width * height, sliceData, 0, width * height);
                    ColorProcessor cp = new ColorProcessor(width, height, sliceData);
                    stack.addSlice("Slice " + (depthIndex + 1), cp);
                }

                // free memory
                buffer = null;
                System.gc();

                return new ImagePlus("Int Image", stack);

            } else if (buffer instanceof FloatBuffer) {
                float[] floatData = new float[width * height * max(1, depth)];
                buffer.rewind();

                ((FloatBuffer) buffer).get(floatData);


                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    float[] sliceData = new float[width * height];
                    System.arraycopy(floatData, depthIndex * width * height, sliceData, 0, width * height);
                    FloatProcessor fp = new FloatProcessor(width, height, sliceData);
                    stack.addSlice("Slice " + (depthIndex + 1), fp);
                }

                // free memory
                buffer = null;
                System.gc();

                return new ImagePlus("Float Image", stack);
            } else if (buffer instanceof DoubleBuffer) {
                double[] doubleData = new double[width * height * max(1, depth)];
                buffer.rewind();

                ((DoubleBuffer) buffer).get(doubleData);

                // Convert double array to float array
                float[] floatData = new float[doubleData.length * max(1, depth) * numComponents];
                for (int i = 0; i < doubleData.length; i++) {
                    floatData[i] = (float) doubleData[i];
                }


                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    float[] sliceData = new float[width * height];
                    System.arraycopy(floatData, depthIndex * width * height, sliceData, 0, width * height);
                    FloatProcessor fp = new FloatProcessor(width, height, sliceData);
                    stack.addSlice("Slice " + (depthIndex + 1), fp);
                }

                // free memory
                buffer = null;
                System.gc();

                return new ImagePlus("Double Image", stack);
            }
        } else if (numComponents == 3) {
            if (buffer instanceof ByteBuffer) {
                byte[] data = new byte[width * height * numComponents * max(1, depth)];
                buffer.rewind();

                ((ByteBuffer) buffer).get(data);


                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    byte[] sliceData = new byte[width * height * numComponents * max(1, depth)];

                    System.arraycopy(data, depthIndex * width * height * numComponents, sliceData, 0, width * height * numComponents);

                    int[] rgb = new int[width * height * numComponents];
                    for (int i = 0; i < width * height; i++) {
                        int offset = i * numComponents;
                        int r = sliceData[offset] & 0xFF; // rouge
                        int g = sliceData[offset + 1] & 0xFF; // vert
                        int b = sliceData[offset + 2] & 0xFF; // bleu
                        rgb[i] = (r << 16) | (g << 8) | b; // combine les composantes RGB en une seule valeur
                    }

                    ColorProcessor cp = new ColorProcessor(width, height * numComponents, rgb);
                    stack.addSlice("Slice " + (depthIndex + 1), cp);
                }
                // free memory
                buffer = null;
                System.gc();

                return new ImagePlus("Byte Image", stack);
            } else if (buffer instanceof ShortBuffer) {
                short[] data = new short[width * height * numComponents];
                buffer.rewind();

                ((ShortBuffer) buffer).get(data);


                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    short[] sliceData = new short[width * height * numComponents * max(1, depth)];
                    int[] rgb = new int[width * height];
                    for (int i = 0; i < width * height; i++) {
                        int offset = i * numComponents;
                        int r = data[offset] & 0xFFFF;
                        int g = data[offset + 1] & 0xFFFF;
                        int b = data[offset + 2] & 0xFFFF;
                        rgb[i] = (r << 16) | (g << 8) | b;
                    }
                    System.arraycopy(data, depthIndex * width * height * numComponents, sliceData, 0, width * height * numComponents);
                    ColorProcessor cp = new ColorProcessor(width, height, rgb);
                    stack.addSlice("Slice " + (depthIndex + 1), cp);
                }
                // free memory
                buffer = null;
                System.gc();

                return new ImagePlus("Short Image", stack);
            } else if (buffer instanceof CharBuffer) {
                buffer.rewind();

                CharBuffer charBuffer = (CharBuffer) buffer;
                ShortBuffer shortBuffer = ShortBuffer.allocate(charBuffer.capacity());
                for (int i = 0; i < charBuffer.capacity(); i++) {
                    shortBuffer.put((short) charBuffer.get());
                }
                shortBuffer.rewind();
                short[] data = new short[width * height * numComponents];

                shortBuffer.get(data);

                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    short[] sliceData = new short[width * height * numComponents * max(1, depth)];
                    int[] rgb = new int[width * height];
                    for (int i = 0; i < width * height; i++) {
                        int offset = i * numComponents;
                        int r = data[offset] & 0xFFFF;
                        int g = data[offset + 1] & 0xFFFF;
                        int b = data[offset + 2] & 0xFFFF;
                        rgb[i] = (r << 16) | (g << 8) | b;
                    }
                    System.arraycopy(data, depthIndex * width * height * numComponents, sliceData, 0, width * height * numComponents);
                    ColorProcessor cp = new ColorProcessor(width, height, rgb);
                    stack.addSlice("Slice " + (depthIndex + 1), cp);
                }

                // free memory
                buffer = null;
                System.gc();

                return new ImagePlus("Char Image", stack);
            } else if (buffer instanceof IntBuffer) {
                int[] data = new int[width * height * numComponents];
                buffer.rewind();

                ((IntBuffer) buffer).get(data);

                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    int[] sliceData = new int[width * height * numComponents * max(1, depth)];
                    int[] rgb = new int[width * height];
                    for (int i = 0; i < width * height; i++) {
                        int offset = i * numComponents;
                        int r = data[offset];
                        int g = data[offset + 1];
                        int b = data[offset + 2];
                        rgb[i] = (r << 16) | (g << 8) | b;
                    }
                    System.arraycopy(data, depthIndex * width * height * numComponents, sliceData, 0, width * height * numComponents);
                    ColorProcessor cp = new ColorProcessor(width, height, rgb);
                    stack.addSlice("Slice " + (depthIndex + 1), cp);
                }
            } else if (buffer instanceof FloatBuffer) {
                float[] floatData = new float[width * height * numComponents];
                buffer.rewind();

                ((FloatBuffer) buffer).get(floatData);

                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    float[] sliceData = new float[width * height * numComponents];
                    int[] rgb = new int[width * height];
                    for (int i = 0; i < width * height; i++) {
                        int offset = i * numComponents;
                        int r = (int) floatData[offset];
                        int g = (int) floatData[offset + 1];
                        int b = (int) floatData[offset + 2];
                        rgb[i] = (r << 16) | (g << 8) | b;
                    }
                    System.arraycopy(floatData, depthIndex * width * height * numComponents, sliceData, 0, width * height * numComponents);
                    ColorProcessor cp = new ColorProcessor(width, height, rgb);
                    stack.addSlice("Slice " + (depthIndex + 1), cp);
                }
            } else if (buffer instanceof DoubleBuffer) {
                double[] doubleData = new double[width * height * numComponents];
                buffer.rewind();

                ((DoubleBuffer) buffer).get(doubleData);

                // Convert double array to float array
                float[] floatData = new float[doubleData.length * numComponents];
                for (int i = 0; i < doubleData.length; i++) {
                    floatData[i] = (float) doubleData[i];
                }

                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    float[] sliceData = new float[width * height * numComponents];
                    int[] rgb = new int[width * height];
                    for (int i = 0; i < width * height; i++) {
                        int offset = i * numComponents;
                        int r = (int) floatData[offset];
                        int g = (int) floatData[offset + 1];
                        int b = (int) floatData[offset + 2];
                        rgb[i] = (r << 16) | (g << 8) | b;
                    }
                    System.arraycopy(floatData, depthIndex * width * height * numComponents, sliceData, 0, width * height * numComponents);
                    ColorProcessor cp = new ColorProcessor(width, height, rgb);
                    stack.addSlice("Slice " + (depthIndex + 1), cp);
                }
            } else {
                throw new IllegalArgumentException("Unsupported pixel type: " + pixelBytes);
            }
        } else {
            throw new IllegalArgumentException("Unsupported number of components: " + numComponents);
        }

        return null;

    }

    public static ImagePlus convertITK2ImagePlusDirectST(Image itkImage) {
        // Get image properties
        int width = (int) itkImage.getWidth();
        int height = (int) itkImage.getHeight();
        int depth = (int) itkImage.getDepth();
        int dim = (int) itkImage.getDimension();

        int numComponents = (int) itkImage.getNumberOfComponentsPerPixel();


        Buffer buffer = itkImage.getBufferAsBuffer();
        int pixelBytes = (buffer.capacity()) / (width * height * max(1, depth) * numComponents);
        if (verbose) {
            System.out.println("Buffer type: " + buffer.getClass().getSimpleName());
            System.out.println("Pixel size: " + pixelBytes);
            System.out.println("Number of components: " + numComponents);
            System.out.println("Depth: " + depth);
            System.out.println("Image type: " + itkImage.getPixelIDTypeAsString());
        }

        final ImageStack stack = new ImageStack(width, height);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);
        CountDownLatch latch = new CountDownLatch(Math.max(1, depth));

        // Depending on the number of components, we can convert the Buffer to different types
        if (numComponents == 1) {
            // buffer type == java.nio.ByteBuffer
            if (buffer instanceof ByteBuffer) {
                // Tableau de données pour chaque couche de profondeur
                byte[] data = new byte[width * height * max(1, depth)];
                buffer.rewind();

                ((ByteBuffer) buffer).get(data);

                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    final int sliceIndex = depthIndex;
                    Future<Void> future = completionService.submit(() -> {
                        byte[] sliceData = new byte[width * height];
                        System.arraycopy(data, sliceIndex * width * height, sliceData, 0, width * height);
                        ByteProcessor bp = new ByteProcessor(width, height, sliceData, null);
                        synchronized (stack) {
                            stack.addSlice("Slice " + (sliceIndex + 1), bp);
                        }
                        latch.countDown();
                        return null;
                    });
                }
            } else if (buffer instanceof ShortBuffer) {
                short[] data = new short[width * height * max(1, depth)];
                buffer.rewind();

                ((ShortBuffer) buffer).get(data);

                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    final int sliceIndex = depthIndex;
                    executor.submit(() -> {
                        short[] sliceData = new short[width * height];
                        System.arraycopy(data, sliceIndex * width * height, sliceData, 0, width * height);
                        ShortProcessor sp = new ShortProcessor(width, height, sliceData, null);
                        synchronized (stack) {
                            stack.addSlice("Slice " + (sliceIndex + 1), sp);
                        }
                        latch.countDown();
                    });
                }
            } else if (buffer instanceof CharBuffer) {
                buffer.rewind();

                //CharBuffer charBuffer = (CharBuffer) buffer;
                //ShortBuffer shortBuffer = convertCharBufferToShortBuffer(charBuffer);
                //shortBuffer.rewind();
                CharBuffer charBuffer = (CharBuffer) buffer;
                ShortBuffer shortBuffer = ShortBuffer.allocate(charBuffer.capacity());
                for (int i = 0; i < charBuffer.capacity(); i++) {
                    shortBuffer.put((short) charBuffer.get());
                }
                shortBuffer.rewind();
                short[] data = new short[width * height * max(1, depth)];

                shortBuffer.get(data);

                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    final int sliceIndex = depthIndex;
                    executor.submit(() -> {
                        short[] sliceData = new short[width * height];
                        System.arraycopy(data, sliceIndex * width * height, sliceData, 0, width * height);
                        ShortProcessor sp = new ShortProcessor(width, height, sliceData, null);
                        synchronized (stack) {
                            stack.addSlice("Slice " + (sliceIndex + 1), sp);
                        }
                        latch.countDown();

                    });
                }

            } else {
                if (buffer instanceof IntBuffer) {
                    int[] data = new int[width * height * max(1, depth)];
                    buffer.rewind();

                    ((IntBuffer) buffer).get(data);
                    for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                        final int sliceIndex = depthIndex;
                        executor.submit(() -> {
                            int[] sliceData = new int[width * height];
                            System.arraycopy(data, sliceIndex * width * height, sliceData, 0, width * height);
                            ColorProcessor cp = new ColorProcessor(width, height, sliceData);
                            synchronized (stack) {
                                stack.addSlice("Slice " + (sliceIndex + 1), cp);
                            }
                            latch.countDown();

                        });
                    }
                } else if (buffer instanceof FloatBuffer) {
                    float[] floatData = new float[width * height * max(1, depth)];
                    buffer.rewind();

                    ((FloatBuffer) buffer).get(floatData);
                    for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                        final int sliceIndex = depthIndex;
                        executor.submit(() -> {
                            float[] sliceData = new float[width * height];
                            System.arraycopy(floatData, sliceIndex * width * height, sliceData, 0, width * height);
                            FloatProcessor fp = new FloatProcessor(width, height, sliceData);
                            synchronized (stack) {
                                stack.addSlice("Slice " + (sliceIndex + 1), fp);
                            }
                            latch.countDown();

                        });
                    }
                } else if (buffer instanceof DoubleBuffer) {
                    buffer.rewind();

                    // Convert double array to float array
                    //FloatBuffer floatBuffer = convertDoubleBufferToFloatBuffer((DoubleBuffer) buffer);
                    double[] doubleData = new double[width * height * max(1, depth)];
                    ((DoubleBuffer) buffer).get(doubleData);

                    // Convert double array to float array
                    float[] floatData = new float[doubleData.length * max(1, depth) * numComponents];
                    for (int i = 0; i < doubleData.length; i++) {
                        floatData[i] = (float) doubleData[i];
                    }
                    for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                        final int sliceIndex = depthIndex;
                        executor.submit(() -> {
                            float[] sliceData = new float[width * height];
                            System.arraycopy(floatData, sliceIndex * width * height, sliceData, 0, width * height);
                            FloatProcessor fp = new FloatProcessor(width, height, sliceData);
                            synchronized (stack) {
                                stack.addSlice("Slice " + (sliceIndex + 1), fp);
                            }
                            latch.countDown();

                        });
                    }
                }
            }
        } else if (numComponents == 3) {
            if (buffer instanceof ByteBuffer) {
                byte[] data = new byte[width * height * numComponents * max(1, depth)];
                buffer.rewind();

                ((ByteBuffer) buffer).get(data);
                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    final int sliceIndex = depthIndex;
                    executor.submit(() -> {
                        byte[] sliceData = new byte[width * height * numComponents];
                        System.arraycopy(data, sliceIndex * width * height * numComponents, sliceData, 0, width * height * numComponents);

                        int[] rgb = new int[width * height];
                        for (int i = 0; i < width * height; i++) {
                            int offset = i * numComponents;
                            int r = sliceData[offset] & 0xFF; // rouge
                            int g = sliceData[offset + 1] & 0xFF; // vert
                            int b = sliceData[offset + 2] & 0xFF; // bleu
                            rgb[i] = (r << 16) | (g << 8) | b; // combine les composantes RGB en une seule valeur
                        }
                        ColorProcessor cp = new ColorProcessor(width, height, rgb);
                        synchronized (stack) {
                            stack.addSlice("Slice " + (sliceIndex + 1), cp);
                        }

                        latch.countDown();
                    });
                }
            } else if (buffer instanceof ShortBuffer) {
                short[] data = new short[width * height * numComponents];
                buffer.rewind();

                ((ShortBuffer) buffer).get(data);

                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    final int sliceIndex = depthIndex;
                    executor.submit(() -> {
                        short[] sliceData = new short[width * height * numComponents * max(1, depth)];
                        int[] rgb = new int[width * height];
                        for (int i = 0; i < width * height; i++) {
                            int offset = i * numComponents;
                            int r = data[offset] & 0xFFFF;
                            int g = data[offset + 1] & 0xFFFF;
                            int b = data[offset + 2] & 0xFFFF;
                            rgb[i] = (r << 16) | (g << 8) | b;
                        }
                        System.arraycopy(data, sliceIndex * width * height * numComponents, sliceData, 0, width * height * numComponents);
                        ColorProcessor cp = new ColorProcessor(width, height, rgb);
                        synchronized (stack) {
                            stack.addSlice("Slice " + (sliceIndex + 1), cp);
                        }
                        latch.countDown();
                    });
                }
            } else if (buffer instanceof CharBuffer) {
                buffer.rewind();

                //CharBuffer charBuffer = (CharBuffer) buffer;
                //ShortBuffer shortBuffer = convertCharBufferToShortBuffer(charBuffer);
                //shortBuffer.rewind();
                CharBuffer charBuffer = (CharBuffer) buffer;
                ShortBuffer shortBuffer = ShortBuffer.allocate(charBuffer.capacity());
                for (int i = 0; i < charBuffer.capacity(); i++) {
                    shortBuffer.put((short) charBuffer.get());
                }
                shortBuffer.rewind();
                short[] data = new short[width * height * numComponents];

                shortBuffer.get(data);

                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    final int sliceIndex = depthIndex;
                    executor.submit(() -> {
                        short[] sliceData = new short[width * height * numComponents];
                        int[] rgb = new int[width * height];
                        for (int i = 0; i < width * height; i++) {
                            int offset = i * numComponents;
                            int r = data[offset] & 0xFFFF;
                            int g = data[offset + 1] & 0xFFFF;
                            int b = data[offset + 2] & 0xFFFF;
                            rgb[i] = (r << 16) | (g << 8) | b;
                        }
                        System.arraycopy(data, sliceIndex * width * height * numComponents, sliceData, 0, width * height * numComponents);
                        ColorProcessor cp = new ColorProcessor(width, height, rgb);
                        synchronized (stack) {
                            stack.addSlice("Slice " + (sliceIndex + 1), cp);
                        }
                        latch.countDown();
                    });
                }

            } else if (buffer instanceof IntBuffer) {
                int[] data = new int[width * height * numComponents];
                buffer.rewind();

                ((IntBuffer) buffer).get(data);

                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    final int sliceIndex = depthIndex;
                    executor.submit(() -> {
                        int[] sliceData = new int[width * height * numComponents];
                        int[] rgb = new int[width * height];
                        for (int i = 0; i < width * height; i++) {
                            int offset = i * numComponents;
                            int r = data[offset] & 0xFFFFFFFF;
                            int g = data[offset + 1] & 0xFFFFFFFF;
                            int b = data[offset + 2] & 0xFFFFFFFF;
                            rgb[i] = (r << 16) | (g << 8) | b;
                        }
                        System.arraycopy(data, sliceIndex * width * height * numComponents, sliceData, 0, width * height * numComponents);
                        ColorProcessor cp = new ColorProcessor(width, height, rgb);
                        synchronized (stack) {
                            stack.addSlice("Slice " + (sliceIndex + 1), cp);
                        }
                        latch.countDown();
                    });
                }
            } else if (buffer instanceof FloatBuffer) {
                float[] floatData = new float[width * height * numComponents];
                buffer.rewind();

                ((FloatBuffer) buffer).get(floatData);

                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    final int sliceIndex = depthIndex;
                    executor.submit(() -> {
                        float[] sliceData = new float[width * height * numComponents];
                        int[] rgb = new int[width * height];
                        for (int i = 0; i < width * height; i++) {
                            int offset = i * numComponents;
                            int r = (int) floatData[offset];
                            int g = (int) floatData[offset + 1];
                            int b = (int) floatData[offset + 2];
                            rgb[i] = (r << 16) | (g << 8) | b;
                        }
                        System.arraycopy(floatData, sliceIndex * width * height * numComponents, sliceData, 0, width * height * numComponents);
                        ColorProcessor cp = new ColorProcessor(width, height, rgb);
                        synchronized (stack) {
                            stack.addSlice("Slice " + (sliceIndex + 1), cp);
                        }
                        latch.countDown();
                    });
                }
            } else if (buffer instanceof DoubleBuffer) {
                buffer.rewind();

                // Convert double array to float array
                //FloatBuffer floatBuffer = convertDoubleBufferToFloatBuffer((DoubleBuffer) buffer);
                double[] doubleData = new double[width * height * numComponents];
                ((DoubleBuffer) buffer).get(doubleData);

                // Convert double array to float array
                float[] floatData = new float[doubleData.length * numComponents];
                for (int i = 0; i < doubleData.length; i++) {
                    floatData[i] = (float) doubleData[i];
                }

                for (int depthIndex = 0; depthIndex < max(1, depth); depthIndex++) {
                    final int sliceIndex = depthIndex;
                    executor.submit(() -> {
                        float[] sliceData = new float[width * height * numComponents];
                        int[] rgb = new int[width * height];
                        for (int i = 0; i < width * height; i++) {
                            int offset = i * numComponents;
                            int r = (int) floatData[offset];
                            int g = (int) floatData[offset + 1];
                            int b = (int) floatData[offset + 2];
                            rgb[i] = (r << 16) | (g << 8) | b;
                        }
                        System.arraycopy(floatData, sliceIndex * width * height * numComponents, sliceData, 0, width * height * numComponents);
                        ColorProcessor cp = new ColorProcessor(width, height, rgb);
                        synchronized (stack) {
                            stack.addSlice("Slice " + (sliceIndex + 1), cp);
                        }
                        latch.countDown();
                    });
                }
            } else {
                throw new IllegalArgumentException("Unsupported pixel type: " + pixelBytes + " for " + itkImage.getPixelIDTypeAsString());
            }
        } else {
            throw new IllegalArgumentException("Unsupported number of components: " + numComponents);
        }

        try {
            latch.await(); // Wait until all slices are processed
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();

        // free memory
        buffer = null;
        System.gc();

        return new ImagePlus("Image", stack);

    }


    /****************************************************************************/

    // Helper methods
    public static ShortBuffer convertCharBufferToShortBuffer(CharBuffer charBuffer) {
        int bufferSize = charBuffer.capacity();
        ShortBuffer shortBuffer = ShortBuffer.allocate(bufferSize);

        int numThreads = Runtime.getRuntime().availableProcessors(); // Number of threads to use
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        int batchSize = bufferSize / numThreads; // Split buffer size into batches for each thread

        for (int i = 0; i < numThreads; i++) {
            final int startIdx = i * batchSize;
            final int endIdx = (i == numThreads - 1) ? bufferSize : (i + 1) * batchSize;

            executor.submit(() -> {
                for (int j = startIdx; j < endIdx; j++) {
                    shortBuffer.put((short) charBuffer.get(j));
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        shortBuffer.rewind(); // Rewind buffer to the beginning after all threads complete
        return shortBuffer;
    }

    public static FloatBuffer convertDoubleBufferToFloatBuffer(DoubleBuffer doubleBuffer) {
        int bufferSize = doubleBuffer.capacity();
        FloatBuffer floatBuffer = FloatBuffer.allocate(bufferSize);

        int numThreads = Runtime.getRuntime().availableProcessors(); // Number of threads to use
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        int batchSize = bufferSize / numThreads; // Split buffer size into batches for each thread

        for (int i = 0; i < numThreads; i++) {
            final int startIdx = i * batchSize;
            final int endIdx = (i == numThreads - 1) ? bufferSize : (i + 1) * batchSize;

            executor.submit(() -> {
                for (int j = startIdx; j < endIdx; j++) {
                    floatBuffer.put((float) doubleBuffer.get(j));
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        floatBuffer.rewind(); // Rewind buffer to the beginning after all threads complete
        return floatBuffer;
    }


}