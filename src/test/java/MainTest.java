import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MainTest {

    private Object backgroundSnapInstance;

    @Before
    public void setUp() throws Exception {
        // Since BackgroundSnap is package-private, we can instantiate it if this test is in the same package (default).
        // The default package is used since Main.java has no package declaration.
        Class<?> clazz = Class.forName("BackgroundSnap");
        backgroundSnapInstance = clazz.getDeclaredConstructor().newInstance();
    }

    @After
    public void tearDown() {
        // Clean up thread interrupt status if any tests leaked it
        Thread.interrupted();
    }

    @Test
    public void testMainAppStartInterrupt() throws Exception {
        // Run main logic in a separate thread so it doesn't block the test
        Thread appThread = new Thread(() -> {
            Main.main(new String[]{});
        });

        appThread.start();

        // Wait a short time to let it initialize and enter the loop
        Thread.sleep(500);

        // Interrupt the thread to exit the infinite loop in BackgroundSnap.start()
        appThread.interrupt();

        // Wait for the thread to finish cleanly
        appThread.join(2000);

        // Verify the thread finished and isn't alive anymore
        assertFalse("App thread should have exited after interruption", appThread.isAlive());
    }

    @Test
    public void testScaleImage() throws Exception {
        Method scaleImageMethod = backgroundSnapInstance.getClass().getDeclaredMethod("scaleImage", BufferedImage.class);
        scaleImageMethod.setAccessible(true);

        // Test with null input
        Object resultNull = scaleImageMethod.invoke(backgroundSnapInstance, (BufferedImage) null);
        assertNull("Scaling a null image should return null", resultNull);

        // Create a dummy image
        int originalWidth = 100;
        int originalHeight = 50;
        BufferedImage dummyImage = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_ARGB);

        // Invoke scaleImage
        BufferedImage scaledImage = (BufferedImage) scaleImageMethod.invoke(backgroundSnapInstance, dummyImage);

        // Verify it returned a scaled image
        assertNotNull("Scaling a valid image should return a new image", scaledImage);

        // Verify the dimensions are adjusted based on targetWidth/targetHeight logic
        Field targetWidthField = backgroundSnapInstance.getClass().getDeclaredField("targetWidth");
        targetWidthField.setAccessible(true);
        int targetWidth = targetWidthField.getInt(backgroundSnapInstance);

        Field targetHeightField = backgroundSnapInstance.getClass().getDeclaredField("targetHeight");
        targetHeightField.setAccessible(true);
        int targetHeight = targetHeightField.getInt(backgroundSnapInstance);

        assertTrue("Scaled image width should be <= target width", scaledImage.getWidth() <= targetWidth);
        assertTrue("Scaled image height should be <= target height", scaledImage.getHeight() <= targetHeight);
    }

    @Test
    public void testTakeWebcamPhotoHandlesFailure() throws Exception {
        Method takeWebcamPhotoMethod = backgroundSnapInstance.getClass().getDeclaredMethod("takeWebcamPhoto");
        takeWebcamPhotoMethod.setAccessible(true);

        // If the camera failed to initialize (e.g., in CI), this should gracefully return the dwarf image
        // We ensure it doesn't crash
        Object result = takeWebcamPhotoMethod.invoke(backgroundSnapInstance);

        // In headless CI without camera, it should either return the dwarfImage or null
        // (depending on if dwarfImage loaded successfully)
        Field dwarfImageField = backgroundSnapInstance.getClass().getDeclaredField("dwarfImage");
        dwarfImageField.setAccessible(true);
        Object expectedImage = dwarfImageField.get(backgroundSnapInstance);

        assertEquals("Should return dwarfImage when webcam fails", expectedImage, result);
    }

    @Test
    public void testTakeScreenshotHandlesFailure() throws Exception {
        Method takeScreenshotMethod = backgroundSnapInstance.getClass().getDeclaredMethod("takeScreenshot");
        takeScreenshotMethod.setAccessible(true);

        // This might fail in headless CI environments without X11, but should return null or a valid image, not crash
        Object result = takeScreenshotMethod.invoke(backgroundSnapInstance);

        // We just care that it invoked without throwing uncaught exceptions.
        // It could return a valid BufferedImage if it worked, or null/exception caught internally if it failed.
        assertTrue("Result should be null or a BufferedImage", result == null || result instanceof BufferedImage);
    }
}
