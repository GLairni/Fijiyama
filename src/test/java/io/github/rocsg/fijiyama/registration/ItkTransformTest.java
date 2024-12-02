package io.github.rocsg.fijiyama.registration;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

class ItkTransformTest {

    private ItkTransform imageTransform;
    private ImagePlus imgRef;
    private ImagePlus imgMov;

    @BeforeEach
    public void setUp() {
        imageTransform = new ItkTransform();
        // random number of slices and frames
        int nSlices = 10 + (int) (Math.random() * 10);
        imgRef = createTestImage(100, 100, nSlices);  // Reference image: 100x100, nSlices, nFrames
        imgMov = createTestImage(100, 100, nSlices);  // Moving image: 100x100, nSlices, nFrames
        System.out.println("Reference image: " + imgRef.getWidth() + "x" + imgRef.getHeight() + "x" + imgRef.getNSlices());
    }

    private ImagePlus createTestImage(int width, int height, int nSlices) {
        ImageStack stack = new ImageStack(width, height);
        // create 3D image
        for (int i = 0; i < Math.max(nSlices, 1); i++) {
            ImageProcessor ip = new ij.process.FloatProcessor(width, height);
            // Create image with random pixel values
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    ip.setf(x, y, (float) Math.random());
                }
            }
            stack.addSlice(ip);
        }

        return new ImagePlus("Test Image", stack);
    }

    @Test
    public void testTransformImageWithBasicParameters() {
        ImagePlus result = imageTransform.transformImage(imgRef, imgMov);
        assertNotNull(result);
        assertEquals(imgRef.getWidth(), result.getWidth());
        assertEquals(imgRef.getHeight(), result.getHeight());
    }

    @Test
    public void testTransformImageWithSmoothing() {
        boolean smoothingBeforeDownSampling = true;
        ImagePlus result = imageTransform.transformImage(imgRef, imgMov, smoothingBeforeDownSampling);
        assertNotNull(result);
        assertEquals(imgRef.getWidth(), result.getWidth());
        assertEquals(imgRef.getHeight(), result.getHeight());
    }

    @Test
    public void testTransformImageExtensive() {
        boolean smoothingBeforeDownSampling = true;
        ImagePlus result = imageTransform.transformImageExtensive(imgRef, imgMov, smoothingBeforeDownSampling);
        assertNotNull(result);
        assertEquals(imgRef.getWidth(), result.getWidth());
        assertEquals(imgRef.getHeight(), result.getHeight());
    }

    @Test
    public void testTransformImageWithHyperstack() {
        // Create hyperstack reference image (3 channels, 1 slice, 5 frames)
        imgRef = createTestImage(100, 100, 1);
        imgRef.setDimensions(3, 1, 5);

        // Create hyperstack moving image (3 channels, 1 slice, 5 frames)
        imgMov = createTestImage(100, 100, 1);
        imgMov.setDimensions(3, 1, 5);

        ImagePlus result = imageTransform.transformImage(imgRef, imgMov);
        assertNotNull(result);
        assertEquals(imgRef.getWidth(), result.getWidth());
        assertEquals(imgRef.getHeight(), result.getHeight());
        assertEquals(imgRef.getNChannels(), result.getNChannels());
        assertEquals(imgRef.getNFrames(), result.getNFrames());
    }

    @Test
    public void testTransformImageWithDifferentSize() {
        // Create moving image with a different size
        imgMov = createTestImage(200, 200, 1);

        ImagePlus result = imageTransform.transformImage(imgRef, imgMov);
        assertNotNull(result);
        assertEquals(imgRef.getWidth(), result.getWidth(), "The width of the transformed image should match the reference image");
        assertEquals(imgRef.getHeight(), result.getHeight(), "The height of the transformed image should match the reference image");
    }

    @Test
    public void testTransformImageWithMultipleSlices() {
        // Create reference and moving images with multiple slices
        imgRef = createTestImage(100, 100, 5);
        imgMov = createTestImage(100, 100, 5);

        ImagePlus result = imageTransform.transformImage(imgRef, imgMov);
        assertNotNull(result);
        assertEquals(imgRef.getNSlices(), result.getNSlices(), "The number of slices should match the reference image");
    }

    @Test
    public void testTransformImageTimingActions() {
        long initialTime = System.currentTimeMillis();
        boolean timeActions = true;
        ImagePlus result = imageTransform.transformImage(imgRef, imgMov, false, timeActions, initialTime, false);
        assertNotNull(result);
        assertEquals(imgRef.getWidth(), result.getWidth());
        assertEquals(imgRef.getHeight(), result.getHeight());
    }

    @Test
    public void testTransformImageExtensiveWithoutSmoothing() {
        ImagePlus result = imageTransform.transformImageExtensive(imgRef, imgMov, false);
        assertNotNull(result);
        assertEquals(imgRef.getWidth(), result.getWidth());
        assertEquals(imgRef.getHeight(), result.getHeight());
    }

    @Test
    public void testTransformImageWith2DTranslation() {
        // Define a translation in space with random integer values
        double[] translation = {(int) (Math.random() * imgRef.getWidth()), (int) (Math.random() * imgRef.getHeight()), 0};
        ItkTransform transform = ItkTransform.getRigidTransform(
                new double[]{0, 0, 0}, new double[]{0, 0, 0}, translation
        );

        // Apply the transformation
        ImagePlus transformedImage = transform.transformImage(imgRef, imgMov.duplicate());
        assertNotNull(transformedImage);

        // Check if the transformed image has the same dimensions as the reference image
        assertEquals(imgRef.getWidth(), transformedImage.getWidth());
        assertEquals(imgRef.getHeight(), transformedImage.getHeight());
        assertEquals(imgRef.getNSlices(), transformedImage.getNSlices());

        // check that the transformed image is not the same as the reference image
        assertNotEquals(imgRef.getProcessor(), transformedImage.getProcessor());

        // check that the transformed image is not the same as the moving image
        assertNotEquals(imgMov.getProcessor(), transformedImage.getProcessor());

        // check that pixels at the same position are not the same
        for (int z = 0; z < imgRef.getNSlices(); z++) {
            for (int y = 0; y < imgRef.getHeight(); y++) {
                for (int x = 0; x < imgRef.getWidth(); x++) {
                    assertNotEquals(imgRef.getProcessor().getf(x, y), transformedImage.getStack().getProcessor(z + 1).getf(x, y));
                }
            }
        }

        // By manually translating the image, we can check if the transformation was successful
        for (int z = 0; z < imgMov.getNSlices(); z++) {
            for (int y = (int) translation[1]; y < imgMov.getHeight() - (int) translation[1]; y++) {
                for (int x = (int) translation[0]; x < imgMov.getWidth() - (int) translation[0]; x++) {
                    assertEquals(imgMov.getProcessor().getf(x + (int) translation[0], y + (int) translation[1]), transformedImage.getProcessor().getf(x, y));
                }
            }
        }
    }

    @Test
    public void testTransformImageWith3DTranslation() {
        // Define a translation in space with random integer values
        double[] translation = {(int) (Math.random() * imgRef.getWidth()), (int) (Math.random() * imgRef.getHeight()), (int) (Math.random() * imgRef.getNSlices())};
        ItkTransform transform = ItkTransform.getRigidTransform(
                new double[]{0, 0, 0}, new double[]{0, 0, 0}, translation
        );

        // Apply the transformation
        ImagePlus transformedImage = transform.transformImage(imgRef, imgMov.duplicate());
        assertNotNull(transformedImage);

        // Check if the transformed image has the same dimensions as the reference image
        assertEquals(imgRef.getWidth(), transformedImage.getWidth());
        assertEquals(imgRef.getHeight(), transformedImage.getHeight());
        assertEquals(imgRef.getNSlices(), transformedImage.getNSlices());

        // check that the transformed image is not the same as the reference image
        assertNotEquals(imgRef.getProcessor(), transformedImage.getProcessor());

        // check that the transformed image is not the same as the moving image
        assertNotEquals(imgMov.getProcessor(), transformedImage.getProcessor());

        // check that pixels at the same position are not the same
        for (int z = 0; z < imgRef.getNSlices(); z++) {
            for (int y = 0; y < imgRef.getHeight(); y++) {
                for (int x = 0; x < imgRef.getWidth(); x++) {
                    assertNotEquals(imgRef.getProcessor().getf(x, y), transformedImage.getStack().getProcessor(z + 1).getf(x, y));
                }
            }
        }

        // By manually translating the image, we can check if the transformation was successful
        for (int z = (int) translation[2]; z < imgMov.getNSlices() - (int) translation[2]; z++) {
            for (int y = (int) translation[1]; y < imgMov.getHeight() - (int) translation[1]; y++) {
                for (int x = (int) translation[0]; x < imgMov.getWidth() - (int) translation[0]; x++) {
                    assertEquals(imgMov.getStack().getProcessor(z + 1 + (int) translation[2]).getf(x + (int) translation[0], y + (int) translation[1]), transformedImage.getStack().getProcessor(z + 1).getf(x, y));
                }
            }
        }
    }
}